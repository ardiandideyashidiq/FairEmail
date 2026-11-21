package eu.faircode.email.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun SingleInputDialogContent(
    @DrawableRes titleIconRes: Int,
    @StringRes titleRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String? = null,
    @DrawableRes leadingIconRes: Int = 0,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    autoCorrectEnabled: Boolean = false,
    onDone: () -> Unit = {},
    focusRequester: FocusRequester = remember { FocusRequester() },
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(titleIconRes),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            placeholder = hint?.let { { Text(text = it) } },
            leadingIcon = if (leadingIconRes != 0) {
                {
                    Icon(
                        painter = painterResource(leadingIconRes),
                        contentDescription = null
                    )
                }
            } else null,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization,
                autoCorrectEnabled = autoCorrectEnabled,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .focusRequester(focusRequester)
        )
    }
}

class SingleInputDialogComposeView(
    context: Context,
    @DrawableRes private val titleIconRes: Int,
    @StringRes private val titleRes: Int,
    private val keyboardType: KeyboardType = KeyboardType.Text,
    private val isPassword: Boolean = false,
    private val capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    private val autoCorrectEnabled: Boolean = false
) : AbstractComposeView(context) {

    private var textState by mutableStateOf("")
    private var hintState by mutableStateOf<String?>(null)
    private var leadingIconResState by mutableIntStateOf(0)
    private var enabledState by mutableStateOf(true)
    private var focusTrigger by mutableIntStateOf(0)
    private var onDoneListener: Runnable? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun getText(): String = textState

    fun setText(text: CharSequence?) {
        textState = text?.toString().orEmpty()
    }

    fun setHint(hint: CharSequence?) {
        hintState = hint?.toString()
    }

    fun setHint(@StringRes hintRes: Int) {
        hintState = context.getString(hintRes)
    }

    fun setLeadingIconRes(@DrawableRes iconRes: Int) {
        leadingIconResState = iconRes
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        enabledState = enabled
    }

    fun setOnDoneListener(listener: Runnable?) {
        onDoneListener = listener
    }

    fun requestInputFocus() {
        focusTrigger++
    }

    @Composable
    override fun Content() {
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(focusTrigger) {
            if (focusTrigger > 0 && enabledState) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }

        FairEmailTheme {
            SingleInputDialogContent(
                titleIconRes = titleIconRes,
                titleRes = titleRes,
                value = textState,
                onValueChange = { textState = it },
                hint = hintState,
                leadingIconRes = leadingIconResState,
                enabled = enabledState,
                isPassword = isPassword,
                keyboardType = keyboardType,
                capitalization = capitalization,
                autoCorrectEnabled = autoCorrectEnabled,
                onDone = { onDoneListener?.run() },
                focusRequester = focusRequester
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SingleInputDialogContentPreview() {
    FairEmailTheme {
        SingleInputDialogContent(
            titleIconRes = R.drawable.twotone_edit_24,
            titleRes = R.string.title_edit_subject,
            value = "Re: Hello",
            onValueChange = {}
        )
    }
}
