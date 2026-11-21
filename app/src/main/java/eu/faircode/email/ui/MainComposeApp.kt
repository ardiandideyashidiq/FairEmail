package eu.faircode.email

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.text.format.DateUtils
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import eu.faircode.email.ui.AddAccountMode
import eu.faircode.email.ui.AddAccountSheet
import eu.faircode.email.ui.SingleInputDialogContent
import eu.faircode.email.ui.theme.ExpressiveMotion
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
internal fun <T> observeLiveData(liveData: LiveData<T>, initial: T): T {
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
fun MainComposeApp(
    initialOpenMessageId: Long = -1L,
    initialResumeDraftId: Long = -1L,
    initialOpenTab: String? = null,
    initialError: String? = null,
    onConsumeInitialDeepLink: () -> Unit = {},
    onExitApp: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember(context) { DB.getInstance(context) }
    val prefs = remember(context) { PreferenceManager.getDefaultSharedPreferences(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var eulaAccepted by remember { mutableStateOf(prefs.getBoolean("eula", false)) }
    if (!eulaAccepted) {
        EulaWelcomeScreen(
            onAgree = {
                prefs.edit().putBoolean("eula", true).apply()
                eulaAccepted = true
            },
            onDisagree = onExitApp
        )
        return
    }

    var currentTab by remember { mutableStateOf(MainTab.Inbox) }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var selectedFolderTitle by remember { mutableStateOf<String?>(null) }
    var openedMessage by remember { mutableStateOf<TupleMessageEx?>(null) }
    var composeTarget by remember { mutableStateOf<ComposeTarget?>(null) }
    var addAccountMode by remember { mutableStateOf<AddAccountMode?>(null) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    val unseenMessages = observeLiveData(
        liveData = remember(db) { db.message().liveComposeMessages(null, true, false, false, null, 100) },
        initial = emptyList()
    )

    val canNavigateBack = fabMenuExpanded ||
        composeTarget != null ||
        openedMessage != null ||
        (currentTab == MainTab.Inbox && selectedFolderId != null) ||
        currentTab != MainTab.Inbox

    BackHandler(enabled = canNavigateBack) {
        when {
            fabMenuExpanded -> fabMenuExpanded = false
            composeTarget != null -> composeTarget = null
            openedMessage != null -> openedMessage = null
            currentTab == MainTab.Inbox && selectedFolderId != null -> {
                selectedFolderId = null
                selectedFolderTitle = null
                currentTab = MainTab.Folders
            }
            currentTab != MainTab.Inbox -> currentTab = MainTab.Inbox
        }
    }

    LaunchedEffect(initialOpenMessageId, initialResumeDraftId, initialOpenTab, initialError) {
        if (initialOpenTab == "accounts") {
            currentTab = MainTab.Accounts
            onConsumeInitialDeepLink()
        } else if (initialOpenMessageId > 0L) {
            val loaded = withContext(Dispatchers.IO) {
                db.message().getMessageEx(initialOpenMessageId)
            }
            if (loaded != null) {
                openedMessage = loaded
            } else {
                snackbarHostState.showSnackbar(context.getString(R.string.title_no_message))
            }
            onConsumeInitialDeepLink()
        } else if (initialResumeDraftId > 0L) {
            val draft = withContext(Dispatchers.IO) {
                db.message().getMessageEx(initialResumeDraftId)
            }
            if (draft != null) {
                composeTarget = ComposeTarget.Reply(draft)
            }
            onConsumeInitialDeepLink()
        } else if (!initialError.isNullOrBlank()) {
            snackbarHostState.showSnackbar(initialError)
            onConsumeInitialDeepLink()
        }
    }

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

    AnimatedContent(
        targetState = Pair(composeTarget, openedMessage),
        transitionSpec = {
            (fadeIn(ExpressiveMotion.effectsDefault()) + scaleIn(
                initialScale = 0.96f,
                animationSpec = ExpressiveMotion.spatialDefault()
            )) togetherWith (fadeOut(ExpressiveMotion.effectsDefault()) + scaleOut(
                targetScale = 0.96f,
                animationSpec = ExpressiveMotion.spatialDefault()
            ))
        },
        label = "MessageDetailTransition"
    ) { (activeCompose, msg) ->
        if (activeCompose != null) {
            ComposeEmailScreen(
                db = db,
                target = activeCompose,
                onBack = { composeTarget = null },
                onCompleted = { feedback ->
                    composeTarget = null
                    scope.launch { snackbarHostState.showSnackbar(feedback) }
                }
            )
        } else if (msg != null) {
            MessageDetailScreen(
                message = msg,
                onBack = { openedMessage = null },
                onReply = { composeTarget = ComposeTarget.Reply(it) },
                onForward = { composeTarget = ComposeTarget.Forward(it) },
                onToggleStar = {
                    scope.launch(Dispatchers.IO) {
                        EntityOperation.queue(context, it, EntityOperation.FLAG, !it.ui_flagged)
                        ServiceSynchronize.eval(context, "flag")
                    }
                },
                onDelete = {
                    scope.launch(Dispatchers.IO) {
                        EntityOperation.queue(context, it, EntityOperation.DELETE)
                        ServiceSynchronize.eval(context, "delete")
                    }
                    openedMessage = null
                }
            )
        } else {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (currentTab == MainTab.Inbox && selectedFolderTitle != null)
                                    selectedFolderTitle!! else currentTab.label,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
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
                bottomBar = {
                    FloatingExpressiveDock(
                        currentTab = currentTab,
                        unreadCount = unseenMessages.size,
                        fabMenuExpanded = fabMenuExpanded,
                        onSelectTab = {
                            fabMenuExpanded = false
                            currentTab = it
                        },
                        onFabClick = {
                            if (fabMenuExpanded) {
                                fabMenuExpanded = false
                            } else {
                                composeTarget = ComposeTarget.New
                            }
                        },
                        onFabLongClick = { fabMenuExpanded = !fabMenuExpanded },
                        onNewEmail = {
                            fabMenuExpanded = false
                            composeTarget = ComposeTarget.New
                        },
                        onSyncNow = {
                            fabMenuExpanded = false
                            ServiceUI.sync(context, null)
                            scope.launch { snackbarHostState.showSnackbar("Syncing Gmail...") }
                        },
                        onAddGmail = {
                            fabMenuExpanded = false
                            addAccountMode = AddAccountMode.OAuth
                        }
                    )
                }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(ExpressiveMotion.effectsDefault()) +
                            scaleIn(initialScale = 0.98f, animationSpec = ExpressiveMotion.spatialFast()) togetherWith
                            fadeOut(ExpressiveMotion.effectsDefault())
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    label = "TabTransition"
                ) { tab ->
                    when (tab) {
                        MainTab.Inbox -> InboxScreen(
                            db = db,
                            folderId = selectedFolderId,
                            onMessageClick = { item ->
                                openedMessage = item
                                if (!item.ui_seen) {
                                    scope.launch(Dispatchers.IO) {
                                        EntityOperation.queue(context, item, EntityOperation.SEEN, true)
                                        ServiceSynchronize.eval(context, "seen")
                                    }
                                }
                            },
                            onToggleStar = { item ->
                                scope.launch(Dispatchers.IO) {
                                    EntityOperation.queue(context, item, EntityOperation.FLAG, !item.ui_flagged)
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
                            onMessageClick = { item -> openedMessage = item },
                            onToggleStar = { item ->
                                scope.launch(Dispatchers.IO) {
                                    EntityOperation.queue(context, item, EntityOperation.FLAG, !item.ui_flagged)
                                    ServiceSynchronize.eval(context, "flag")
                                }
                            }
                        )

                        MainTab.Accounts -> AccountsScreen(
                            db = db,
                            onAddGmailOAuth = { addAccountMode = AddAccountMode.OAuth },
                            onAddGmailAndroid = { addAccountMode = AddAccountMode.AndroidAccount },
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
        }
    }

    if (addAccountMode != null) {
        AddAccountSheet(
            initialMode = addAccountMode!!,
            onDismiss = { addAccountMode = null },
            onSuccess = { message ->
                addAccountMode = null
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    if (pendingBackupAction != null && pendingBackupUri != null) {
        val action = pendingBackupAction!!
        val uri = pendingBackupUri!!

        AlertDialog(
            onDismissRequest = {
                pendingBackupAction = null
                pendingBackupUri = null
            },
            text = {
                SingleInputDialogContent(
                    titleIconRes = R.drawable.twotone_vpn_key_24,
                    titleRes = R.string.title_setup_password,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FloatingExpressiveDock(
    currentTab: MainTab,
    unreadCount: Int,
    fabMenuExpanded: Boolean,
    onSelectTab: (MainTab) -> Unit,
    onFabClick: () -> Unit,
    onFabLongClick: () -> Unit,
    onNewEmail: () -> Unit,
    onSyncNow: () -> Unit,
    onAddGmail: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var dragAccumulator by remember { mutableStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnimatedVisibility(
            visible = fabMenuExpanded,
            enter = fadeIn(ExpressiveMotion.effectsDefault()) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = ExpressiveMotion.spatialFast()
                ) +
                scaleIn(initialScale = 0.9f, animationSpec = ExpressiveMotion.spatialFast()),
            exit = fadeOut(ExpressiveMotion.effectsDefault()) +
                slideOutVertically(
                    targetOffsetY = { it / 2 },
                    animationSpec = ExpressiveMotion.spatialFast()
                ) +
                scaleOut(targetScale = 0.9f, animationSpec = ExpressiveMotion.spatialFast())
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExpressiveFabMenuItem(
                    iconRes = R.drawable.twotone_account_circle_24,
                    label = "Add Gmail",
                    onClick = onAddGmail
                )
                ExpressiveFabMenuItem(
                    iconRes = R.drawable.twotone_sync_24,
                    label = "Sync Now",
                    onClick = onSyncNow
                )
                ExpressiveFabMenuItem(
                    iconRes = R.drawable.twotone_edit_24,
                    label = stringResource(R.string.title_compose),
                    onClick = onNewEmail
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(currentTab) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccumulator = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccumulator += dragAmount
                                val threshold = 72f
                                if (dragAccumulator > threshold) {
                                    val idx = MainTab.entries.indexOf(currentTab)
                                    if (idx < MainTab.entries.lastIndex) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectTab(MainTab.entries[idx + 1])
                                    }
                                    dragAccumulator = 0f
                                } else if (dragAccumulator < -threshold) {
                                    val idx = MainTab.entries.indexOf(currentTab)
                                    if (idx > 0) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSelectTab(MainTab.entries[idx - 1])
                                    }
                                    dragAccumulator = 0f
                                }
                            }
                        )
                    }
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                        .height(50.dp)
                ) {
                    val tabs = MainTab.entries
                    val tabWidth = maxWidth / tabs.size
                    val selectedIndex = tabs.indexOf(currentTab).coerceAtLeast(0)
                    val indicatorOffsetState = animateDpAsState(
                        targetValue = tabWidth * selectedIndex,
                        animationSpec = ExpressiveMotion.spatialFast(),
                        label = "SlidingIndicatorOffset"
                    )

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(indicatorOffsetState.value.roundToPx(), 0) }
                            .width(tabWidth)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentTab == tab
                            val interactionSource = remember { MutableInteractionSource() }
                            val pressed by interactionSource.collectIsPressedAsState()
                            val contentColor by animateColorAsState(
                                targetValue = if (selected)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                animationSpec = ExpressiveMotion.effectsDefault(),
                                label = "NavTextColor"
                            )
                            val scaleState = animateFloatAsState(
                                targetValue = when {
                                    pressed -> 0.92f
                                    selected -> 1.03f
                                    else -> 1f
                                },
                                animationSpec = ExpressiveMotion.spatialFast(),
                                label = "NavScale"
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .graphicsLayer {
                                        val s = scaleState.value
                                        scaleX = s
                                        scaleY = s
                                    }
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = null
                                    ) {
                                        if (!selected) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onSelectTab(tab)
                                    }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(
                                        painter = painterResource(tab.iconRes),
                                        contentDescription = tab.label,
                                        tint = contentColor,
                                        modifier = Modifier.size(21.dp)
                                    )
                                    if (tab == MainTab.Inbox && unreadCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError,
                                            modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                                        ) {
                                            Text(
                                                text = if (unreadCount >= 100) "99+" else unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = contentColor,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            val fabInteraction = remember { MutableInteractionSource() }
            val fabPressed by fabInteraction.collectIsPressedAsState()
            val fabCorner by animateDpAsState(
                targetValue = when {
                    fabMenuExpanded -> 28.dp
                    fabPressed -> 14.dp
                    else -> 20.dp
                },
                animationSpec = ExpressiveMotion.spatialFast(),
                label = "FabCornerMorph"
            )
            val fabScaleState = animateFloatAsState(
                targetValue = if (fabPressed) 0.92f else 1f,
                animationSpec = ExpressiveMotion.spatialFast(),
                label = "FabScale"
            )
            val fabContainer by animateColorAsState(
                targetValue = if (fabMenuExpanded)
                    MaterialTheme.colorScheme.tertiaryContainer
                else
                    MaterialTheme.colorScheme.primary,
                animationSpec = ExpressiveMotion.effectsDefault(),
                label = "FabContainer"
            )
            val fabContent by animateColorAsState(
                targetValue = if (fabMenuExpanded)
                    MaterialTheme.colorScheme.onTertiaryContainer
                else
                    MaterialTheme.colorScheme.onPrimary,
                animationSpec = ExpressiveMotion.effectsDefault(),
                label = "FabContent"
            )

            Surface(
                shape = RoundedCornerShape(fabCorner),
                color = fabContainer,
                contentColor = fabContent,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        val s = fabScaleState.value
                        scaleX = s
                        scaleY = s
                    }
                    .clip(RoundedCornerShape(fabCorner))
                    .combinedClickable(
                        interactionSource = fabInteraction,
                        indication = null,
                        onClick = onFabClick,
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onFabLongClick()
                        }
                    )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(
                            if (fabMenuExpanded) R.drawable.twotone_close_24 else R.drawable.twotone_edit_24
                        ),
                        contentDescription = stringResource(R.string.title_compose),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpressiveFabMenuItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        }
    }
}

@Composable
private fun ExpressiveFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val corner by animateDpAsState(
        targetValue = if (selected) 20.dp else 10.dp,
        animationSpec = ExpressiveMotion.spatialFast(),
        label = "ChipCorner"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = ExpressiveMotion.effectsDefault(),
        label = "ChipColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = ExpressiveMotion.effectsDefault(),
        label = "ChipTextColor"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(corner),
        color = bgColor,
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExpressiveFilterChip(
                label = "All",
                selected = !unseenOnly && !flaggedOnly && !attachmentsOnly,
                onClick = {
                    unseenOnly = false
                    flaggedOnly = false
                    attachmentsOnly = false
                }
            )
            ExpressiveFilterChip(
                label = "Unread",
                selected = unseenOnly,
                onClick = { unseenOnly = !unseenOnly }
            )
            ExpressiveFilterChip(
                label = "Starred",
                selected = flaggedOnly,
                onClick = { flaggedOnly = !flaggedOnly }
            )
            ExpressiveFilterChip(
                label = "Attachments",
                selected = attachmentsOnly,
                onClick = { attachmentsOnly = !attachmentsOnly }
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id ?: 0L }) { msg ->
                    val onItemClick = remember(msg.id) { { onMessageClick(msg) } }
                    val onItemStar = remember(msg.id, msg.ui_flagged) { { onToggleStar(msg) } }
                    MessageRowItem(
                        message = msg,
                        onClick = onItemClick,
                        onToggleStar = onItemStar
                    )
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
    val cardShape = remember(message.ui_seen) {
        RoundedCornerShape(if (message.ui_seen) 14.dp else 22.dp)
    }
    val cardContainer by animateColorAsState(
        targetValue = if (message.ui_seen)
            MaterialTheme.colorScheme.surfaceContainerLow
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = ExpressiveMotion.effectsDefault(),
        label = "MsgContainer"
    )
    val starScaleState = animateFloatAsState(
        targetValue = if (message.ui_flagged) 1.15f else 1f,
        animationSpec = ExpressiveMotion.spatialFast(),
        label = "StarScale"
    )

    Card(
        onClick = onClick,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!message.ui_seen) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
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
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
            IconButton(
                onClick = onToggleStar,
                modifier = Modifier.graphicsLayer {
                    val s = starScaleState.value
                    scaleX = s
                    scaleY = s
                }
            ) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(folders, key = { it.id ?: 0L }) { folder ->
                Card(
                    onClick = { onFolderClick(folder) },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(EntityFolder.getIcon(folder.type)),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.getDisplayName(context),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val accName = folder.accountName
                            if (!accName.isNullOrEmpty()) {
                                Text(
                                    text = accName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (folder.unseen > 0) {
                            Badge { Text(folder.unseen.toString()) }
                        }
                    }
                }
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
            shape = CircleShape,
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
            ExpressiveFilterChip(label = "Unread", selected = unseenOnly, onClick = { unseenOnly = !unseenOnly })
            ExpressiveFilterChip(label = "Starred", selected = flaggedOnly, onClick = { flaggedOnly = !flaggedOnly })
            ExpressiveFilterChip(label = "Attachments", selected = attachmentsOnly, onClick = { attachmentsOnly = !attachmentsOnly })
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id ?: 0L }) { msg ->
                MessageRowItem(
                    message = msg,
                    onClick = { onMessageClick(msg) },
                    onToggleStar = { onToggleStar(msg) }
                )
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
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = account.name ?: account.user ?: "Gmail",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        val user = account.user
                        if (!user.isNullOrEmpty()) {
                            Text(
                                text = user,
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
            Button(
                onClick = onAddGmailOAuth,
                shape = CircleShape,
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Gmail (OAuth)")
            }
            OutlinedButton(
                onClick = onAddGmailAndroid,
                shape = CircleShape,
                modifier = Modifier.weight(1f)
            ) {
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
            Button(onClick = onExportAccounts, shape = CircleShape, modifier = Modifier.weight(1f)) {
                Text("Export Accounts")
            }
            OutlinedButton(onClick = onImportAccounts, shape = CircleShape, modifier = Modifier.weight(1f)) {
                Text("Import Accounts")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onExportMail, shape = CircleShape, modifier = Modifier.weight(1f)) {
                Text("Export Mail")
            }
            OutlinedButton(onClick = onImportMail, shape = CircleShape, modifier = Modifier.weight(1f)) {
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
            OutlinedButton(onClick = onExportAccounts, shape = CircleShape, modifier = Modifier.weight(1f)) {
                Text("Export Accounts")
            }
            OutlinedButton(onClick = onImportAccounts, shape = CircleShape, modifier = Modifier.weight(1f)) {
                Text("Import Accounts")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onExportMail, shape = CircleShape, modifier = Modifier.weight(1f)) {
                Text("Export Mail")
            }
            OutlinedButton(onClick = onImportMail, shape = CircleShape, modifier = Modifier.weight(1f)) {
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
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
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
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onReply(message) }) {
                            Icon(
                                painter = painterResource(R.drawable.twotone_reply_24),
                                contentDescription = "Reply"
                            )
                        }
                        IconButton(onClick = { onForward(message) }) {
                            Icon(
                                painter = painterResource(R.drawable.twotone_forward_24),
                                contentDescription = "Forward"
                            )
                        }
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
private fun FloatingNavPreview() {
    FairEmailTheme {
        FloatingExpressiveDock(
            currentTab = MainTab.Inbox,
            unreadCount = 3,
            fabMenuExpanded = false,
            onSelectTab = {},
            onFabClick = {},
            onFabLongClick = {},
            onNewEmail = {},
            onSyncNow = {},
            onAddGmail = {}
        )
    }
}
