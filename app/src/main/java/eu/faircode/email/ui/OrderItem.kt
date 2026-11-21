package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun OrderItem(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.twotone_drag_handle_24),
            contentDescription = null
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

class OrderItemComposeView(context: Context) : AbstractComposeView(context) {
    private var titleState by mutableStateOf("")
    private var subtitleState by mutableStateOf("")

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(title: String, subtitle: String) {
        titleState = title
        subtitleState = subtitle
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            OrderItem(
                title = titleState,
                subtitle = subtitleState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderItemPreview() {
    FairEmailTheme {
        OrderItem(
            title = "Inbox",
            subtitle = "Personal"
        )
    }
}
