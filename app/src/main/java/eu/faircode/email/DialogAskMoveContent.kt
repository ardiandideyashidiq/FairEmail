package eu.faircode.email

import android.content.Context
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogAskMoveContent(
    messages: String,
    sourceFolders: String,
    targetFolders: String,
    @DrawableRes sourceIcon: Int?,
    @DrawableRes targetIcon: Int?,
    sourceIconColor: Color?,
    targetIconColor: Color?,
    showNotAgain: Boolean,
    notAgainState: MutableState<Boolean>,
    showJunkLearn: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = messages,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sourceIcon != null) {
                Icon(
                    painter = painterResource(id = sourceIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp),
                    tint = sourceIconColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = sourceFolders,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.twotone_south_24),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 12.dp)
                .size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (targetIcon != null) {
                Icon(
                    painter = painterResource(id = targetIcon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp),
                    tint = targetIconColor ?: MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = targetFolders,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        if (showNotAgain) {
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clickable { notAgainState.value = !notAgainState.value },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = notAgainState.value,
                    onCheckedChange = { notAgainState.value = it }
                )
                Text(
                    text = stringResource(id = R.string.title_no_ask_again),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        if (showJunkLearn) {
            Text(
                text = stringResource(id = R.string.title_hint_junk_learn),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }
    }
}

class DialogAskMoveView(
    context: Context,
    messages: String,
    sourceFolders: String,
    targetFolders: String,
    @DrawableRes sourceIcon: Int?,
    @DrawableRes targetIcon: Int?,
    sourceColor: Int?,
    targetColor: Int?,
    showNotAgain: Boolean,
    showJunkLearn: Boolean,
    onNotAgainChanged: ((Boolean) -> Unit)? = null
) {
    val notAgainState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            FairEmailTheme {
                val currentNotAgain = notAgainState.value
                androidx.compose.runtime.LaunchedEffect(currentNotAgain) {
                    onNotAgainChanged?.invoke(currentNotAgain)
                }

                DialogAskMoveContent(
                    messages = messages,
                    sourceFolders = sourceFolders,
                    targetFolders = targetFolders,
                    sourceIcon = sourceIcon,
                    targetIcon = targetIcon,
                    sourceIconColor = sourceColor?.let { Color(it) },
                    targetIconColor = targetColor?.let { Color(it) },
                    showNotAgain = showNotAgain,
                    notAgainState = notAgainState,
                    showJunkLearn = showJunkLearn
                )
            }
        }
    }

    val isNotAgain: Boolean
        get() = notAgainState.value
}

@Preview(showBackground = true)
@Composable
fun DialogAskMovePreview() {
    FairEmailTheme {
        DialogAskMoveContent(
            messages = "Move 3 messages?",
            sourceFolders = "Inbox",
            targetFolders = "Archive",
            sourceIcon = R.drawable.twotone_inbox_24,
            targetIcon = R.drawable.twotone_archive_24,
            sourceIconColor = null,
            targetIconColor = null,
            showNotAgain = true,
            notAgainState = remember { mutableStateOf(false) },
            showJunkLearn = true
        )
    }
}
