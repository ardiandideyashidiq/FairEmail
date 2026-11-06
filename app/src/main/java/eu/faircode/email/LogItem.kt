package eu.faircode.email

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LogItem(
    time: String,
    data: CharSequence,
    dataColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        SelectionContainer(
            modifier = Modifier
                .padding(start = 3.dp)
                .weight(1f)
        ) {
            Text(
                text = data.toString(),
                fontSize = 12.sp,
                color = dataColor ?: MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

object LogItemBinder {
    @JvmStatic
    fun createComposeView(parent: android.view.ViewGroup): ComposeView {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
        time: String,
        data: CharSequence,
        colorInt: Int?
    ) {
        val dataColor = colorInt?.let { Color(it) }
        composeView.setContent {
            FairEmailTheme {
                LogItem(
                    time = time,
                    data = data,
                    dataColor = dataColor
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogItemPreview() {
    FairEmailTheme {
        LogItem(
            time = "12:34:56",
            data = "IMAP connection established successfully",
            dataColor = Color(0xFF00C000)
        )
    }
}
