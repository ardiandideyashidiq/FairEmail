package eu.faircode.email.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun ConfirmCheckboxDialogContent(
    @DrawableRes iconRes: Int,
    useWarningTint: Boolean,
    message: String,
    @StringRes checkboxLabelRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
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
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = if (useWarningTint) FairEmailTheme.colors.warning else LocalContentColor.current
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
        CheckboxRowItem(
            text = stringResource(checkboxLabelRes),
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(top = 24.dp)
        )
    }
}

class ConfirmCheckboxDialogComposeView(
    context: Context,
    @DrawableRes private val iconRes: Int,
    private val useWarningTint: Boolean,
    @StringRes private val checkboxLabelRes: Int
) : AbstractComposeView(context) {

    private var messageState by mutableStateOf("")
    private var checkedState by mutableStateOf(false)
    private var listener: CheckboxRowComposeView.OnCheckedChangeListener? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun setMessage(message: CharSequence?) {
        messageState = message?.toString().orEmpty()
    }

    fun setMessage(@StringRes messageRes: Int) {
        messageState = context.getString(messageRes)
    }

    fun isChecked(): Boolean = checkedState

    fun setChecked(checked: Boolean) {
        checkedState = checked
    }

    fun setOnCheckedChangeListener(listener: CheckboxRowComposeView.OnCheckedChangeListener?) {
        this.listener = listener
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            ConfirmCheckboxDialogContent(
                iconRes = iconRes,
                useWarningTint = useWarningTint,
                message = messageState,
                checkboxLabelRes = checkboxLabelRes,
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
private fun ConfirmCheckboxDialogContentPreview() {
    FairEmailTheme {
        ConfirmCheckboxDialogContent(
            iconRes = R.drawable.twotone_report_24,
            useWarningTint = true,
            message = "Mark selected message as spam?",
            checkboxLabelRes = R.string.title_block_sender,
            checked = true,
            onCheckedChange = {}
        )
    }
}
