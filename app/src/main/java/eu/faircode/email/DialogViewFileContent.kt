package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogViewFileContent(
    fileTitle: String,
    mimeType: String,
    extensionLabel: String,
    notAgainTypeState: MutableState<Boolean>,
    notAgainState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(
            text = fileTitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = mimeType,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        val typeEnabled = !notAgainState.value

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .then(
                    if (typeEnabled) Modifier.clickable {
                        notAgainTypeState.value = !notAgainTypeState.value
                    } else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = notAgainTypeState.value,
                onCheckedChange = if (typeEnabled) { { notAgainTypeState.value = it } } else null,
                enabled = typeEnabled
            )
            Text(
                text = extensionLabel,
                fontSize = 14.sp,
                color = if (typeEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { notAgainState.value = !notAgainState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = notAgainState.value,
                onCheckedChange = { notAgainState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_no_ask_again),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

class DialogViewFileView(
    context: Context,
    fileTitle: String,
    mimeType: String,
    extensionLabel: String,
    val onTypeListener: OnCheckChangedListener?,
    val onAllListener: OnCheckChangedListener?
) {
    val notAgainTypeState: MutableState<Boolean> = mutableStateOf(false)
    val notAgainState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogViewFileContent(
                    fileTitle = fileTitle,
                    mimeType = mimeType,
                    extensionLabel = extensionLabel,
                    notAgainTypeState = notAgainTypeState,
                    notAgainState = notAgainState
                )
            }
        }
    }

    val isNotAgainType: Boolean
        get() = notAgainTypeState.value

    val isNotAgain: Boolean
        get() = notAgainState.value

    fun interface OnCheckChangedListener {
        fun onChanged(checked: Boolean)
    }
}

@Preview(showBackground = true)
@Composable
fun DialogViewFilePreview() {
    FairEmailTheme {
        DialogViewFileContent(
            fileTitle = "View invoice.pdf?",
            mimeType = "application/pdf",
            extensionLabel = "Do not ask again for .pdf",
            notAgainTypeState = remember { mutableStateOf(false) },
            notAgainState = remember { mutableStateOf(false) }
        )
    }
}
