package eu.faircode.email.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun AskMoveDialogContent(
    title: String,
    sourceFolders: String,
    @DrawableRes sourceIconRes: Int,
    sourceColor: Color?,
    targetFolders: String,
    @DrawableRes targetIconRes: Int,
    targetColor: Color?,
    showJunkLearn: Boolean,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.padding(top = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (sourceIconRes != 0) {
                Icon(
                    painter = painterResource(sourceIconRes),
                    contentDescription = null,
                    tint = sourceColor ?: LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = sourceFolders,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        Icon(
            painter = painterResource(R.drawable.twotone_south_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(top = 12.dp)
                .size(48.dp)
        )

        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (targetIconRes != 0) {
                Icon(
                    painter = painterResource(targetIconRes),
                    contentDescription = null,
                    tint = targetColor ?: LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = targetFolders,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        CheckboxRowItem(
            text = stringResource(R.string.title_no_ask_again),
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = 24.dp)
        )

        if (showJunkLearn) {
            Text(
                text = stringResource(R.string.title_hint_junk_learn),
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

class AskMoveDialogComposeView(context: Context) : AbstractComposeView(context) {
    private var titleState by mutableStateOf("")
    private var sourceFoldersState by mutableStateOf("")
    private var sourceIconResState by mutableIntStateOf(R.drawable.twotone_inbox_24)
    private var sourceColorState by mutableStateOf<Color?>(null)
    private var targetFoldersState by mutableStateOf("")
    private var targetIconResState by mutableIntStateOf(R.drawable.twotone_archive_24)
    private var targetColorState by mutableStateOf<Color?>(null)
    private var showJunkLearnState by mutableStateOf(false)
    private var checkedState by mutableStateOf(false)
    private var listener: CheckboxRowComposeView.OnCheckedChangeListener? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(
        title: String,
        sourceFolders: String,
        @DrawableRes sourceIconRes: Int,
        sourceColorArgb: Int?,
        targetFolders: String,
        @DrawableRes targetIconRes: Int,
        targetColorArgb: Int?,
        showJunkLearn: Boolean
    ) {
        titleState = title
        sourceFoldersState = sourceFolders
        sourceIconResState = sourceIconRes
        sourceColorState = sourceColorArgb?.let { Color(it) }
        targetFoldersState = targetFolders
        targetIconResState = targetIconRes
        targetColorState = targetColorArgb?.let { Color(it) }
        showJunkLearnState = showJunkLearn
    }

    fun setOnCheckedChangeListener(listener: CheckboxRowComposeView.OnCheckedChangeListener?) {
        this.listener = listener
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            AskMoveDialogContent(
                title = titleState,
                sourceFolders = sourceFoldersState,
                sourceIconRes = sourceIconResState,
                sourceColor = sourceColorState,
                targetFolders = targetFoldersState,
                targetIconRes = targetIconResState,
                targetColor = targetColorState,
                showJunkLearn = showJunkLearnState,
                checked = checkedState,
                onCheckedChange = { newValue ->
                    checkedState = newValue
                    listener?.onCheckedChanged(newValue)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AskMoveDialogContentPreview() {
    FairEmailTheme {
        AskMoveDialogContent(
            title = "Move 3 messages?",
            sourceFolders = "Inbox",
            sourceIconRes = R.drawable.twotone_inbox_24,
            sourceColor = null,
            targetFolders = "Archive",
            targetIconRes = R.drawable.twotone_archive_24,
            targetColor = null,
            showJunkLearn = false,
            checked = false,
            onCheckedChange = {}
        )
    }
}
