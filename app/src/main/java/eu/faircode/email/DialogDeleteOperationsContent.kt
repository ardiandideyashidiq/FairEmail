package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogDeleteOperationsContent(
    errorState: MutableState<Boolean>,
    fetchState: MutableState<Boolean>,
    moveState: MutableState<Boolean>,
    flagState: MutableState<Boolean>,
    deleteState: MutableState<Boolean>,
    sendState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val warningColor = remember(context) {
        Color(Helper.resolveColor(context, R.attr.colorWarning))
    }

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
                painter = painterResource(id = R.drawable.twotone_delete_forever_24),
                contentDescription = null,
                tint = warningColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = R.string.title_delete_operation_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OperationCheckbox(
            text = stringResource(id = R.string.title_delete_operation_error),
            checkedState = errorState,
            modifier = Modifier.padding(top = 24.dp)
        )

        OperationCheckbox(
            text = stringResource(id = R.string.title_delete_operation_fetch),
            checkedState = fetchState,
            description = "FETCH, DOWNLOAD, RAW, BODY, ATTACHMENT, HEADERS, RULE, SYNC",
            modifier = Modifier.padding(top = 12.dp)
        )

        OperationCheckbox(
            text = stringResource(id = R.string.title_delete_operation_move),
            checkedState = moveState,
            description = "MOVE, COPY",
            modifier = Modifier.padding(top = 12.dp)
        )

        OperationCheckbox(
            text = stringResource(id = R.string.title_delete_operation_flag),
            checkedState = flagState,
            description = "SEEN, ANSWERED, FLAG, KEYWORD, LABEL, REPORT",
            modifier = Modifier.padding(top = 12.dp)
        )

        OperationCheckbox(
            text = stringResource(id = R.string.title_delete_operation_delete),
            checkedState = deleteState,
            description = "DELETE, PURGE, EXPUNGE, DETACH",
            modifier = Modifier.padding(top = 12.dp)
        )

        OperationCheckbox(
            text = stringResource(id = R.string.title_delete_operation_send),
            checkedState = sendState,
            description = "SEND",
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun OperationCheckbox(
    text: String,
    checkedState: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { checkedState.value = !checkedState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (description != null) {
            Text(
                text = description,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 48.dp)
            )
        }
    }
}

class DialogDeleteOperationsView(context: Context) {
    val errorState: MutableState<Boolean> = mutableStateOf(true)
    val fetchState: MutableState<Boolean> = mutableStateOf(false)
    val moveState: MutableState<Boolean> = mutableStateOf(false)
    val flagState: MutableState<Boolean> = mutableStateOf(false)
    val deleteState: MutableState<Boolean> = mutableStateOf(false)
    val sendState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            FairEmailTheme {
                DialogDeleteOperationsContent(
                    errorState = errorState,
                    fetchState = fetchState,
                    moveState = moveState,
                    flagState = flagState,
                    deleteState = deleteState,
                    sendState = sendState
                )
            }
        }
    }

    val isError: Boolean
        get() = errorState.value

    val isFetch: Boolean
        get() = fetchState.value

    val isMove: Boolean
        get() = moveState.value

    val isFlag: Boolean
        get() = flagState.value

    val isDelete: Boolean
        get() = deleteState.value

    val isSend: Boolean
        get() = sendState.value
}

@Preview(showBackground = true)
@Composable
fun DialogDeleteOperationsPreview() {
    FairEmailTheme {
        DialogDeleteOperationsContent(
            errorState = remember { mutableStateOf(true) },
            fetchState = remember { mutableStateOf(false) },
            moveState = remember { mutableStateOf(false) },
            flagState = remember { mutableStateOf(false) },
            deleteState = remember { mutableStateOf(false) },
            sendState = remember { mutableStateOf(false) }
        )
    }
}
