package eu.faircode.email

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.Spanned
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogTranslateContent(
    languages: List<DeepL.Language>,
    selectedLanguageIndexState: MutableState<Int>,
    isLoading: Boolean,
    isTranslateAllEnabled: Boolean,
    onTranslateAllClick: () -> Unit,
    onLanguageSelected: (Int) -> Unit,
    textViewFactory: (Context) -> TextView,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val expanded = remember { mutableStateOf(false) }

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
                painter = painterResource(id = R.drawable.twotone_translate_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_translate_tap),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onTranslateAllClick,
                enabled = isTranslateAllEnabled
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.twotone_select_all_24),
                    contentDescription = stringResource(id = R.string.title_select_all),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            val selectedLanguage = languages.getOrNull(selectedLanguageIndexState.value)
            OutlinedTextField(
                value = selectedLanguage?.name ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                languages.forEachIndexed { index, language ->
                    DropdownMenuItem(
                        text = { Text(text = language.name ?: "") },
                        onClick = {
                            selectedLanguageIndexState.value = index
                            onLanguageSelected(index)
                            expanded.value = false
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .heightIn(min = 60.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx -> textViewFactory(ctx) },
                modifier = Modifier.fillMaxWidth()
            )
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

class DialogTranslateView(
    context: Context,
    val languages: List<DeepL.Language>,
    initialLanguageIndex: Int,
    val onTranslateAllClick: Runnable,
    val onLanguageSelected: (Int) -> Unit
) {
    val selectedLanguageIndexState: MutableState<Int> = mutableIntStateOf(
        if (languages.isNotEmpty()) initialLanguageIndex.coerceIn(0, languages.size - 1) else 0
    )
    val loadingState: MutableState<Boolean> = mutableStateOf(false)
    val translateAllEnabledState: MutableState<Boolean> = mutableStateOf(true)

    val textView: AppCompatTextView = AppCompatTextView(context).apply {
        setTextIsSelectable(true)
        isFocusable = false
        minHeight = (60 * context.resources.displayMetrics.density).toInt()
        setTextAppearance(context, androidx.appcompat.R.style.TextAppearance_AppCompat_Medium)
        setTextColor(Helper.resolveColor(context, android.R.attr.textColorPrimary))
    }

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogTranslateContent(
                    languages = languages,
                    selectedLanguageIndexState = selectedLanguageIndexState,
                    isLoading = loadingState.value,
                    isTranslateAllEnabled = translateAllEnabledState.value,
                    onTranslateAllClick = { onTranslateAllClick.run() },
                    onLanguageSelected = onLanguageSelected,
                    textViewFactory = { textView }
                )
            }
        }
    }

    val selectedLanguage: DeepL.Language?
        get() = languages.getOrNull(selectedLanguageIndexState.value)

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }

    fun setTranslateAllEnabled(enabled: Boolean) {
        translateAllEnabledState.value = enabled
    }
}

@Preview(showBackground = true)
@Composable
fun DialogTranslatePreview() {
    FairEmailTheme {
        DialogTranslateContent(
            languages = emptyList(),
            selectedLanguageIndexState = remember { mutableIntStateOf(0) },
            isLoading = false,
            isTranslateAllEnabled = true,
            onTranslateAllClick = {},
            onLanguageSelected = {},
            textViewFactory = { ctx -> TextView(ctx).apply { text = "Sample translation preview" } }
        )
    }
}
