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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAnswerButtonContent(
    options: List<String>,
    singleIndexState: MutableState<Int>,
    longIndexState: MutableState<Int>,
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_settings_24),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = stringResource(id = R.string.title_advanced_answer_caption),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Text(
            text = stringResource(id = R.string.title_advanced_answer_default),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 24.dp)
        )

        AnswerDropdown(
            options = options,
            selectedIndex = singleIndexState.value,
            onSelect = { singleIndexState.value = it }
        )

        Text(
            text = stringResource(id = R.string.title_advanced_answer_action),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 24.dp)
        )

        AnswerDropdown(
            options = options,
            selectedIndex = longIndexState.value,
            onSelect = { longIndexState.value = it }
        )

        val values = stringArrayResource(id = R.array.answerValues)
        val singleVal = values.getOrNull(singleIndexState.value) ?: ""
        val longVal = values.getOrNull(longIndexState.value) ?: ""
        val showWarning = singleVal != "menu" && longVal != "menu"

        if (showWarning) {
            Text(
                text = stringResource(id = R.string.title_advanced_answer_warning),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        Text(
            text = stringResource(id = R.string.title_advanced_answer_hint),
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnswerDropdown(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val selectedText = options.getOrNull(selectedIndex) ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
    ) {
        OutlinedTextField(
            value = selectedText,
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
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onSelect(index)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

class DialogAnswerButtonView(
    context: Context,
    options: List<String>,
    initialSinglePos: Int,
    initialLongPos: Int
) {
    val singleIndexState: MutableState<Int> = mutableIntStateOf(initialSinglePos)
    val longIndexState: MutableState<Int> = mutableIntStateOf(initialLongPos)

    val singleSelectedPosition: Int
        get() = singleIndexState.value

    val longSelectedPosition: Int
        get() = longIndexState.value

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            FairEmailTheme {
                DialogAnswerButtonContent(
                    options = options,
                    singleIndexState = singleIndexState,
                    longIndexState = longIndexState
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialogAnswerButtonPreview() {
    FairEmailTheme {
        DialogAnswerButtonContent(
            options = listOf("Reply", "Reply all", "Menu"),
            singleIndexState = remember { mutableIntStateOf(0) },
            longIndexState = remember { mutableIntStateOf(1) }
        )
    }
}
