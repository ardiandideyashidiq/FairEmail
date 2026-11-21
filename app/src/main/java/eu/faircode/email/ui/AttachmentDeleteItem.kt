package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun AttachmentDeleteItem(
    name: String,
    info: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CheckboxRowItem(
            text = name,
            checked = checked,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
        if (info.isNotEmpty()) {
            Text(
                text = info,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

class AttachmentDeleteComposeView(context: Context) : AbstractComposeView(context) {
    private var nameState by mutableStateOf("")
    private var infoState by mutableStateOf("")
    private var checkedState by mutableStateOf(false)
    private var enabledState by mutableStateOf(true)
    private var listener: CheckboxRowComposeView.OnCheckedChangeListener? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(name: String, info: String, checked: Boolean, enabled: Boolean) {
        nameState = name
        infoState = info
        checkedState = checked
        enabledState = enabled
    }

    fun setOnCheckedChangeListener(listener: CheckboxRowComposeView.OnCheckedChangeListener?) {
        this.listener = listener
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            AttachmentDeleteItem(
                name = nameState,
                info = infoState,
                checked = checkedState,
                enabled = enabledState,
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
private fun AttachmentDeleteItemPreview() {
    FairEmailTheme {
        AttachmentDeleteItem(
            name = "document.pdf",
            info = "application/pdf 1.2 MB",
            checked = true,
            enabled = true,
            onCheckedChange = {}
        )
    }
}
