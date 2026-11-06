package eu.faircode.email

import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VirusTotalItem(
    name: String,
    category: String,
    isMalicious: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = category,
            fontSize = 14.sp,
            fontWeight = if (isMalicious) FontWeight.Bold else FontWeight.Normal,
            color = if (isMalicious) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

object VirusTotalItemBinder {
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
        name: String,
        category: String,
        isMalicious: Boolean
    ) {
        composeView.setContent {
            FairEmailTheme {
                VirusTotalItem(
                    name = name,
                    category = category,
                    isMalicious = isMalicious
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VirusTotalItemPreview() {
    FairEmailTheme {
        VirusTotalItem(
            name = "ClamAV",
            category = "malicious",
            isMalicious = true
        )
    }
}
