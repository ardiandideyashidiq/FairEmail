package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
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
fun DialogEditNameContent(
    nameState: MutableState<String>,
    primaryState: MutableState<Boolean>,
    showPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.title_advanced_edit_name),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        if (showPrimary) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = primaryState.value,
                    onCheckedChange = { primaryState.value = it }
                )
                Text(
                    text = stringResource(id = R.string.title_primary),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

class DialogEditNameView @JvmOverloads constructor(
    context: Context,
    initialName: String?,
    initialPrimary: Boolean = false,
    showPrimary: Boolean = false
) {
    val nameState: MutableState<String> = mutableStateOf(initialName ?: "")
    val primaryState: MutableState<Boolean> = mutableStateOf(initialPrimary)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogEditNameContent(
                    nameState = nameState,
                    primaryState = primaryState,
                    showPrimary = showPrimary
                )
            }
        }
    }

    val name: String
        get() = nameState.value

    val isPrimary: Boolean
        get() = primaryState.value
}

@Preview(showBackground = true)
@Composable
fun DialogEditNamePreview() {
    FairEmailTheme {
        DialogEditNameContent(
            nameState = remember { mutableStateOf("Work Account") },
            primaryState = remember { mutableStateOf(true) },
            showPrimary = true
        )
    }
}
