package eu.faircode.email

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.text.format.DateUtils
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import eu.faircode.email.ui.SingleInputDialogContent
import eu.faircode.email.ui.theme.FairEmailTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MainTab(val label: String, val iconRes: Int) {
    Inbox("Inbox", R.drawable.twotone_inbox_24),
    Folders("Folders", R.drawable.twotone_folder_24),
    Search("Search", R.drawable.twotone_search_24),
    Accounts("Accounts", R.drawable.twotone_account_circle_24),
    Settings("Settings", R.drawable.twotone_settings_24)
}

private enum class BackupAction {
    EXPORT_ACCOUNTS,
    IMPORT_ACCOUNTS,
    EXPORT_MAIL,
    IMPORT_MAIL
}

@Composable
private fun <T> observeLiveData(liveData: LiveData<T>, initial: T): T {
    var state by remember(liveData) { mutableStateOf(liveData.value ?: initial) }
    DisposableEffect(liveData) {
        val observer = Observer<T> { value -> state = value }
        liveData.observeForever(observer)
        onDispose { liveData.removeObserver(observer) }
    }
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainComposeApp() {
    val context = LocalContext.current
    val db = remember(context) { DB.getInstance(context) }
    val prefs = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(MainTab.Inbox) }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var selectedFolderTitle by remember { mutableStateOf<String?>(null) }
    var openedMessage by remember { mutableStateOf<TupleMessageEx?>(null) }

    // Encrypted backup workflow state
    var pendingBackupAction by remember { mutableStateOf<BackupAction?>(null) }
    var pendingBackupUri by remember { mutableStateOf<Uri?>(null) }
    var passwordInput by remember { mutableStateOf("") }

    val exportAccountsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            pendingBackupUri = uri
            pendingBackupAction = BackupAction.EXPORT_ACCOUNTS
            passwordInput = ""
        }
    }

    val importAccountsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingBackupUri = uri
            pendingBackupAction = BackupAction.IMPORT_ACCOUNTS
            passwordInput = ""
        }
    }

    val exportMailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            pendingBackupUri = uri
            pendingBackupAction = BackupAction.EXPORT_MAIL
            passwordInput = ""
        }
    }

    val importMailLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingBackupUri = uri
            pendingBackupAction = BackupAction.IMPORT_MAIL
            passwordInput = ""
        }
    }

    if (openedMessage != null) {
        MessageDetailScreen(
            message = openedMessage!!,
            onBack = { openedMessage = null },
            onReply = { msg ->
                context.startActivity(
                    Intent(context, ActivityCompose::class.java)
                        .putExtra("action", "reply")
                        .putExtra("id", msg.id)
                )
            },
            onForward = { msg ->
                context.startActivity(
                    Intent(context, ActivityCompose::class.java)
                        .putExtra("action", "forward")
                        .putExtra("id", msg.id)
                )
            },
            onToggleStar = { msg ->
                scope.launch(Dispatchers.IO) {
                    EntityOperation.queue(context, msg, EntityOperation.FLAG, !msg.ui_flagged)
                    ServiceSynchronize.eval(context, "flag")
                }
            },
            onDelete = { msg ->
                scope.launch(Dispatchers.IO) {
                    EntityOperation.queue(context, msg, EntityOperation.DELETE)
                    ServiceSynchronize.eval(context, "delete")
                }
                openedMessage = null
            }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentTab == MainTab.Inbox && selectedFolderTitle != null)
                            selectedFolderTitle!! else currentTab.label
                    )
                },
                actions = {
                    if (currentTab == MainTab.Inbox && selectedFolderId != null) {
                        TextButton(onClick = {
                            selectedFolderId = null
                            selectedFolderTitle = null
                        }) {
                            Text("All Inbox")
                        }
                    }
                    if (currentTab == MainTab.Inbox || currentTab == MainTab.Folders) {
                        IconButton(onClick = { ServiceUI.sync(context, null) }) {
                            Icon(
                                painter = painterResource(R.drawable.twotone_sync_24),
                                contentDescription = "Sync"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTab == MainTab.Inbox || currentTab == MainTab.Folders) {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(
                            Intent(context, ActivityCompose::class.java)
                                .putExtra("action", "new")
                        )
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.twotone_edit_24),
                        contentDescription = stringResource(R.string.title_compose)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconRes),
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.Inbox -> InboxScreen(
                    db = db,
                    folderId = selectedFolderId,
                    onMessageClick = { msg ->
                        openedMessage = msg
                        if (!msg.ui_seen) {
                            scope.launch(Dispatchers.IO) {
                                EntityOperation.queue(context, msg, EntityOperation.SEEN, true)
                                ServiceSynchronize.eval(context, "seen")
                            }
                        }
                    },
                    onToggleStar = { msg ->
                        scope.launch(Dispatchers.IO) {
                            EntityOperation.queue(context, msg, EntityOperation.FLAG, !msg.ui_flagged)
                            ServiceSynchronize.eval(context, "flag")
                        }
                    }
                )

                MainTab.Folders -> FoldersScreen(
                    db = db,
                    onFolderClick = { folder ->
                        selectedFolderId = folder.id
                        selectedFolderTitle = folder.getDisplayName(context)
                        currentTab = MainTab.Inbox
                    }
                )

                MainTab.Search -> SearchScreen(
                    db = db,
                    onMessageClick = { msg -> openedMessage = msg },
                    onToggleStar = { msg ->
                        scope.launch(Dispatchers.IO) {
                            EntityOperation.queue(context, msg, EntityOperation.FLAG, !msg.ui_flagged)
                            ServiceSynchronize.eval(context, "flag")
                        }
                    }
                )

                MainTab.Accounts -> AccountsScreen(
                    db = db,
                    onAddGmailOAuth = {
                        context.startActivity(
                            Intent(context, ActivitySetup::class.java)
                                .putExtra("target", "oauth")
                                .putExtra("id", "gmail")
                                .putExtra("name", "Gmail")
                                .putExtra("askAccount", true)
                        )
                    },
                    onAddGmailAndroid = {
                        context.startActivity(
                            Intent(context, ActivitySetup::class.java)
                                .putExtra("target", "gmail")
                        )
                    },
                    onExportAccounts = { exportAccountsLauncher.launch("fairemail-accounts.enc") },
                    onImportAccounts = { importAccountsLauncher.launch(arrayOf("*/*")) },
                    onExportMail = { exportMailLauncher.launch("fairemail-mail.enc") },
                    onImportMail = { importMailLauncher.launch(arrayOf("*/*")) }
                )

                MainTab.Settings -> SettingsScreen(
                    prefs = prefs,
                    onExportAccounts = { exportAccountsLauncher.launch("fairemail-accounts.enc") },
                    onImportAccounts = { importAccountsLauncher.launch(arrayOf("*/*")) },
                    onExportMail = { exportMailLauncher.launch("fairemail-mail.enc") },
                    onImportMail = { importMailLauncher.launch(arrayOf("*/*")) }
                )
            }
        }
    }

    if (pendingBackupAction != null && pendingBackupUri != null) {
        val action = pendingBackupAction!!
        val uri = pendingBackupUri!!
        val titleRes = when (action) {
            BackupAction.EXPORT_ACCOUNTS, BackupAction.EXPORT_MAIL -> R.string.title_setup_password
            BackupAction.IMPORT_ACCOUNTS, BackupAction.IMPORT_MAIL -> R.string.title_setup_password
        }

        AlertDialog(
            onDismissRequest = {
                pendingBackupAction = null
                pendingBackupUri = null
            },
            text = {
                SingleInputDialogContent(
                    titleIconRes = R.drawable.twotone_vpn_key_24,
                    titleRes = titleRes,
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    hint = "Encryption password",
                    isPassword = true,
                    keyboardType = KeyboardType.Password
                )
            },
            confirmButton = {
                TextButton(
                    enabled = passwordInput.isNotEmpty(),
                    onClick = {
                        val pwd = passwordInput
                        pendingBackupAction = null
                        pendingBackupUri = null
                        scope.launch {
                            val resultText = withContext(Dispatchers.IO) {
                                try {
                                    when (action) {
                                        BackupAction.EXPORT_ACCOUNTS -> {
                                            val count = BackupCryptoHelper.exportAccountsEncrypted(context, uri, pwd)
                                            "Exported $count encrypted account(s)"
                                        }
                                        BackupAction.IMPORT_ACCOUNTS -> {
                                            val count = BackupCryptoHelper.importAccountsEncrypted(context, uri, pwd)
                                            "Imported $count encrypted account(s)"
                                        }
                                        BackupAction.EXPORT_MAIL -> {
                                            val count = BackupCryptoHelper.exportMailEncrypted(context, uri, pwd, selectedFolderId)
                                            "Exported $count encrypted message(s)"
                                        }
                                        BackupAction.IMPORT_MAIL -> {
                                            val count = BackupCryptoHelper.importMailEncrypted(context, uri, pwd)
                                            "Imported $count encrypted message(s)"
                                        }
                                    }
                                } catch (ex: Throwable) {
                                    ex.message ?: "Operation failed"
                                }
                            }
                            snackbarHostState.showSnackbar(resultText)
                        }
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingBackupAction = null
                    pendingBackupUri = null
                }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun InboxScreen(
    db: DB,
    folderId: Long?,
    onMessageClick: (TupleMessageEx) -> Unit,
    onToggleStar: (TupleMessageEx) -> Unit
) {
    var unseenOnly by remember { mutableStateOf(false) }
    var flaggedOnly by remember { mutableStateOf(false) }
    var attachmentsOnly by remember { mutableStateOf(false) }

    val messages = observeLiveData(
        liveData = remember(folderId, unseenOnly, flaggedOnly, attachmentsOnly) {
            db.message().liveComposeMessages(folderId, unseenOnly, flaggedOnly, attachmentsOnly, null, 200)
        },
        initial = emptyList()
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !unseenOnly && !flaggedOnly && !attachmentsOnly,
                onClick = {
                    unseenOnly = false
                    flaggedOnly = false
                    attachmentsOnly = false
                },
                label = { Text("All") }
            )
            FilterChip(
                selected = unseenOnly,
                onClick = { unseenOnly = !unseenOnly },
                label = { Text("Unread") }
            )
            FilterChip(
                selected = flaggedOnly,
                onClick = { flaggedOnly = !flaggedOnly },
                label = { Text("Starred") }
            )
            FilterChip(
                selected = attachmentsOnly,
                onClick = { attachmentsOnly = !attachmentsOnly },
                label = { Text("Attachments") }
            )
        }

        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.title_no_messages),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(messages, key = { it.id ?: 0L }) { msg ->
                    MessageRowItem(
                        message = msg,
                        onClick = { onMessageClick(msg) },
                        onToggleStar = { onToggleStar(msg) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun MessageRowItem(
    message: TupleMessageEx,
    onClick: () -> Unit,
    onToggleStar: () -> Unit
) {
    val context = LocalContext.current
    val sender = remember(message.from) {
        MessageHelper.formatAddressesShort(message.from).ifEmpty { "(No sender)" }
    }
    val time = remember(message.received) {
        DateUtils.getRelativeTimeSpanString(context, message.received).toString()
    }
    val fontWeight = if (message.ui_seen) FontWeight.Normal else FontWeight.Bold

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sender,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Text(
                text = message.subject ?: "(No subject)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = fontWeight),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!message.preview.isNullOrEmpty()) {
                Text(
                    text = message.preview ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = onToggleStar) {
            Icon(
                painter = painterResource(
                    if (message.ui_flagged) R.drawable.twotone_star_24 else R.drawable.twotone_star_border_24
                ),
                contentDescription = "Star",
                tint = if (message.ui_flagged) FairEmailTheme.colors.accent else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FoldersScreen(
    db: DB,
    onFolderClick: (TupleFolderEx) -> Unit
) {
    val context = LocalContext.current
    val folders = observeLiveData(
        liveData = remember { db.folder().liveFolders(null, false) },
        initial = emptyList()
    )

    if (folders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No folders", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(folders, key = { it.id ?: 0L }) { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFolderClick(folder) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(EntityFolder.getIcon(folder.type)),
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.getDisplayName(context),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!folder.accountName.isNullOrEmpty()) {
                            Text(
                                text = folder.accountName ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (folder.unseen > 0) {
                        Badge { Text(folder.unseen.toString()) }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SearchScreen(
    db: DB,
    onMessageClick: (TupleMessageEx) -> Unit,
    onToggleStar: (TupleMessageEx) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var unseenOnly by remember { mutableStateOf(false) }
    var flaggedOnly by remember { mutableStateOf(false) }
    var attachmentsOnly by remember { mutableStateOf(false) }

    val messages = observeLiveData(
        liveData = remember(query, unseenOnly, flaggedOnly, attachmentsOnly) {
            db.message().liveComposeMessages(null, unseenOnly, flaggedOnly, attachmentsOnly, query.trim(), 100)
        },
        initial = emptyList()
    )

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text("Search Gmail messages...") },
            leadingIcon = {
                Icon(painter = painterResource(R.drawable.twotone_search_24), contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(selected = unseenOnly, onClick = { unseenOnly = !unseenOnly }, label = { Text("Unread") })
            FilterChip(selected = flaggedOnly, onClick = { flaggedOnly = !flaggedOnly }, label = { Text("Starred") })
            FilterChip(selected = attachmentsOnly, onClick = { attachmentsOnly = !attachmentsOnly }, label = { Text("Attachments") })
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages, key = { it.id ?: 0L }) { msg ->
                MessageRowItem(
                    message = msg,
                    onClick = { onMessageClick(msg) },
                    onToggleStar = { onToggleStar(msg) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AccountsScreen(
    db: DB,
    onAddGmailOAuth: () -> Unit,
    onAddGmailAndroid: () -> Unit,
    onExportAccounts: () -> Unit,
    onImportAccounts: () -> Unit,
    onExportMail: () -> Unit,
    onImportMail: () -> Unit
) {
    val accounts = observeLiveData(
        liveData = remember { db.account().liveSynchronizingAccounts() },
        initial = emptyList()
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Gmail Accounts", style = MaterialTheme.typography.titleMedium)
        if (accounts.isEmpty()) {
            Text(
                text = "No Gmail account configured yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            accounts.forEach { account ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = account.name ?: account.user ?: "Gmail",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (!account.user.isNullOrEmpty()) {
                            Text(
                                text = account.user ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onAddGmailOAuth, modifier = Modifier.weight(1f)) {
                Text("Add Gmail (OAuth)")
            }
            OutlinedButton(onClick = onAddGmailAndroid, modifier = Modifier.weight(1f)) {
                Text("Add Gmail (Android)")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(text = "Encrypted Backup & Restore", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "AES-256-GCM + PBKDF2-HMAC-SHA512 password-protected backup for Gmail accounts and mail.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onExportAccounts, modifier = Modifier.weight(1f)) {
                Text("Export Accounts")
            }
            OutlinedButton(onClick = onImportAccounts, modifier = Modifier.weight(1f)) {
                Text("Import Accounts")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onExportMail, modifier = Modifier.weight(1f)) {
                Text("Export Mail")
            }
            OutlinedButton(onClick = onImportMail, modifier = Modifier.weight(1f)) {
                Text("Import Mail")
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    prefs: SharedPreferences,
    onExportAccounts: () -> Unit,
    onImportAccounts: () -> Unit,
    onExportMail: () -> Unit,
    onImportMail: () -> Unit
) {
    var compact by remember { mutableStateOf(prefs.getBoolean("compact", false)) }
    var avatars by remember { mutableStateOf(prefs.getBoolean("avatars", true)) }
    var autoseen by remember { mutableStateOf(prefs.getBoolean("autoseen", true)) }
    var askDelete by remember { mutableStateOf(prefs.getBoolean("ask_delete", true)) }
    var syncEnabled by remember { mutableStateOf(prefs.getBoolean("enabled", true)) }
    var notify by remember { mutableStateOf(prefs.getBoolean("notify", true)) }
    var blockImages by remember { mutableStateOf(!prefs.getBoolean("images", false)) }
    var stripTracking by remember { mutableStateOf(prefs.getBoolean("strip_tracking", true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        SettingSwitchRow("Compact message list", compact) {
            compact = it
            prefs.edit().putBoolean("compact", it).apply()
        }
        SettingSwitchRow("Show sender avatars", avatars) {
            avatars = it
            prefs.edit().putBoolean("avatars", it).apply()
        }

        HorizontalDivider()
        Text("Reading & Actions", style = MaterialTheme.typography.titleMedium)
        SettingSwitchRow("Mark read on open", autoseen) {
            autoseen = it
            prefs.edit().putBoolean("autoseen", it).apply()
        }
        SettingSwitchRow("Confirm before deleting", askDelete) {
            askDelete = it
            prefs.edit().putBoolean("ask_delete", it).apply()
        }

        HorizontalDivider()
        Text("Sync & Notifications", style = MaterialTheme.typography.titleMedium)
        SettingSwitchRow("Background sync", syncEnabled) {
            syncEnabled = it
            prefs.edit().putBoolean("enabled", it).apply()
        }
        SettingSwitchRow("Notifications", notify) {
            notify = it
            prefs.edit().putBoolean("notify", it).apply()
        }

        HorizontalDivider()
        Text("Privacy & Encrypted Backup", style = MaterialTheme.typography.titleMedium)
        SettingSwitchRow("Block remote images", blockImages) {
            blockImages = it
            prefs.edit().putBoolean("images", !it).apply()
        }
        SettingSwitchRow("Strip tracking parameters", stripTracking) {
            stripTracking = it
            prefs.edit().putBoolean("strip_tracking", it).apply()
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onExportAccounts, modifier = Modifier.weight(1f)) {
                Text("Export Accounts")
            }
            OutlinedButton(onClick = onImportAccounts, modifier = Modifier.weight(1f)) {
                Text("Import Accounts")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onExportMail, modifier = Modifier.weight(1f)) {
                Text("Export Mail")
            }
            OutlinedButton(onClick = onImportMail, modifier = Modifier.weight(1f)) {
                Text("Import Mail")
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessageDetailScreen(
    message: TupleMessageEx,
    onBack: () -> Unit,
    onReply: (TupleMessageEx) -> Unit,
    onForward: (TupleMessageEx) -> Unit,
    onToggleStar: (TupleMessageEx) -> Unit,
    onDelete: (TupleMessageEx) -> Unit
) {
    val context = LocalContext.current
    var htmlBody by remember(message.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(message.id) {
        val msgId = message.id ?: return@LaunchedEffect
        htmlBody = withContext(Dispatchers.IO) {
            val file = EntityMessage.getFile(context, msgId)
            if (file.exists()) Helper.readText(file) else message.preview.orEmpty()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = message.subject ?: "(No subject)",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_close_24),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleStar(message) }) {
                        Icon(
                            painter = painterResource(
                                if (message.ui_flagged) R.drawable.twotone_star_24 else R.drawable.twotone_star_border_24
                            ),
                            contentDescription = "Star"
                        )
                    }
                    IconButton(onClick = { onDelete(message) }) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_delete_24),
                            contentDescription = "Delete"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { onReply(message) }, modifier = Modifier.weight(1f)) {
                    Text("Reply")
                }
                OutlinedButton(onClick = { onForward(message) }, modifier = Modifier.weight(1f)) {
                    Text("Forward")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = MessageHelper.formatAddressesShort(message.from),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            val body = htmlBody
            if (body == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.title_loading))
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = false
                            settings.loadsImagesAutomatically = false
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, body, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainTabPreview() {
    FairEmailTheme {
        NavigationBar {
            MainTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab == MainTab.Inbox,
                    onClick = {},
                    icon = { Icon(painterResource(tab.iconRes), contentDescription = tab.label) },
                    label = { Text(tab.label) }
                )
            }
        }
    }
}
