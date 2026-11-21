package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun LogItem(
    time: String,
    data: String,
    dataColor: Color? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
        )
        SelectionContainer(
            modifier = Modifier
                .weight(1f)
                .padding(start = 3.dp)
        ) {
            Text(
                text = data,
                color = dataColor ?: LocalContentColor.current,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
            )
        }
    }
}

class LogItemComposeView(context: Context) : AbstractComposeView(context) {
    private var timeState by mutableStateOf("")
    private var dataState by mutableStateOf("")
    private var colorState by mutableStateOf<Color?>(null)

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(time: String, data: String, colorArgb: Int?) {
        timeState = time
        dataState = data
        colorState = colorArgb?.let { Color(it) }
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            LogItem(
                time = timeState,
                data = dataState,
                dataColor = colorState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LogItemPreview() {
    FairEmailTheme {
        LogItem(
            time = "12:34:56",
            data = "Synchronize completed",
            dataColor = null
        )
    }
}
