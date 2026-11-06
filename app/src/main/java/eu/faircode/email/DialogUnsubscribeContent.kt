package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogUnsubscribeContent(
    sender: String,
    uri: String,
    isLoading: Boolean,
    hasInternet: Boolean,
    onUnsubscribeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_unsubscribe_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_unsubscribe),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = sender,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 16.dp)
        )

        SelectionContainer {
            Text(
                text = uri,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onUnsubscribeClick,
                enabled = !isLoading && hasInternet
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.title_unsubscribe))
                    Icon(
                        painter = painterResource(id = R.drawable.twotone_unsubscribe_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 6.dp)
                    )
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 12.dp)
                )
            }
        }

        if (!hasInternet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.twotone_warning_24),
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 6.dp)
                )
                Text(
                    text = stringResource(id = R.string.title_no_internet),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

class DialogUnsubscribeView(
    context: Context,
    sender: String,
    uri: String,
    onUnsubscribeClick: Runnable
) {
    val loadingState: MutableState<Boolean> = mutableStateOf(false)
    val internetState: MutableState<Boolean> = mutableStateOf(true)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogUnsubscribeContent(
                    sender = sender,
                    uri = uri,
                    isLoading = loadingState.value,
                    hasInternet = internetState.value,
                    onUnsubscribeClick = { onUnsubscribeClick.run() }
                )
            }
        }
    }

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }

    fun setHasInternet(hasInternet: Boolean) {
        internetState.value = hasInternet
    }
}

@Preview(showBackground = true)
@Composable
fun DialogUnsubscribePreview() {
    FairEmailTheme {
        DialogUnsubscribeContent(
            sender = "newsletter@example.com",
            uri = "https://example.com/unsubscribe?id=123",
            isLoading = false,
            hasInternet = true,
            onUnsubscribeClick = {}
        )
    }
}
