package eu.faircode.email.ui

import android.content.Context
import androidx.annotation.DrawableRes
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun AskDialogContent(
    message: String,
    @DrawableRes leadingIconRes: Int = 0,
    leadingIconTint: Color? = null,
    showInfoButton: Boolean = false,
    onInfoClick: () -> Unit = {},
    remark: String? = null,
    @DrawableRes remarkTrailingIconRes: Int = 0,
    onRemarkClick: (() -> Unit)? = null,
    showNotAgain: Boolean = true,
    checked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    acceptText: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIconRes != 0) {
                Icon(
                    painter = painterResource(leadingIconRes),
                    contentDescription = null,
                    tint = leadingIconTint ?: LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            if (showInfoButton) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.twotone_info_24),
                        contentDescription = stringResource(R.string.title_info),
                        tint = FairEmailTheme.colors.accent
                    )
                }
            }
        }

        if (!remark.isNullOrEmpty()) {
            val remarkModifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .let { if (onRemarkClick != null) it.clickable(onClick = onRemarkClick) else it }

            Row(
                modifier = remarkModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = remark,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier.weight(1f)
                )
                if (remarkTrailingIconRes != 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(remarkTrailingIconRes),
                        contentDescription = null
                    )
                }
            }
        }

        if (showNotAgain) {
            CheckboxRowItem(
                text = stringResource(R.string.title_no_ask_again),
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        if (!acceptText.isNullOrEmpty() && (!showNotAgain || checked)) {
            Text(
                text = acceptText,
                color = FairEmailTheme.colors.warning,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                ),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

class AskDialogComposeView(context: Context) : AbstractComposeView(context) {
    private var messageState by mutableStateOf("")
    private var leadingIconResState by mutableIntStateOf(0)
    private var leadingIconTintState by mutableStateOf<Color?>(null)
    private var showInfoButtonState by mutableStateOf(false)
    private var remarkState by mutableStateOf<String?>(null)
    private var remarkTrailingIconResState by mutableIntStateOf(0)
    private var showNotAgainState by mutableStateOf(true)
    private var checkedState by mutableStateOf(false)
    private var acceptTextState by mutableStateOf<String?>(null)
    private var infoClickListener: Runnable? = null
    private var remarkClickListener: Runnable? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun setMessage(message: CharSequence?) {
        messageState = message?.toString().orEmpty()
    }

    fun setLeadingIcon(@DrawableRes iconRes: Int, tintArgb: Int? = null) {
        leadingIconResState = iconRes
        leadingIconTintState = tintArgb?.let { Color(it) }
    }

    fun setInfoButton(show: Boolean, listener: Runnable?) {
        showInfoButtonState = show
        infoClickListener = listener
    }

    fun setRemark(remark: CharSequence?, @DrawableRes trailingIconRes: Int = 0, listener: Runnable? = null) {
        remarkState = remark?.toString()
        remarkTrailingIconResState = trailingIconRes
        remarkClickListener = listener
    }

    fun setShowNotAgain(show: Boolean) {
        showNotAgainState = show
    }

    fun isChecked(): Boolean = checkedState

    fun setChecked(checked: Boolean) {
        checkedState = checked
    }

    fun setAcceptText(acceptText: CharSequence?) {
        acceptTextState = acceptText?.toString()
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            AskDialogContent(
                message = messageState,
                leadingIconRes = leadingIconResState,
                leadingIconTint = leadingIconTintState ?: if (leadingIconResState == R.drawable.twotone_bolt_24) FairEmailTheme.colors.accent else null,
                showInfoButton = showInfoButtonState,
                onInfoClick = { infoClickListener?.run() },
                remark = remarkState,
                remarkTrailingIconRes = remarkTrailingIconResState,
                onRemarkClick = remarkClickListener?.let { listener -> { listener.run() } },
                showNotAgain = showNotAgainState,
                checked = checkedState,
                onCheckedChange = { checkedState = it },
                acceptText = acceptTextState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AskDialogContentPreview() {
    FairEmailTheme {
        AskDialogContent(
            message = "Delete selected message permanently?",
            leadingIconRes = R.drawable.twotone_warning_24,
            leadingIconTint = FairEmailTheme.colors.warning,
            showInfoButton = true,
            remark = "This action cannot be undone.",
            showNotAgain = true,
            checked = true,
            acceptText = stringResource(R.string.title_ask_delete_accept)
        )
    }
}
