package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogAskSpamContent(
    message: String,
    blockSenderState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clickable { blockSenderState.value = !blockSenderState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = blockSenderState.value,
                onCheckedChange = { blockSenderState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_block_sender),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

class DialogAskSpamView(
    context: Context,
    message: String,
    initialBlockSender: Boolean
) {
    val blockSenderState: MutableState<Boolean> = mutableStateOf(initialBlockSender)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogAskSpamContent(
                    message = message,
                    blockSenderState = blockSenderState
                )
            }
        }
    }

    val isBlockSender: Boolean
        get() = blockSenderState.value
}

@Preview(showBackground = true)
@Composable
fun DialogAskSpamPreview() {
    FairEmailTheme {
        DialogAskSpamContent(
            message = "Move 1 message to spam?",
            blockSenderState = remember { mutableStateOf(true) }
        )
    }
}
