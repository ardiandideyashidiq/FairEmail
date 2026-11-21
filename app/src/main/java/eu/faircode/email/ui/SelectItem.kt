package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun SelectItem(
    stripeColor: Color,
    title: String,
    subtitle: String? = null,
    verticalPadding: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 6.dp, top = 3.dp, bottom = 3.dp)
                .width(6.dp)
                .fillMaxHeight()
                .background(stripeColor)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(
                    top = verticalPadding,
                    bottom = if (subtitle == null) verticalPadding else 0.dp
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp, bottom = verticalPadding)
                )
            }
        }
    }
}

class SelectItemComposeView(context: Context) : AbstractComposeView(context) {
    private var stripeColorState by mutableStateOf(Color.Transparent)
    private var titleState by mutableStateOf("")
    private var subtitleState by mutableStateOf<String?>(null)
    private var verticalPaddingDpState by mutableIntStateOf(12)

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun bind(
        colorArgb: Int?,
        title: String,
        subtitle: String?,
        verticalPaddingDp: Int
    ) {
        stripeColorState = colorArgb?.let { Color(it) } ?: Color.Transparent
        titleState = title
        subtitleState = subtitle
        verticalPaddingDpState = verticalPaddingDp
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            SelectItem(
                stripeColor = stripeColorState,
                title = titleState,
                subtitle = subtitleState,
                verticalPadding = verticalPaddingDpState.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectItemPreview() {
    FairEmailTheme {
        SelectItem(
            stripeColor = Color(0xFF039BE5),
            title = "Personal",
            subtitle = "user@gmail.com"
        )
    }
}
