package eu.faircode.email

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Base64
import androidx.preference.PreferenceManager
import eu.faircode.email.DB
import eu.faircode.email.EmailProvider
import eu.faircode.email.EmailService
import eu.faircode.email.EntityAccount
import eu.faircode.email.EntityFolder
import eu.faircode.email.EntityIdentity
import eu.faircode.email.EntityLog
import eu.faircode.email.FairEmailBackupAgent
import eu.faircode.email.FragmentDialogSwipes
import eu.faircode.email.GmailState
import eu.faircode.email.Helper
import eu.faircode.email.Log
import eu.faircode.email.R
import eu.faircode.email.ServiceAuthenticator
import eu.faircode.email.ServiceSynchronize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.openid.appauth.AppAuthConfiguration
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationManagementActivity
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretPost
import net.openid.appauth.GrantTypeValues
import net.openid.appauth.NoClientAuthentication
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import net.openid.appauth.browser.Browsers
import net.openid.appauth.browser.VersionRange
import net.openid.appauth.browser.VersionedBrowserMatcher
import org.json.JSONObject
import java.security.SecureRandom
import java.util.Date
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal object GmailAuthHelper {
    fun formatError(ex: Throwable): String = Log.formatThrowable(ex, false)
    private const val OAUTH_TIMEOUT = 10 * 60 * 1000L
    private const val GET_TOKEN_TIMEOUT = 20 * 1000L
    private const val FAIREMAIL_RANDOM = "fairemail_random"
    private const val FAIREMAIL_EXPIRE = "fairemail_expire"

    private val SBROWSER = VersionedBrowserMatcher(
        Browsers.SBrowser.PACKAGE_NAME,
        Browsers.SBrowser.SIGNATURE_SET,
        false,
        VersionRange.ANY_VERSION
    )
    private val SBROWSER_TAB = VersionedBrowserMatcher(
        Browsers.SBrowser.PACKAGE_NAME,
        Browsers.SBrowser.SIGNATURE_SET,
        true,
        VersionRange.ANY_VERSION
    )

    fun Context.findActivity(): Activity? =
        generateSequence(this) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>()
            .firstOrNull()

    private fun ensureInternet(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network = cm?.activeNetwork
        val caps = if (network != null) cm.getNetworkCapabilities(network) else null
        if (caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            throw IllegalArgumentException(context.getString(R.string.title_no_internet))
        }
    }

    fun buildGmailOAuthIntent(context: Context, emailHint: String): Intent {
        val provider = EmailProvider.getProvider(context, "gmail")
        val oauth = provider.oauth ?: throw IllegalStateException("Gmail OAuth config missing")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val oauthTabs = prefs.getBoolean("oauth_tabs", false)

        val appAuthConfig = AppAuthConfiguration.Builder()
            .setBrowserMatcher { descriptor ->
                val accept = !(SBROWSER.matches(descriptor) || SBROWSER_TAB.matches(descriptor))
                if (descriptor.useCustomTab && !oauthTabs) false else accept
            }
            .build()

        val authService = try {
            AuthorizationService(context, appAuthConfig)
        } catch (ex: Throwable) {
            Log.e(ex)
            AuthorizationService(
                context,
                AppAuthConfiguration.Builder()
                    .setBrowserMatcher { descriptor ->
                        !(SBROWSER.matches(descriptor) || SBROWSER_TAB.matches(descriptor)) && !descriptor.useCustomTab
                    }
                    .build()
            )
        }

        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(oauth.authorizationEndpoint),
            Uri.parse(oauth.tokenEndpoint)
        )

        val random = kotlin.math.abs(SecureRandom().nextInt())
        val expire = Date().time + OAUTH_TIMEOUT
        val authState = AuthState(serviceConfig)
        val jAuthState = authState.jsonSerialize().apply {
            put(FAIREMAIL_RANDOM, random)
            put(FAIREMAIL_EXPIRE, expire)
        }
        prefs.edit().putString("oauth.${provider.id}", jAuthState.toString()).apply()

        val params = oauth.parameters ?: LinkedHashMap()
        val builder = AuthorizationRequest.Builder(
            serviceConfig,
            oauth.clientId,
            ResponseTypeValues.CODE,
            Uri.parse(oauth.redirectUri)
        )
            .setScopes(*oauth.scopes)
            .setState("${provider.id}:$random:")
            .setAdditionalParameters(params)

        val trimmedHint = emailHint.trim()
        if (trimmedHint.isNotEmpty()) {
            builder.setLoginHint(trimmedHint)
        }
        if (!TextUtils.isEmpty(oauth.prompt)) {
            builder.setPrompt(oauth.prompt)
        }

        val authRequest = builder.build()
        EntityLog.log(context, "OAuth request provider=${provider.id}")
        return try {
            authService.getAuthorizationRequestIntent(authRequest)
        } catch (ex: ActivityNotFoundException) {
            Log.w(ex)
            AuthorizationManagementActivity.createStartForResultIntent(
                context,
                authRequest,
                Intent(Intent.ACTION_VIEW, authRequest.toUri())
            )
        }
    }

    suspend fun completeGmailOAuth(
        context: Context,
        data: Intent,
        displayName: String,
        emailHint: String,
        updateExisting: Boolean
    ): String {
        val auth = AuthorizationResponse.fromIntent(data)
            ?: throw (AuthorizationException.fromIntent(data)
                ?: IllegalArgumentException("No OAuth response data"))

        val stateParts = (auth.state ?: "").split(":")
        val providerId = stateParts.getOrNull(0).orEmpty().ifEmpty { "gmail" }
        val returnedRandom = stateParts.getOrNull(1)?.toIntOrNull()
            ?: throw SecurityException("Invalid OAuth state")

        val provider = EmailProvider.getProvider(context, providerId)
        val oauth = provider.oauth ?: throw IllegalStateException("Gmail OAuth config missing")
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val json = prefs.getString("oauth.${provider.id}", null)
            ?: throw SecurityException("Missing saved OAuth state")
        val jAuthState = JSONObject(json)
        val random = jAuthState.optInt(FAIREMAIL_RANDOM, -1)
        val expire = jAuthState.optLong(FAIREMAIL_EXPIRE, -1L)
        jAuthState.remove(FAIREMAIL_RANDOM)
        val now = Date().time
        if (random != returnedRandom) {
            throw SecurityException("OAuth state mismatch")
        }
        if (expire < now) {
            throw SecurityException("OAuth session expired")
        }

        val authState = AuthState.jsonDeserialize(jAuthState).apply {
            update(auth, null)
        }

        val tokenResponse = performTokenRequest(context, auth, oauth)
        if (TextUtils.isEmpty(tokenResponse.refreshToken)) {
            throw IllegalStateException("No refresh token returned by Google")
        }
        authState.update(tokenResponse, null)

        val resolvedEmail = extractEmailFromJwt(tokenResponse.idToken)
            ?: emailHint.trim().takeIf { Helper.EMAIL_ADDRESS.matcher(it).matches() }
            ?: throw IllegalArgumentException(context.getString(R.string.title_no_account))

        return withContext(Dispatchers.IO) {
            ensureInternet(context)

            val serializedState = authState.jsonSerializeString()
            val inbound = provider.imap
            val aProtocol = if (inbound.starttls) "imap" else "imaps"
            val aEncryption = if (inbound.starttls) EmailService.ENCRYPTION_STARTTLS else EmailService.ENCRYPTION_SSL
            val folders = EmailService(
                context, aProtocol, null, aEncryption,
                false, false, false, EmailService.PURPOSE_CHECK, true
            ).use { service ->
                service.connect(
                    false, inbound.host, inbound.port,
                    ServiceAuthenticator.AUTH_TYPE_OAUTH, provider.id,
                    resolvedEmail, serializedState, null, null
                )
                service.getFolders(inbound.host)
            }

            val iProtocol = if (provider.smtp.starttls) "smtp" else "smtps"
            val iEncryption = if (provider.smtp.starttls) EmailService.ENCRYPTION_STARTTLS else EmailService.ENCRYPTION_SSL
            val maxSize = EmailService(
                context, iProtocol, null, iEncryption,
                false, false, false, EmailService.PURPOSE_CHECK, true
            ).use { service ->
                service.connect(
                    false, provider.smtp.host, provider.smtp.port,
                    ServiceAuthenticator.AUTH_TYPE_OAUTH, provider.id,
                    resolvedEmail, serializedState, null, null
                )
                service.maxSize
            }

            saveGmailAccount(
                context = context,
                provider = provider,
                authType = ServiceAuthenticator.AUTH_TYPE_OAUTH,
                providerId = provider.id,
                userEmail = resolvedEmail,
                secret = serializedState,
                displayName = displayName,
                folders = folders,
                maxSize = maxSize,
                updateExisting = updateExisting
            )
            ServiceSynchronize.eval(context, "OAuth")
            FairEmailBackupAgent.dataChanged(context)
            resolvedEmail
        }
    }

    fun buildAndroidAccountPickerIntent(): Intent =
        AccountManager.newChooseAccountIntent(
            null,
            null,
            arrayOf(GmailState.TYPE_GOOGLE),
            false,
            null,
            null,
            null,
            null
        )

    suspend fun completeAndroidGmailAccount(
        context: Context,
        activity: Activity?,
        data: Intent,
        displayName: String,
        updateExisting: Boolean
    ): String {
        val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        val accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        if (accountName.isNullOrBlank() || accountType.isNullOrBlank()) {
            throw IllegalArgumentException(context.getString(R.string.title_no_account))
        }

        val am = AccountManager.get(context.applicationContext)
        val account = am.getAccountsByType(accountType)
            .firstOrNull { accountName.equals(it.name, ignoreCase = true) }
            ?: throw IllegalArgumentException(context.getString(R.string.title_no_account))

        val token = fetchAndroidAuthToken(context, am, account, accountType, activity)

        return withContext(Dispatchers.IO) {
            ensureInternet(context)
            if (!Helper.EMAIL_ADDRESS.matcher(accountName).matches()) {
                throw IllegalArgumentException(context.getString(R.string.title_email_invalid, accountName))
            }

            val provider = EmailProvider.fromDomain(context, "gmail.com", EmailProvider.Discover.ALL)[0]
            val inbound = provider.imap
            val aProtocol = if (inbound.starttls) "imap" else "imaps"
            val aEncryption = if (inbound.starttls) EmailService.ENCRYPTION_STARTTLS else EmailService.ENCRYPTION_SSL
            val folders = EmailService(
                context, aProtocol, null, aEncryption,
                false, false, false, EmailService.PURPOSE_CHECK, true
            ).use { service ->
                service.connect(
                    false, inbound.host, inbound.port,
                    ServiceAuthenticator.AUTH_TYPE_GMAIL, null,
                    accountName, token, null, null
                )
                service.getFolders(inbound.host)
            }

            val iProtocol = if (provider.smtp.starttls) "smtp" else "smtps"
            val iEncryption = if (provider.smtp.starttls) EmailService.ENCRYPTION_STARTTLS else EmailService.ENCRYPTION_SSL
            val maxSize = EmailService(
                context, iProtocol, null, iEncryption,
                false, false, false, EmailService.PURPOSE_CHECK, true
            ).use { service ->
                service.connect(
                    false, provider.smtp.host, provider.smtp.port,
                    ServiceAuthenticator.AUTH_TYPE_GMAIL, null,
                    accountName, token, null, null
                )
                service.maxSize
            }

            saveGmailAccount(
                context = context,
                provider = provider,
                authType = ServiceAuthenticator.AUTH_TYPE_GMAIL,
                providerId = null,
                userEmail = accountName,
                secret = token,
                displayName = displayName,
                folders = folders,
                maxSize = maxSize,
                updateExisting = updateExisting
            )
            ServiceSynchronize.eval(context, "Gmail")
            FairEmailBackupAgent.dataChanged(context)
            accountName
        }
    }

    private suspend fun performTokenRequest(
        context: Context,
        auth: AuthorizationResponse,
        oauth: EmailProvider.OAuth
    ): TokenResponse = suspendCancellableCoroutine { cont ->
        val authService = AuthorizationService(context)
        val clientAuth = if (oauth.clientSecret == null) {
            NoClientAuthentication.INSTANCE
        } else {
            ClientSecretPost(oauth.clientSecret)
        }
        val builder = TokenRequest.Builder(auth.request.configuration, auth.request.clientId)
            .setGrantType(GrantTypeValues.AUTHORIZATION_CODE)
            .setRedirectUri(auth.request.redirectUri)
            .setCodeVerifier(auth.request.codeVerifier)
            .setAuthorizationCode(auth.authorizationCode)
            .setAdditionalParameters(emptyMap())
            .setNonce(auth.request.nonce)
        if (oauth.tokenScopes) {
            builder.setScope(TextUtils.join(" ", oauth.scopes))
        }
        authService.performTokenRequest(builder.build(), clientAuth) { access, error ->
            if (!cont.isActive) return@performTokenRequest
            if (access != null) {
                cont.resume(access)
            } else {
                cont.resumeWithException(error ?: IllegalStateException("Token request failed"))
            }
        }
    }

    private suspend fun fetchAndroidAuthToken(
        context: Context,
        am: AccountManager,
        account: Account,
        accountType: String,
        activity: Activity?
    ): String = suspendCancellableCoroutine { cont ->
        val disabledMsg = context.getString(R.string.title_setup_advanced_protection)
        val handler = Handler(Looper.getMainLooper())
        am.getAuthToken(
            account,
            ServiceAuthenticator.getAuthTokenType(accountType),
            Bundle(),
            activity,
            { future ->
                if (!cont.isActive) return@getAuthToken
                try {
                    val bundle = future.getResult(GET_TOKEN_TIMEOUT, TimeUnit.MILLISECONDS)
                    if (future.isCancelled) {
                        cont.resumeWithException(IllegalArgumentException("Android failed to return a token"))
                        return@getAuthToken
                    }
                    val token = bundle.getString(AccountManager.KEY_AUTHTOKEN)
                    if (token.isNullOrEmpty()) {
                        cont.resumeWithException(IllegalArgumentException("Android returned no token"))
                        return@getAuthToken
                    }
                    cont.resume(token)
                } catch (ex: Throwable) {
                    val mapped = if (ex is AuthenticatorException &&
                        ("ERROR" == ex.message || "ServiceDisabled" == ex.message)
                    ) {
                        IllegalArgumentException(disabledMsg, ex)
                    } else {
                        ex
                    }
                    cont.resumeWithException(mapped)
                }
            },
            handler
        )
    }

    private fun extractEmailFromJwt(jwt: String?): String? {
        if (jwt.isNullOrBlank()) return null
        val segments = jwt.split(".")
        if (segments.size < 2) return null
        return try {
            val payload = String(Base64.decode(segments[1], Base64.DEFAULT))
            val jPayload = JSONObject(payload)
            listOf("email", "preferred_username", "unique_name")
                .firstNotNullOfOrNull { key ->
                    jPayload.optString(key).takeIf { it.isNotBlank() && Helper.EMAIL_ADDRESS.matcher(it).matches() }
                }
        } catch (ex: Throwable) {
            Log.e(ex)
            null
        }
    }

    private fun saveGmailAccount(
        context: Context,
        provider: EmailProvider,
        authType: Int,
        providerId: String?,
        userEmail: String,
        secret: String,
        displayName: String,
        folders: List<EntityFolder>,
        maxSize: Long?,
        updateExisting: Boolean
    ) {
        val db = DB.getInstance(context)
        val protocol = EntityAccount.TYPE_IMAP
        val inbound = provider.imap
        val aEncryption = if (inbound.starttls) EmailService.ENCRYPTION_STARTTLS else EmailService.ENCRYPTION_SSL
        val iEncryption = if (provider.smtp.starttls) EmailService.ENCRYPTION_STARTTLS else EmailService.ENCRYPTION_SSL

        db.beginTransaction()
        try {
            val update = if (updateExisting) {
                db.account().getAccounts(userEmail, protocol)?.singleOrNull()
            } else {
                null
            }

            if (update == null) {
                val primary = db.account().getPrimaryAccount()
                val username = userEmail.substringBefore('@')
                val account = EntityAccount().apply {
                    this.protocol = protocol
                    this.host = inbound.host
                    this.encryption = aEncryption
                    this.port = inbound.port
                    this.auth_type = authType
                    this.provider = providerId
                    this.user = userEmail
                    this.password = secret
                    this.name = if (TextUtils.isEmpty(provider.name)) userEmail else "${provider.name}/$username"
                    this.synchronize = true
                    this.primary = (primary == null)
                    if (provider.keepalive > 0) this.poll_interval = provider.keepalive
                    this.keep_alive_noop = provider.noop
                    this.partial_fetch = provider.partial
                    this.raw_fetch = provider.raw
                    this.created = Date().time
                    this.last_connected = this.created
                }
                account.id = db.account().insertAccount(account)

                for (folder in folders) {
                    val existing = db.folder().getFolderByName(account.id, folder.name)
                    if (existing == null) {
                        folder.account = account.id
                        folder.setSpecials(account)
                        folder.id = db.folder().insertFolder(folder)
                        if (folder.synchronize) {
                            eu.faircode.email.EntityOperation.sync(context, folder.id, true)
                        }
                    }
                }

                FragmentDialogSwipes.setDefaultFolderActions(context, account)
                db.account().updateAccount(account)

                val resolvedName = displayName.trim().ifEmpty { username }
                val identity = EntityIdentity().apply {
                    this.name = resolvedName
                    this.email = userEmail
                    this.account = account.id
                    this.host = provider.smtp.host
                    this.encryption = iEncryption
                    this.port = provider.smtp.port
                    this.auth_type = authType
                    this.provider = providerId
                    this.user = userEmail
                    this.password = secret
                    this.use_ip = provider.useip
                    this.synchronize = true
                    this.primary = true
                    this.max_size = maxSize
                }
                db.identity().insertIdentity(identity)
            } else {
                db.account().setAccountSynchronize(update.id, true)
                db.account().setAccountPassword(update.id, secret, authType, providerId)
                db.identity().setIdentityPassword(update.id, update.user, secret, update.auth_type, authType, providerId)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("has_accounts", true)
            .apply()
    }
}
