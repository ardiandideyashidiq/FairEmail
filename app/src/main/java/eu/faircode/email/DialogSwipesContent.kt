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
fun DialogSwipesContent(
    actions: List<String>,
    leftIndexState: MutableState<Int>,
    rightIndexState: MutableState<Int>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

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
                painter = painterResource(id = R.drawable.twotone_swipe_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_advanced_swipe_actions),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = stringResource(id = R.string.title_advanced_swipe_actions_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = stringResource(id = R.string.title_account_left),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 20.dp)
        )

        SwipeActionDropdown(
            actions = actions,
            selectedIndex = leftIndexState.value,
            onSelect = { leftIndexState.value = it }
        )

        Text(
            text = stringResource(id = R.string.title_account_right),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )

        SwipeActionDropdown(
            actions = actions,
            selectedIndex = rightIndexState.value,
            onSelect = { rightIndexState.value = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeActionDropdown(
    actions: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val currentTitle = actions.getOrNull(selectedIndex) ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        OutlinedTextField(
            value = currentTitle,
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
            actions.forEachIndexed { index, action ->
                DropdownMenuItem(
                    text = { Text(text = action) },
                    onClick = {
                        onSelect(index)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

class DialogSwipesView(
    context: Context,
    val actionsList: List<EntityFolder>,
    initialLeftPos: Int,
    initialRightPos: Int
) {
    val actionNames = actionsList.map { it.name }
    val leftIndexState: MutableState<Int> = mutableIntStateOf(
        if (actionsList.isNotEmpty()) initialLeftPos.coerceIn(0, actionsList.size - 1) else 0
    )
    val rightIndexState: MutableState<Int> = mutableIntStateOf(
        if (actionsList.isNotEmpty()) initialRightPos.coerceIn(0, actionsList.size - 1) else 0
    )

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogSwipesContent(
                    actions = actionNames,
                    leftIndexState = leftIndexState,
                    rightIndexState = rightIndexState
                )
            }
        }
    }

    val selectedLeftPosition: Int
        get() = leftIndexState.value

    val selectedRightPosition: Int
        get() = rightIndexState.value

    val selectedLeftFolder: EntityFolder?
        get() = actionsList.getOrNull(leftIndexState.value)

    val selectedRightFolder: EntityFolder?
        get() = actionsList.getOrNull(rightIndexState.value)
}

@Preview(showBackground = true)
@Composable
fun DialogSwipesPreview() {
    FairEmailTheme {
        DialogSwipesContent(
            actions = listOf("None", "Archive", "Trash", "Spam", "Star"),
            leftIndexState = remember { mutableIntStateOf(2) },
            rightIndexState = remember { mutableIntStateOf(1) }
        )
    }
}
