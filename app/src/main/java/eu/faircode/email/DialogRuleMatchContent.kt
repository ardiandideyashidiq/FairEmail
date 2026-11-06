package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class RuleMatchUiItem(
    val id: Long,
    val time: String,
    val subject: String?
)

@Composable
fun DialogRuleMatchContent(
    messages: List<RuleMatchUiItem>,
    isLoading: Boolean,
    errorMessage: String?,
    onExecuteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            )
        } else if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        } else if (messages.isEmpty()) {
            Text(
                text = stringResource(id = R.string.title_rule_no_matches),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(12.dp)
                ) {
                    items(messages, key = { it.id }) { item ->
                        RuleMatchItem(
                            time = item.time,
                            subject = item.subject
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onExecuteClick,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(id = R.string.title_rule_execute))
                        Icon(
                            painter = painterResource(id = R.drawable.twotone_play_arrow_24),
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

class DialogRuleMatchView(
    context: Context,
    val onExecuteClick: Runnable
) {
    val messagesList = mutableStateListOf<RuleMatchUiItem>()
    val loadingState: MutableState<Boolean> = mutableStateOf(true)
    val errorState: MutableState<String?> = mutableStateOf(null)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogRuleMatchContent(
                    messages = messagesList,
                    isLoading = loadingState.value,
                    errorMessage = errorState.value,
                    onExecuteClick = { onExecuteClick.run() }
                )
            }
        }
    }

    fun setMessages(messages: List<RuleMatchUiItem>?) {
        messagesList.clear()
        if (messages != null) {
            messagesList.addAll(messages)
        }
        errorState.value = null
        loadingState.value = false
    }

    fun setError(message: String) {
        errorState.value = message
        loadingState.value = false
    }

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }
}

@Preview(showBackground = true)
@Composable
fun DialogRuleMatchPreview() {
    FairEmailTheme {
        DialogRuleMatchContent(
            messages = listOf(
                RuleMatchUiItem(1L, "Mon 10:15", "Meeting minutes"),
                RuleMatchUiItem(2L, "Tue 14:30", "Weekly report")
            ),
            isLoading = false,
            errorMessage = null,
            onExecuteClick = {}
        )
    }
}
