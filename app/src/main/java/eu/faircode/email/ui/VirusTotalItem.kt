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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun VirusTotalItem(
    name: String,
    category: String,
    malicious: Boolean,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
        )
        Text(
            text = category,
            color = if (malicious) FairEmailTheme.colors.warning else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (malicious) FontWeight.Bold else FontWeight.Normal
            ),
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

class VirusTotalItemComposeView(context: Context) : AbstractComposeView(context) {
    private var nameState by mutableStateOf("")
    private var categoryState by mutableStateOf("")
    private var maliciousState by mutableStateOf(false)

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(name: String, category: String, malicious: Boolean) {
        nameState = name
        categoryState = category
        maliciousState = malicious
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            VirusTotalItem(
                name = nameState,
                category = categoryState,
                malicious = maliciousState
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VirusTotalItemPreview() {
    FairEmailTheme {
        VirusTotalItem(
            name = "ClamAV",
            category = "malicious",
            malicious = true
        )
    }
}
