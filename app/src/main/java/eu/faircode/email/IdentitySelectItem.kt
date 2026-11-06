package eu.faircode.email

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun IdentitySelectItem(
    name: String,
    email: String,
    color: Color?,
    isCompactPadding: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalPadding = if (isCompactPadding) 6.dp else 12.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (color != null && color != Color.Transparent) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(36.dp)
                    .background(color)
            )
        } else {
            Box(modifier = Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = name.ifEmpty { "-" },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = email,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

object IdentitySelectItemBinder {
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
        email: String,
        colorInt: Int?,
        isCompactPadding: Boolean,
        onClick: Runnable
    ) {
        val color = colorInt?.let { Color(it) }
        composeView.setContent {
            FairEmailTheme {
                IdentitySelectItem(
                    name = name,
                    email = email,
                    color = color,
                    isCompactPadding = isCompactPadding,
                    onClick = { onClick.run() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IdentitySelectItemPreview() {
    FairEmailTheme {
        IdentitySelectItem(
            name = "John Doe",
            email = "john.doe@example.com",
            color = Color(0xFF4285F4),
            isCompactPadding = false,
            onClick = {}
        )
    }
}
