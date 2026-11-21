package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun ImportProgressDialogContent(
    waiting: Boolean,
    log: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.twotone_unarchive_24),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.title_setup_import),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            if (waiting) {
                Spacer(modifier = Modifier.width(6.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        SelectionContainer(modifier = Modifier.padding(top = 24.dp)) {
            Text(
                text = log,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

class ImportProgressComposeView(context: Context) : AbstractComposeView(context) {
    private var waitingState by mutableStateOf(true)
    private var logState by mutableStateOf("")

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun setWaiting(waiting: Boolean) {
        waitingState = waiting
    }

    fun setLog(log: CharSequence?) {
        logState = log?.toString().orEmpty()
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            ImportProgressDialogContent(
                waiting = waitingState,
                log = logState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImportProgressDialogContentPreview() {
    FairEmailTheme {
        ImportProgressDialogContent(
            waiting = true,
            log = "Importing settings...\nImporting accounts..."
        )
    }
}
