package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp

@Composable
fun DialogMarkdownContent(
    markdownText: CharSequence?,
    isLoading: Boolean,
    showNotAgain: Boolean,
    notAgainState: MutableState<Boolean>,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.twotone_close_24),
                            contentDescription = stringResource(id = android.R.string.cancel),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                SelectionContainer(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = markdownText?.toString() ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (showNotAgain) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clickable { notAgainState.value = !notAgainState.value },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = notAgainState.value,
                            onCheckedChange = { notAgainState.value = it }
                        )
                        Text(
                            text = stringResource(id = R.string.title_no_show_again),
                            fontSize = 14.sp,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

class DialogMarkdownView(
    context: Context,
    showNotAgain: Boolean,
    val onNotAgainListener: OnNotAgainChangedListener?,
    onCloseClick: Runnable
) {
    val markdownState: MutableState<CharSequence?> = mutableStateOf(null)
    val loadingState: MutableState<Boolean> = mutableStateOf(true)
    val notAgainState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                val state = notAgainState.value
                LaunchedEffect(state) {
                    onNotAgainListener?.onChanged(state)
                }
                DialogMarkdownContent(
                    markdownText = markdownState.value,
                    isLoading = loadingState.value,
                    showNotAgain = showNotAgain,
                    notAgainState = notAgainState,
                    onCloseClick = { onCloseClick.run() }
                )
            }
        }
    }

    fun setMarkdown(markdown: CharSequence?) {
        markdownState.value = markdown
        loadingState.value = false
    }

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }

    fun interface OnNotAgainChangedListener {
        fun onChanged(checked: Boolean)
    }
}

@Preview(showBackground = true)
@Composable
fun DialogMarkdownPreview() {
    FairEmailTheme {
        DialogMarkdownContent(
            markdownText = "### Changelog\n\n- Migrated to Jetpack Compose\n- Improved performance\n- Fixed minor issues",
            isLoading = false,
            showNotAgain = true,
            notAgainState = remember { mutableStateOf(false) },
            onCloseClick = {}
        )
    }
}
