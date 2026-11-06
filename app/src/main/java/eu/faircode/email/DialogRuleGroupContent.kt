package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogRuleGroupContent(
    textState: MutableState<String>,
    groups: List<String>,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val filtered = remember(textState.value, groups) {
        if (textState.value.isEmpty()) groups
        else groups.filter { it.contains(textState.value, ignoreCase = true) }
    }

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

        ExposedDropdownMenuBox(
            expanded = expanded.value && filtered.isNotEmpty(),
            onExpandedChange = { expanded.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            OutlinedTextField(
                value = textState.value,
                onValueChange = {
                    textState.value = it
                    expanded.value = true
                },
                singleLine = true,
                modifier = Modifier
                    .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded.value && filtered.isNotEmpty(),
                onDismissRequest = { expanded.value = false }
            ) {
                filtered.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(text = group) },
                        onClick = {
                            textState.value = group
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

class DialogRuleGroupView(context: Context, initialName: String?) {
    val textState: MutableState<String> = mutableStateOf(initialName ?: "")
    val groupsList = mutableStateListOf<String>()

    fun setGroups(groups: List<String>?) {
        groupsList.clear()
        if (groups != null)
            groupsList.addAll(groups)
    }

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogRuleGroupContent(
                    textState = textState,
                    groups = groupsList
                )
            }
        }
    }

    val text: String
        get() = textState.value
}

@Preview(showBackground = true)
@Composable
fun DialogRuleGroupPreview() {
    FairEmailTheme {
        DialogRuleGroupContent(
            textState = remember { mutableStateOf("Billing") },
            groups = listOf("Billing", "Newsletters", "Social")
        )
    }
}
