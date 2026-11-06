package eu.faircode.email

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SingleInputPrompt(
    title: String,
    textState: MutableState<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

class SingleInputPromptView(
    context: Context,
    @param:StringRes private val titleRes: Int,
    initialText: String = ""
) {
    val textState: MutableState<String> = mutableStateOf(initialText)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                SingleInputPrompt(
                    title = stringResource(id = titleRes),
                    textState = textState
                )
            }
        }
    }

    val text: String
        get() = textState.value
}

@Preview(showBackground = true)
@Composable
fun SingleInputPromptPreview() {
    FairEmailTheme {
        SingleInputPrompt(
            title = "Activation response",
            textState = remember { mutableStateOf("abc123xyz") }
        )
    }
}
