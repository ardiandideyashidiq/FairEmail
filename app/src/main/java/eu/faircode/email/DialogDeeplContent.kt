package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogDeeplContent(
    keyState: MutableState<String>,
    formalState: MutableState<Boolean>,
    formalLanguages: String,
    smallState: MutableState<Boolean>,
    replaceState: MutableState<Boolean>,
    highlightState: MutableState<Boolean>,
    htmlState: MutableState<Boolean>,
    usageState: MutableState<String?>,
    onInfoClick: (() -> Unit)?,
    onUsageClick: (() -> Unit)?,
    onPrivacyClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        // Title row: icon + title + info button
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
                    .padding(end = 6.dp)
            )
            Text(
                text = "DeepL",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (onInfoClick != null) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.twotone_info_24),
                        contentDescription = stringResource(id = R.string.title_info),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // API Key field with show/hide password toggle
        OutlinedTextField(
            value = keyState.value,
            onValueChange = { keyState.value = it },
            label = { Text(text = stringResource(id = R.string.title_translate_key)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val icon = if (passwordVisible) {
                    painterResource(id = R.drawable.twotone_visibility_off_24)
                } else {
                    painterResource(id = R.drawable.twotone_visibility_24)
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        // Formal checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { formalState.value = !formalState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = formalState.value,
                onCheckedChange = { formalState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_translate_formal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Formal languages hint
        if (formalLanguages.isNotEmpty()) {
            Text(
                text = formalLanguages,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        // Small checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable {
                    val newVal = !smallState.value
                    smallState.value = newVal
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = smallState.value,
                onCheckedChange = { smallState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_translate_small),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Replace checkbox (disabled when small is checked)
        val replaceEnabled = !smallState.value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 12.dp)
                .clickable(enabled = replaceEnabled) {
                    replaceState.value = !replaceState.value
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = replaceState.value,
                onCheckedChange = { replaceState.value = it },
                enabled = replaceEnabled
            )
            Text(
                text = stringResource(id = R.string.title_translate_replace),
                style = MaterialTheme.typography.bodyMedium,
                color = if (replaceEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Highlight checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { highlightState.value = !highlightState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = highlightState.value,
                onCheckedChange = { highlightState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_translate_highlight),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // HTML checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { htmlState.value = !htmlState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = htmlState.value,
                onCheckedChange = { htmlState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_translate_html),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // HTML hint
        Text(
            text = stringResource(id = R.string.title_translate_html_hint),
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        // Usage text (visible when loaded)
        usageState.value?.let { usageText ->
            Text(
                text = usageText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .clickable(enabled = onUsageClick != null) { onUsageClick?.invoke() }
            )
        }

        // Privacy Policy link
        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable(enabled = onPrivacyClick != null) { onPrivacyClick?.invoke() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.title_privacy_policy),
                style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline),
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                painter = painterResource(id = R.drawable.twotone_open_in_new_12),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(12.dp)
                    .padding(start = 6.dp)
            )
        }
    }
}

class DialogDeeplView(
    context: Context,
    initialKey: String?,
    initialFormal: Boolean,
    formalLanguages: String,
    initialSmall: Boolean,
    initialReplace: Boolean,
    initialHighlight: Boolean,
    initialHtml: Boolean,
    onInfoClick: Runnable?,
    onUsageClick: Runnable?,
    onPrivacyClick: Runnable?
) {
    val keyState: MutableState<String> = mutableStateOf(initialKey ?: "")
    val formalState: MutableState<Boolean> = mutableStateOf(initialFormal)
    val smallState: MutableState<Boolean> = mutableStateOf(initialSmall)
    val replaceState: MutableState<Boolean> = mutableStateOf(initialReplace)
    val highlightState: MutableState<Boolean> = mutableStateOf(initialHighlight)
    val htmlState: MutableState<Boolean> = mutableStateOf(initialHtml)
    val usageState: MutableState<String?> = mutableStateOf(null)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogDeeplContent(
                    keyState = keyState,
                    formalState = formalState,
                    formalLanguages = formalLanguages,
                    smallState = smallState,
                    replaceState = replaceState,
                    highlightState = highlightState,
                    htmlState = htmlState,
                    usageState = usageState,
                    onInfoClick = onInfoClick?.let { r -> { r.run() } },
                    onUsageClick = onUsageClick?.let { r -> { r.run() } },
                    onPrivacyClick = onPrivacyClick?.let { r -> { r.run() } }
                )
            }
        }
    }

    fun setUsage(usage: String?) {
        usageState.value = usage
    }

    val key: String
        get() = keyState.value.trim()

    val isFormal: Boolean
        get() = formalState.value

    val isSmall: Boolean
        get() = smallState.value

    val isReplace: Boolean
        get() = replaceState.value

    val isHighlight: Boolean
        get() = highlightState.value

    val isHtml: Boolean
        get() = htmlState.value
}

@Preview(showBackground = true)
@Composable
fun DialogDeeplPreview() {
    FairEmailTheme {
        DialogDeeplContent(
            keyState = remember { mutableStateOf("abc-123-deepl-key") },
            formalState = remember { mutableStateOf(true) },
            formalLanguages = "German, French, Spanish, Italian, Dutch, Polish, Portuguese, Russian, Japanese",
            smallState = remember { mutableStateOf(false) },
            replaceState = remember { mutableStateOf(false) },
            highlightState = remember { mutableStateOf(true) },
            htmlState = remember { mutableStateOf(false) },
            usageState = remember { mutableStateOf("12,345 / 500,000 characters used (2%) +15") },
            onInfoClick = {},
            onUsageClick = {},
            onPrivacyClick = {}
        )
    }
}
