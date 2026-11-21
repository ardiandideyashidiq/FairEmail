package eu.faircode.email

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

object BackupCryptoHelper {
    private const val MAGIC_ACCOUNTS = "___FE_ACC_V1___"
    private const val MAGIC_MAIL = "___FE_MAIL_V1___"
    private const val DERIVATION = "PBKDF2WithHmacSHA512"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12

    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance(DERIVATION)
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return factory.generateSecret(spec)
    }

    private fun openEncryptedOutput(raw: OutputStream, magic: String, password: String): CipherOutputStream {
        require(password.isNotEmpty()) { "Password is required" }
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH).also { random.nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)

        raw.write(magic.toByteArray(StandardCharsets.US_ASCII))
        raw.write(salt)
        raw.write(cipher.iv)
        return CipherOutputStream(raw, cipher)
    }

    private fun openEncryptedInput(raw: InputStream, expectedMagic: String, password: String): CipherInputStream {
        require(password.isNotEmpty()) { "Password is required" }
        val magicBytes = ByteArray(expectedMagic.length)
        Helper.readBuffer(raw, magicBytes)
        val magic = String(magicBytes, StandardCharsets.US_ASCII)
        if (magic != expectedMagic)
            throw IllegalArgumentException("Invalid encrypted backup format")

        val salt = ByteArray(SALT_LENGTH)
        Helper.readBuffer(raw, salt)
        val iv = ByteArray(IV_LENGTH)
        Helper.readBuffer(raw, iv)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return CipherInputStream(raw, cipher)
    }

    fun exportAccountsEncrypted(context: Context, uri: Uri, password: String): Int {
        val db = DB.getInstance(context)
        val accounts = db.account().accounts ?: emptyList()

        val jAccounts = JSONArray()
        for (account in accounts) {
            val jAccount = account.toJSON()
            val identities = db.identity().getIdentities(account.id) ?: emptyList()
            val jIdentities = JSONArray()
            for (identity in identities) {
                jIdentities.put(identity.toJSON())
            }
            jAccount.put("identities", jIdentities)
            jAccounts.put(jAccount)
        }

        val root = JSONObject().apply {
            put("version", 1)
            put("exported_at", Date().time)
            put("accounts", jAccounts)
        }

        val raw = context.contentResolver.openOutputStream(uri)
            ?: throw FileNotFoundException(uri.toString())
        raw.use { out ->
            openEncryptedOutput(out, MAGIC_ACCOUNTS, password).use { cout ->
                cout.write(root.toString(2).toByteArray(StandardCharsets.UTF_8))
                cout.flush()
            }
        }
        return accounts.size
    }

    fun importAccountsEncrypted(context: Context, uri: Uri, password: String): Int {
        val raw = context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException(uri.toString())

        val jsonText = raw.use { input ->
            openEncryptedInput(input, MAGIC_ACCOUNTS, password).use { cin ->
                val bos = ByteArrayOutputStream()
                val buf = ByteArray(8192)
                var len: Int
                while (cin.read(buf).also { len = it } != -1) {
                    bos.write(buf, 0, len)
                }
                bos.toString(StandardCharsets.UTF_8.name())
            }
        }

        val root = JSONObject(jsonText)
        val jAccounts = root.getJSONArray("accounts")
        val db = DB.getInstance(context)
        var imported = 0

        try {
            db.beginTransaction()
            for (i in 0 until jAccounts.length()) {
                val jAccount = jAccounts.getJSONObject(i)
                val account = EntityAccount.fromJSON(jAccount)
                account.id = null
                account.id = db.account().insertAccount(account)

                val outbox = EntityFolder().apply {
                    this.account = account.id
                    this.name = "OUTBOX"
                    this.type = EntityFolder.OUTBOX
                    this.synchronize = false
                    this.sync_state = null
                }
                db.folder().insertFolder(outbox)

                val jIdentities = jAccount.optJSONArray("identities")
                if (jIdentities != null) {
                    for (j in 0 until jIdentities.length()) {
                        val identity = EntityIdentity.fromJSON(jIdentities.getJSONObject(j))
                        identity.id = null
                        identity.account = account.id
                        identity.id = db.identity().insertIdentity(identity)
                    }
                }
                imported++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        ServiceSynchronize.eval(context, "import:accounts")
        return imported
    }

    fun exportMailEncrypted(context: Context, uri: Uri, password: String, folderId: Long? = null, limit: Int = 500): Int {
        val db = DB.getInstance(context)
        val messages = db.message().getMessagesForExport(folderId, limit) ?: emptyList()

        val raw = context.contentResolver.openOutputStream(uri)
            ?: throw FileNotFoundException(uri.toString())

        var exported = 0
        raw.use { out ->
            openEncryptedOutput(out, MAGIC_MAIL, password).use { cout ->
                ZipOutputStream(cout).use { zos ->
                    val manifest = JSONArray()
                    for (msg in messages) {
                        val msgId = msg.id ?: continue
                        val file = EntityMessage.getFile(context, msgId)
                        if (!file.exists()) continue

                        val entryName = "msg_$msgId.eml"
                        zos.putNextEntry(ZipEntry(entryName))
                        FileInputStream(file).use { fis ->
                            val buf = ByteArray(8192)
                            var len: Int
                            while (fis.read(buf).also { len = it } != -1) {
                                zos.write(buf, 0, len)
                            }
                        }
                        zos.closeEntry()

                        manifest.put(JSONObject().apply {
                            put("entry", entryName)
                            put("subject", msg.subject ?: "")
                            put("preview", msg.preview ?: "")
                            put("received", msg.received)
                            put("sent", msg.sent ?: msg.received)
                            put("seen", msg.ui_seen)
                            put("flagged", msg.ui_flagged)
                            put("msgid", msg.msgid ?: "")
                        })
                        exported++
                    }

                    zos.putNextEntry(ZipEntry("manifest.json"))
                    zos.write(manifest.toString(2).toByteArray(StandardCharsets.UTF_8))
                    zos.closeEntry()
                }
            }
        }
        return exported
    }

    fun importMailEncrypted(context: Context, uri: Uri, password: String): Int {
        val db = DB.getInstance(context)
        val accounts = db.account().getSynchronizingAccounts(null)
        val targetAccount = accounts?.firstOrNull()
            ?: db.account().accounts?.firstOrNull()
            ?: throw IllegalStateException(context.getString(R.string.title_no_account))
        val accountId = targetAccount.id ?: throw IllegalStateException("Invalid account")

        var targetFolder = db.folder().getFolderByType(accountId, EntityFolder.INBOX)
        if (targetFolder == null) {
            targetFolder = EntityFolder().apply {
                this.account = accountId
                this.name = "INBOX"
                this.type = EntityFolder.INBOX
                this.synchronize = true
                this.unified = true
                this.id = db.folder().insertFolder(this)
            }
        }
        val folderId = targetFolder.id ?: throw IllegalStateException("Invalid folder")

        val raw = context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException(uri.toString())

        val emlFiles = mutableMapOf<String, ByteArray>()
        var manifestJson = "[]"

        raw.use { input ->
            openEncryptedInput(input, MAGIC_MAIL, password).use { cin ->
                ZipInputStream(cin).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val bos = ByteArrayOutputStream()
                        val buf = ByteArray(8192)
                        var len: Int
                        while (zis.read(buf).also { len = it } != -1) {
                            bos.write(buf, 0, len)
                        }
                        if (entry.name == "manifest.json") {
                            manifestJson = bos.toString(StandardCharsets.UTF_8.name())
                        } else if (entry.name.endsWith(".eml")) {
                            emlFiles[entry.name] = bos.toByteArray()
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            }
        }

        val manifest = JSONArray(manifestJson)
        var imported = 0
        try {
            db.beginTransaction()
            for (i in 0 until manifest.length()) {
                val item = manifest.getJSONObject(i)
                val entryName = item.getString("entry")
                val bytes = emlFiles[entryName] ?: continue

                val msg = EntityMessage().apply {
                    this.account = accountId
                    this.folder = folderId
                    this.msgid = item.optString("msgid").ifEmpty { null }
                    this.subject = item.optString("subject").ifEmpty { null }
                    this.preview = item.optString("preview").ifEmpty { null }
                    this.received = item.optLong("received", Date().time)
                    this.sent = item.optLong("sent", this.received)
                    this.stored = Date().time
                    this.seen = item.optBoolean("seen", true)
                    this.ui_seen = this.seen
                    this.flagged = item.optBoolean("flagged", false)
                    this.ui_flagged = this.flagged
                    this.content = true
                    this.size = bytes.size.toLong()
                    this.total = bytes.size.toLong()
                }
                val newId = db.message().insertMessage(msg)
                val outFile = EntityMessage.getFile(context, newId)
                FileOutputStream(outFile).use { fos -> fos.write(bytes) }
                imported++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return imported
    }
}
