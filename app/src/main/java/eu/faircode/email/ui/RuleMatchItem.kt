package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun RuleMatchItem(
    time: String,
    subject: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = subject,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        )
    }
}

class RuleMatchItemComposeView(context: Context) : AbstractComposeView(context) {
    private var timeState by mutableStateOf("")
    private var subjectState by mutableStateOf("")

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(time: String, subject: String) {
        timeState = time
        subjectState = subject
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            RuleMatchItem(
                time = timeState,
                subject = subjectState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RuleMatchItemPreview() {
    FairEmailTheme {
        RuleMatchItem(
            time = "Mon 12:34",
            subject = "Invoice #12345"
        )
    }
}
