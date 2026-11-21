package eu.faircode.email

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import eu.faircode.email.ui.theme.FairEmailTheme

internal class ActivityComposeMain : ActivityBase() {
    private var openMessageId by mutableLongStateOf(-1L)
    private var resumeDraftId by mutableLongStateOf(-1L)
    private var initialError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consumeIntent(intent)
        setContent {
            FairEmailTheme {
                MainComposeApp(
                    initialOpenMessageId = openMessageId,
                    initialResumeDraftId = resumeDraftId,
                    initialError = initialError,
                    onConsumeInitialDeepLink = {
                        openMessageId = -1L
                        resumeDraftId = -1L
                        initialError = null
                    },
                    onExitApp = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        consumeIntent(intent)
    }

    private fun consumeIntent(intent: Intent?) {
        if (intent == null) return
        val msgId = intent.getLongExtra("open_message_id", -1L)
        if (msgId > 0L) openMessageId = msgId
        val draftId = intent.getLongExtra("resume_draft_id", -1L)
        if (draftId > 0L) resumeDraftId = draftId
        val err = intent.getStringExtra("error")
        if (!err.isNullOrBlank()) initialError = err
    }
}
