package eu.faircode.email

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.ExpressiveMotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.mail.internet.InternetAddress

internal sealed interface ComposeTarget {
    data object New : ComposeTarget
    data class Reply(val message: TupleMessageEx) : ComposeTarget
    data class Forward(val message: TupleMessageEx) : ComposeTarget
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComposeEmailScreen(
    db: DB,
    target: ComposeTarget,
    onBack: () -> Unit,
    onCompleted: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val identities = observeLiveData(
        liveData = remember { db.identity().liveIdentities() },
        initial = emptyList()
    ).filter { it.synchronize }

    val refMessage = when (target) {
        is ComposeTarget.New -> null
        is ComposeTarget.Reply -> target.message
        is ComposeTarget.Forward -> target.message
    }

    var selectedIdentityId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(identities, refMessage) {
        if (selectedIdentityId == null && identities.isNotEmpty()) {
            val preferred = refMessage?.account?.let { acc ->
                identities.firstOrNull { it.account == acc && it.primary }
                    ?: identities.firstOrNull { it.account == acc }
            }
            selectedIdentityId = (preferred ?: identities.firstOrNull { it.primary } ?: identities.first()).id
        }
    }

    var toText by remember(target) {
        mutableStateOf(
            when (target) {
                is ComposeTarget.Reply -> {
                    val replyAddr = target.message.reply ?: target.message.from
                    MessageHelper.formatAddressesCompose(replyAddr) ?: ""
                }
                else -> ""
            }
        )
    }
    var showCcBcc by remember { mutableStateOf(false) }
    var ccText by remember(target) { mutableStateOf("") }
    var bccText by remember(target) { mutableStateOf("") }

    var subjectText by remember(target) {
        mutableStateOf(
            when (target) {
                is ComposeTarget.Reply -> {
                    val orig = target.message.subject.orEmpty()
                    if (orig.startsWith("Re:", ignoreCase = true)) orig else "Re: $orig"
                }
                is ComposeTarget.Forward -> {
                    val orig = target.message.subject.orEmpty()
                    if (orig.startsWith("Fwd:", ignoreCase = true)) orig else "Fwd: $orig"
                }
                ComposeTarget.New -> ""
            }
        )
    }

    var bodyText by remember(target) {
        mutableStateOf(
            when (target) {
                is ComposeTarget.Reply -> {
                    val who = MessageHelper.formatAddressesShort(target.message.from)
                    val preview = target.message.preview.orEmpty()
                    if (preview.isNotBlank()) "\n\n--- On ${who} wrote ---\n$preview" else ""
                }
                is ComposeTarget.Forward -> {
                    val who = MessageHelper.formatAddressesShort(target.message.from)
                    val subj = target.message.subject.orEmpty()
                    val preview = target.message.preview.orEmpty()
                    "\n\n--- Forwarded message ---\nFrom: $who\nSubject: $subj\n\n$preview"
                }
                ComposeTarget.New -> ""
            }
        )
    }

    var isSending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val title = when (target) {
        is ComposeTarget.New -> "Compose"
        is ComposeTarget.Reply -> "Reply"
        is ComposeTarget.Forward -> "Forward"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSending) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_close_24),
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        enabled = !isSending && identities.isNotEmpty(),
                        onClick = {
                            val identity = identities.firstOrNull { it.id == selectedIdentityId }
                                ?: identities.firstOrNull()
                            if (identity == null) {
                                errorMessage = "No active Gmail account found."
                                return@IconButton
                            }
                            scope.launch {
                                isSending = true
                                errorMessage = null
                                try {
                                    saveOrSendComposeMessage(
                                        context = context,
                                        db = db,
                                        identity = identity,
                                        refMessage = refMessage,
                                        toText = toText,
                                        ccText = ccText,
                                        bccText = bccText,
                                        subjectText = subjectText,
                                        bodyText = bodyText,
                                        sendNow = false
                                    )
                                    onCompleted("Draft saved")
                                } catch (ex: Throwable) {
                                    Log.e(ex)
                                    errorMessage = Log.formatThrowable(ex, false)
                                } finally {
                                    isSending = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_save_alt_24),
                            contentDescription = "Save draft"
                        )
                    }

                    Button(
                        enabled = !isSending && identities.isNotEmpty(),
                        shape = CircleShape,
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            val identity = identities.firstOrNull { it.id == selectedIdentityId }
                                ?: identities.firstOrNull()
                            if (identity == null) {
                                errorMessage = "No active Gmail account found."
                                return@Button
                            }
                            scope.launch {
                                isSending = true
                                errorMessage = null
                                try {
                                    saveOrSendComposeMessage(
                                        context = context,
                                        db = db,
                                        identity = identity,
                                        refMessage = refMessage,
                                        toText = toText,
                                        ccText = ccText,
                                        bccText = bccText,
                                        subjectText = subjectText,
                                        bodyText = bodyText,
                                        sendNow = true
                                    )
                                    onCompleted(context.getString(R.string.title_queued))
                                } catch (ex: Throwable) {
                                    Log.e(ex)
                                    errorMessage = Log.formatThrowable(ex, false)
                                } finally {
                                    isSending = false
                                }
                            }
                        }
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.twotone_send_24),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text("Send")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (identities.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Add a Gmail account first before composing or sending mail.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "From:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    identities.forEach { identity ->
                        val selected = identity.id == selectedIdentityId
                        FilterChip(
                            selected = selected,
                            onClick = { selectedIdentityId = identity.id },
                            shape = RoundedCornerShape(if (selected) 14.dp else 20.dp),
                            label = {
                                Text(
                                    text = identity.email ?: identity.name ?: "Gmail",
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = toText,
                    onValueChange = { toText = it },
                    label = { Text("To") },
                    placeholder = { Text("recipient@gmail.com") },
                    singleLine = true,
                    enabled = !isSending,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { showCcBcc = !showCcBcc },
                    enabled = !isSending
                ) {
                    Text(if (showCcBcc) "Hide" else "Cc/Bcc")
                }
            }

            AnimatedVisibility(
                visible = showCcBcc,
                enter = fadeIn(ExpressiveMotion.effectsDefault()) +
                    expandVertically(ExpressiveMotion.spatialFast()),
                exit = fadeOut(ExpressiveMotion.effectsDefault()) +
                    shrinkVertically(ExpressiveMotion.spatialFast())
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = ccText,
                        onValueChange = { ccText = it },
                        label = { Text("Cc") },
                        singleLine = true,
                        enabled = !isSending,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bccText,
                        onValueChange = { bccText = it },
                        label = { Text("Bcc") },
                        singleLine = true,
                        enabled = !isSending,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = subjectText,
                onValueChange = { subjectText = it },
                label = { Text("Subject") },
                singleLine = true,
                enabled = !isSending,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(ExpressiveMotion.effectsDefault()) +
                    expandVertically(ExpressiveMotion.spatialFast()),
                exit = fadeOut(ExpressiveMotion.effectsDefault()) +
                    shrinkVertically(ExpressiveMotion.spatialFast())
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            OutlinedTextField(
                value = bodyText,
                onValueChange = { bodyText = it },
                label = { Text("Message") },
                enabled = !isSending,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
            )
        }
    }
}

private suspend fun saveOrSendComposeMessage(
    context: Context,
    db: DB,
    identity: TupleIdentityEx,
    refMessage: TupleMessageEx?,
    toText: String,
    ccText: String,
    bccText: String,
    subjectText: String,
    bodyText: String,
    sendNow: Boolean
) = withContext(Dispatchers.IO) {
    val ato = if (toText.isBlank()) emptyArray() else MessageHelper.dedup(MessageHelper.parseAddresses(context, toText))
    val acc = if (ccText.isBlank()) emptyArray() else MessageHelper.dedup(MessageHelper.parseAddresses(context, ccText))
    val abcc = if (bccText.isBlank()) emptyArray() else MessageHelper.dedup(MessageHelper.parseAddresses(context, bccText))

    if (sendNow && ato.isEmpty() && acc.isEmpty() && abcc.isEmpty()) {
        throw IllegalArgumentException("Please enter at least one recipient")
    }

    val html = "<div>" + TextUtils.htmlEncode(bodyText).replace("\n", "<br>") + "</div>"
    val fullText = HtmlHelper.getFullText(context, html)
    val preview = HtmlHelper.getPreview(fullText)
    val language = HtmlHelper.getLanguage(context, subjectText, fullText)
    val now = Date().time

    val msg = EntityMessage().apply {
        this.account = identity.account
        this.identity = identity.id
        this.from = arrayOf(InternetAddress(identity.email, identity.name, StandardCharsets.UTF_8.name()))
        this.to = ato
        this.cc = acc
        this.bcc = abcc
        this.subject = subjectText
        this.sender = MessageHelper.getSortKey(this.from)
        val lookupUri: Uri? = ContactInfo.getLookupUri(this.from)
        this.avatar = lookupUri?.toString()
        this.msgid = EntityMessage.generateMessageId()
        this.inreplyto = refMessage?.msgid
        this.thread = refMessage?.thread ?: this.msgid
        this.received = now
        this.seen = true
        this.ui_seen = true
        this.content = true
        this.language = language
        this.preview = preview
        this.revision = 1
        this.revisions = 1
    }

    db.beginTransaction()
    try {
        val targetFolder = if (sendNow) {
            db.folder().getOutbox()
                ?: throw IllegalStateException("Outbox folder not found")
        } else {
            db.folder().getFolderByType(identity.account, EntityFolder.DRAFTS)
                ?: throw IllegalArgumentException(context.getString(R.string.title_no_drafts))
        }

        msg.folder = targetFolder.id
        msg.id = db.message().insertMessage(msg)
        Helper.writeText(msg.getFile(context), html)
        if (!sendNow) {
            Helper.writeText(msg.getFile(context, msg.revision), html)
        }

        if (sendNow) {
            EntityOperation.queue(context, msg, EntityOperation.SEND)
        }
        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }

    ServiceSynchronize.eval(context, if (sendNow) "compose/send" else "compose/draft")
    if (sendNow) {
        ServiceSend.start(context)
    }
}
