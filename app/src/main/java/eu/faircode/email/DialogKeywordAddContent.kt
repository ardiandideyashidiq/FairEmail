package eu.faircode.email

import android.content.Context
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
fun DialogKeywordAddContent(
    textState: MutableState<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_add_keyword),
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

class DialogKeywordAddView(context: Context) {
    val textState: MutableState<String> = mutableStateOf("")

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogKeywordAddContent(textState = textState)
            }
        }
    }

    val text: String
        get() = textState.value
}

@Preview(showBackground = true)
@Composable
fun DialogKeywordAddPreview() {
    FairEmailTheme {
        DialogKeywordAddContent(
            textState = remember { mutableStateOf("IMPORTANT") }
        )
    }
}
