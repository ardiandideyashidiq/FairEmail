package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun DialogPlaceholderContent(
    nameState: MutableState<String>,
    valueState: MutableState<String>,
    onNameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_answer_placeholder),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 4.dp)
            )
            OutlinedTextField(
                value = nameState.value,
                onValueChange = {
                    nameState.value = it
                    onNameChanged(it)
                },
                label = { Text(text = stringResource(id = R.string.title_answer_define_name)) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        OutlinedTextField(
            value = valueState.value,
            onValueChange = { valueState.value = it },
            label = { Text(text = stringResource(id = R.string.title_answer_define_value)) },
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

class DialogPlaceholderView(
    context: Context,
    initialName: String?
) {
    val nameState: MutableState<String> = mutableStateOf(initialName ?: "")
    val valueState: MutableState<String> = mutableStateOf("")

    init {
        if (!initialName.isNullOrEmpty()) {
            val v = EntityAnswer.getCustomPlaceholder(context, initialName.trim())
            if (!v.isNullOrEmpty())
                valueState.value = v
        }
    }

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogPlaceholderContent(
                    nameState = nameState,
                    valueState = valueState,
                    onNameChanged = { name ->
                        val v = EntityAnswer.getCustomPlaceholder(context, name.trim())
                        if (!v.isNullOrEmpty())
                            valueState.value = v
                    }
                )
            }
        }
    }

    val name: String
        get() = nameState.value

    val value: String
        get() = valueState.value
}

@Preview(showBackground = true)
@Composable
fun DialogPlaceholderPreview() {
    FairEmailTheme {
        DialogPlaceholderContent(
            nameState = remember { mutableStateOf("signature") },
            valueState = remember { mutableStateOf("Best regards,\nJohn") },
            onNameChanged = {}
        )
    }
}
