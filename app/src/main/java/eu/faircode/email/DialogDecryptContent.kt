package eu.faircode.email

import android.content.Context
import android.text.Spanned
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun DialogDecryptContent(
    passwordState: MutableState<String>,
    inputVisible: Boolean,
    inputEnabled: Boolean,
    decryptEnabled: Boolean,
    errorText: String?,
    errorDetailText: String?,
    contentVisible: Boolean,
    onPasswordChanged: (String) -> Unit,
    onDecryptClick: () -> Unit,
    contentTextViewFactory: (Context) -> TextView,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var passwordVisible by remember { mutableStateOf(false) }

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
                painter = painterResource(id = R.drawable.twotone_lock_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 6.dp)
            )
            Text(
                text = stringResource(id = R.string.title_style_protected),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (inputVisible) {
            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                    onPasswordChanged(it)
                },
                enabled = inputEnabled,
                label = { Text(text = stringResource(id = R.string.title_setup_password)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (decryptEnabled && inputEnabled) {
                            onDecryptClick()
                        }
                    }
                ),
                trailingIcon = {
                    val image = if (passwordVisible) {
                        painterResource(id = R.drawable.twotone_visibility_24)
                    } else {
                        painterResource(id = R.drawable.twotone_visibility_off_24)
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = image,
                            contentDescription = null
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )

            Button(
                onClick = onDecryptClick,
                enabled = decryptEnabled && inputEnabled,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(text = stringResource(id = R.string.title_decrypt))
                Icon(
                    painter = painterResource(id = R.drawable.twotone_lock_open_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 6.dp)
                )
            }
        }

        if (contentVisible) {
            AndroidView(
                factory = { ctx -> contentTextViewFactory(ctx) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }

        if (!errorText.isNullOrEmpty()) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            )
        }

        if (!errorDetailText.isNullOrEmpty()) {
            Text(
                text = errorDetailText,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }
    }
}

class DialogDecryptView(
    val context: Context,
    initialPassword: String?,
    val onPasswordChangedListener: (String) -> Unit,
    val onDecryptClickListener: () -> Unit,
    val contentTextView: TextView
) {
    val passwordState: MutableState<String> = mutableStateOf(initialPassword ?: "")
    var inputVisibleState = mutableStateOf(true)
    var inputEnabledState = mutableStateOf(true)
    var decryptEnabledState = mutableStateOf(!initialPassword.isNullOrEmpty())
    var errorTextState = mutableStateOf<String?>(null)
    var errorDetailTextState = mutableStateOf<String?>(null)
    var contentVisibleState = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            FairEmailTheme {
                DialogDecryptContent(
                    passwordState = passwordState,
                    inputVisible = inputVisibleState.value,
                    inputEnabled = inputEnabledState.value,
                    decryptEnabled = decryptEnabledState.value,
                    errorText = errorTextState.value,
                    errorDetailText = errorDetailTextState.value,
                    contentVisible = contentVisibleState.value,
                    onPasswordChanged = { pwd ->
                        decryptEnabledState.value = pwd.isNotEmpty()
                        onPasswordChangedListener(pwd)
                    },
                    onDecryptClick = { onDecryptClickListener() },
                    contentTextViewFactory = { contentTextView }
                )
            }
        }
    }

    fun getPassword(): String = passwordState.value

    fun setPassword(password: String) {
        passwordState.value = password
        decryptEnabledState.value = password.isNotEmpty()
    }

    fun setInputEnabled(enabled: Boolean) {
        inputEnabledState.value = enabled
    }

    fun setDecryptEnabled(enabled: Boolean) {
        decryptEnabledState.value = enabled
    }

    fun setInputVisible(visible: Boolean) {
        inputVisibleState.value = visible
    }

    fun setContentVisible(visible: Boolean) {
        contentVisibleState.value = visible
    }

    fun setError(error: String?, detail: String?) {
        errorTextState.value = error
        errorDetailTextState.value = detail
    }

    fun performDecrypt() {
        onDecryptClickListener()
    }
}

@Preview(showBackground = true)
@Composable
fun DialogDecryptPreview() {
    FairEmailTheme {
        DialogDecryptContent(
            passwordState = remember { mutableStateOf("secret") },
            inputVisible = true,
            inputEnabled = true,
            decryptEnabled = true,
            errorText = null,
            errorDetailText = null,
            contentVisible = false,
            onPasswordChanged = {},
            onDecryptClick = {},
            contentTextViewFactory = { TextView(it) }
        )
    }
}
