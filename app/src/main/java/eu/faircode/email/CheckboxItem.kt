package eu.faircode.email

import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CheckboxItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

object CheckboxItemBinder {
    @JvmStatic
    fun createComposeView(parent: ViewGroup): ComposeView {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
            )
        }
        return composeView
    }

    @JvmStatic
    fun bind(
        composeView: ComposeView,
        text: String,
        checked: Boolean,
        listener: OnCheckboxChangedListener
    ) {
        composeView.setContent {
            FairEmailTheme {
                val state = remember(checked) { mutableStateOf(checked) }
                CheckboxItem(
                    text = text,
                    checked = state.value,
                    onCheckedChange = { newValue ->
                        state.value = newValue
                        listener.onChanged(newValue)
                    }
                )
            }
        }
    }

    fun interface OnCheckboxChangedListener {
        fun onChanged(checked: Boolean)
    }
}

@Preview(showBackground = true)
@Composable
fun CheckboxItemPreview() {
    FairEmailTheme {
        CheckboxItem(
            text = "Enable Spamhaus DBL",
            checked = true,
            onCheckedChange = {}
        )
    }
}
