package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun CheckboxRowItem(
    text: String,
    checked: Boolean,
    enabled: Boolean = true,
    verticalPadding: Dp = 0.dp,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = verticalPadding)
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

class CheckboxRowComposeView(
    context: Context,
    private val verticalPaddingDp: Int = 0
) : AbstractComposeView(context) {

    fun interface OnCheckedChangeListener {
        fun onCheckedChanged(isChecked: Boolean)
    }

    private var textState by mutableStateOf("")
    private var checkedState by mutableStateOf(false)
    private var enabledState by mutableStateOf(true)
    private var listener: OnCheckedChangeListener? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun setText(text: CharSequence?) {
        textState = text?.toString().orEmpty()
    }

    fun setChecked(checked: Boolean) {
        checkedState = checked
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        enabledState = enabled
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            CheckboxRowItem(
                text = textState,
                checked = checkedState,
                enabled = enabledState,
                verticalPadding = verticalPaddingDp.dp,
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
private fun CheckboxRowItemPreview() {
    FairEmailTheme {
        CheckboxRowItem(
            text = "Example item",
            checked = true,
            verticalPadding = 6.dp,
            onCheckedChange = {}
        )
    }
}
