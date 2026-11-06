package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogResetQuestionsContent(
    generalState: MutableState<Boolean>,
    linksState: MutableState<Boolean>,
    filesState: MutableState<Boolean>,
    imagesState: MutableState<Boolean>,
    fullState: MutableState<Boolean>,
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
                painter = painterResource(id = R.drawable.twotone_restart_alt_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_setup_reset_questions),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        ResetQuestionCheckbox(
            text = stringResource(id = R.string.title_setup_reset_general),
            state = generalState,
            modifier = Modifier.padding(top = 16.dp)
        )

        ResetQuestionCheckbox(
            text = stringResource(id = R.string.title_setup_reset_links),
            state = linksState,
            modifier = Modifier.padding(top = 12.dp)
        )

        ResetQuestionCheckbox(
            text = stringResource(id = R.string.title_setup_reset_files),
            state = filesState,
            modifier = Modifier.padding(top = 12.dp)
        )

        ResetQuestionCheckbox(
            text = stringResource(id = R.string.title_setup_reset_images),
            state = imagesState,
            modifier = Modifier.padding(top = 12.dp)
        )

        ResetQuestionCheckbox(
            text = stringResource(id = R.string.title_setup_reset_full),
            state = fullState,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ResetQuestionCheckbox(
    text: String,
    state: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { state.value = !state.value },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = state.value,
            onCheckedChange = { state.value = it }
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

class DialogResetQuestionsView(context: Context) {
    val generalState: MutableState<Boolean> = mutableStateOf(true)
    val linksState: MutableState<Boolean> = mutableStateOf(false)
    val filesState: MutableState<Boolean> = mutableStateOf(false)
    val imagesState: MutableState<Boolean> = mutableStateOf(false)
    val fullState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogResetQuestionsContent(
                    generalState = generalState,
                    linksState = linksState,
                    filesState = filesState,
                    imagesState = imagesState,
                    fullState = fullState
                )
            }
        }
    }

    val isGeneral: Boolean
        get() = generalState.value

    val isLinks: Boolean
        get() = linksState.value

    val isFiles: Boolean
        get() = filesState.value

    val isImages: Boolean
        get() = imagesState.value

    val isFull: Boolean
        get() = fullState.value
}

@Preview(showBackground = true)
@Composable
fun DialogResetQuestionsPreview() {
    FairEmailTheme {
        DialogResetQuestionsContent(
            generalState = remember { mutableStateOf(true) },
            linksState = remember { mutableStateOf(false) },
            filesState = remember { mutableStateOf(false) },
            imagesState = remember { mutableStateOf(false) },
            fullState = remember { mutableStateOf(false) }
        )
    }
}
