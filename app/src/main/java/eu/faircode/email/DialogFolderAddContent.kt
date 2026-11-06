package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogFolderAddContent(
    nameState: MutableState<String>,
    parents: List<String>,
    parentIndexState: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val expanded = remember { mutableStateOf(false) }
    val currentParent = parents.getOrNull(parentIndexState.value) ?: "-"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_add_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_create_folder),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text(text = stringResource(id = R.string.title_create_folder_name)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Text(
            text = stringResource(id = R.string.title_create_folder_parent),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            OutlinedTextField(
                value = currentParent,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                parents.forEachIndexed { index, parentName ->
                    DropdownMenuItem(
                        text = { Text(text = parentName) },
                        onClick = {
                            parentIndexState.value = index
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

class DialogFolderAddView(context: Context) {
    val nameState: MutableState<String> = mutableStateOf("")
    val parentsList = mutableStateListOf<String>("-")
    val parentIndexState: MutableState<Int> = mutableIntStateOf(0)

    fun setParents(parents: List<String>?) {
        parentsList.clear()
        if (parents != null)
            parentsList.addAll(parents)
    }

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogFolderAddContent(
                    nameState = nameState,
                    parents = parentsList,
                    parentIndexState = parentIndexState
                )
            }
        }
    }

    val name: String
        get() = nameState.value.trim()

    val selectedParent: String?
        get() {
            val idx = parentIndexState.value
            return if (idx <= 0 || idx >= parentsList.size) null else parentsList[idx]
        }
}

@Preview(showBackground = true)
@Composable
fun DialogFolderAddPreview() {
    FairEmailTheme {
        DialogFolderAddContent(
            nameState = remember { mutableStateOf("Work Projects") },
            parents = listOf("-", "Inbox", "Archive"),
            parentIndexState = remember { mutableIntStateOf(0) }
        )
    }
}
