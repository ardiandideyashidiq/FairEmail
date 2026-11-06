package eu.faircode.email

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderItem(
    title: String,
    subtitle: String?,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isDragging) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.twotone_drag_handle_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp)
        )
        if (!subtitle.isNullOrEmpty()) {
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

object OrderItemBinder {
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
    @JvmOverloads
    fun bind(
        composeView: ComposeView,
        title: String,
        subtitle: String?,
        isDragging: Boolean = false
    ) {
        composeView.setContent {
            FairEmailTheme {
                OrderItem(
                    title = title,
                    subtitle = subtitle,
                    isDragging = isDragging
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OrderItemPreview() {
    FairEmailTheme {
        OrderItem(
            title = "Unified Inbox",
            subtitle = "Inbox",
            isDragging = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OrderItemDraggingPreview() {
    FairEmailTheme {
        OrderItem(
            title = "Trash",
            subtitle = "System",
            isDragging = true
        )
    }
}
