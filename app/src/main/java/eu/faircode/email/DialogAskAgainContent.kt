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
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogAskAgainContent(
    question: String,
    remark: String?,
    accept: String?,
    hasNotAgain: Boolean,
    isWarning: Boolean,
    notAgainState: MutableState<Boolean>,
    onInfoClick: (() -> Unit)?,
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
            if (isWarning) {
                Icon(
                    painter = painterResource(id = R.drawable.twotone_warning_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 8.dp)
                )
            }
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (onInfoClick != null) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.twotone_info_24),
                        contentDescription = stringResource(id = R.string.title_info),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (!remark.isNullOrEmpty()) {
            Text(
                text = remark,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (hasNotAgain) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clickable { notAgainState.value = !notAgainState.value },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = notAgainState.value,
                    onCheckedChange = { notAgainState.value = it }
                )
                Text(
                    text = stringResource(id = R.string.title_no_ask_again),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        val showAccept = (accept != null) && (!hasNotAgain || notAgainState.value)
        if (showAccept) {
            Text(
                text = accept,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

class DialogAskAgainView(
    context: Context,
    question: String,
    remark: String?,
    accept: String?,
    hasNotAgain: Boolean,
    isWarning: Boolean,
    onInfoClick: Runnable?
) {
    val notAgainState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogAskAgainContent(
                    question = question,
                    remark = remark,
                    accept = accept,
                    hasNotAgain = hasNotAgain,
                    isWarning = isWarning,
                    notAgainState = notAgainState,
                    onInfoClick = onInfoClick?.let { r -> { r.run() } }
                )
            }
        }
    }

    val isNotAgain: Boolean
        get() = notAgainState.value
}

@Preview(showBackground = true)
@Composable
fun DialogAskAgainPreview() {
    FairEmailTheme {
        DialogAskAgainContent(
            question = "Are you sure you want to permanently delete these messages?",
            remark = "This cannot be undone.",
            accept = "Messages will be removed from server.",
            hasNotAgain = true,
            isWarning = true,
            notAgainState = remember { mutableStateOf(true) },
            onInfoClick = {}
        )
    }
}
