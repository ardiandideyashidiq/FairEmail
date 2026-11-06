package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun UnsupportedDeviceContent(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(id = R.string.app_unsupported),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF9800),
            modifier = Modifier.padding(top = 12.dp)
        )

        Button(
            onClick = onContinueClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.app_continue))
                Icon(
                    painter = painterResource(id = R.drawable.twotone_check_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 6.dp)
                )
            }
        }
    }
}

class UnsupportedDeviceView(context: Context, onContinueClick: Runnable) {
    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                UnsupportedDeviceContent(onContinueClick = { onContinueClick.run() })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnsupportedDevicePreview() {
    FairEmailTheme {
        UnsupportedDeviceContent(onContinueClick = {})
    }
}
