package eu.faircode.email

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogSummarizeContent(
    caption: String?,
    from: String?,
    subject: String?,
    summary: CharSequence?,
    elapsed: String?,
    error: String?,
    isLoading: Boolean,
    summaryTextSize: TextUnit = 16.sp,
    onCopyClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    SelectionContainer {
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
                    painter = painterResource(id = R.drawable.twotone_smart_toy_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp)
                )
                Text(
                    text = caption ?: stringResource(id = R.string.title_summarize),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (!from.isNullOrEmpty()) {
                Text(
                    text = from,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (!subject.isNullOrEmpty()) {
                Text(
                    text = subject,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (!summary.isNullOrEmpty()) {
                Text(
                    text = summary.toString(),
                    fontSize = summaryTextSize,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onCopyClick?.invoke() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.twotone_file_copy_24),
                            contentDescription = stringResource(id = R.string.title_clipboard_copy),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!elapsed.isNullOrEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.twotone_hourglass_bottom_12),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 3.dp)
                            )
                            Text(
                                text = elapsed,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (!error.isNullOrEmpty()) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

class DialogSummarizeView(
    context: Context,
    caption: String?,
    from: String?,
    subject: String?,
    summaryTextSizePx: Float = 0f
) {
    val captionState: MutableState<String?> = mutableStateOf(caption)
    val fromState: MutableState<String?> = mutableStateOf(from)
    val subjectState: MutableState<String?> = mutableStateOf(subject)
    val summaryState: MutableState<CharSequence?> = mutableStateOf(null)
    val elapsedState: MutableState<String?> = mutableStateOf(null)
    val errorState: MutableState<String?> = mutableStateOf(null)
    val loadingState: MutableState<Boolean> = mutableStateOf(true)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        val density = context.resources.displayMetrics.scaledDensity
        val textSizeSp = if (summaryTextSizePx > 0 && density > 0) {
            (summaryTextSizePx / density).sp
        } else {
            16.sp
        }

        setContent {
            FairEmailTheme {
                val ctx = LocalContext.current
                DialogSummarizeContent(
                    caption = captionState.value,
                    from = fromState.value,
                    subject = subjectState.value,
                    summary = summaryState.value,
                    elapsed = elapsedState.value,
                    error = errorState.value,
                    isLoading = loadingState.value,
                    summaryTextSize = textSizeSp,
                    onCopyClick = {
                        val text = summaryState.value
                        if (!text.isNullOrEmpty()) {
                            val clipboard = Helper.getSystemService(ctx, ClipboardManager::class.java)
                            if (clipboard != null) {
                                val clip = ClipData.newPlainText(ctx.getString(R.string.app_name), text)
                                clipboard.setPrimaryClip(clip)
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                    ToastEx.makeText(ctx, R.string.title_clipboard_copied, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    fun setCaption(caption: String?) {
        captionState.value = caption
    }

    fun setSummary(summary: CharSequence?, elapsed: String?) {
        summaryState.value = summary
        elapsedState.value = elapsed
        loadingState.value = false
    }

    fun setError(error: String?) {
        errorState.value = error
        loadingState.value = false
    }

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
        if (loading) {
            summaryState.value = null
            elapsedState.value = null
            errorState.value = null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialogSummarizePreview() {
    FairEmailTheme {
        DialogSummarizeContent(
            caption = "Summarize message",
            from = "sender@example.com",
            subject = "Project Update",
            summary = "Here is the summarized content of the email highlighting key takeaways and next steps.",
            elapsed = "1.23s",
            error = null,
            isLoading = false
        )
    }
}
