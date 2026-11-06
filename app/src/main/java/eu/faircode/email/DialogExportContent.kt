package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogExportContent(
    password1State: MutableState<String>,
    password2State: MutableState<String>,
    debug: Boolean = false,
    onValidationChanged: ((Boolean) -> Unit)? = null,
    onDone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val p1 = password1State.value
    val p2 = password2State.value
    val isValid = (debug || p1.isNotEmpty()) && p1 == p2
    val isMismatch = p2.isNotEmpty() && p1 != p2

    var pass1Visible by remember { mutableStateOf(false) }
    var pass2Visible by remember { mutableStateOf(false) }

    LaunchedEffect(isValid) {
        onValidationChanged?.invoke(isValid)
    }

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
                painter = painterResource(id = R.drawable.twotone_archive_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_setup_export),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value = p1,
            onValueChange = { password1State.value = it },
            label = { Text(text = stringResource(id = R.string.title_setup_password)) },
            singleLine = true,
            visualTransformation = if (pass1Visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            trailingIcon = {
                val icon = if (pass1Visible) {
                    painterResource(id = R.drawable.twotone_visibility_off_24)
                } else {
                    painterResource(id = R.drawable.twotone_visibility_24)
                }
                IconButton(onClick = { pass1Visible = !pass1Visible }) {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = p2,
            onValueChange = { password2State.value = it },
            label = {
                Text(
                    text = if (isMismatch) {
                        stringResource(id = R.string.title_setup_password_different)
                    } else {
                        stringResource(id = R.string.title_setup_password_repeat)
                    }
                )
            },
            isError = isMismatch,
            singleLine = true,
            visualTransformation = if (pass2Visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone?.invoke() }
            ),
            trailingIcon = {
                val icon = if (pass2Visible) {
                    painterResource(id = R.drawable.twotone_visibility_off_24)
                } else {
                    painterResource(id = R.drawable.twotone_visibility_24)
                }
                IconButton(onClick = { pass2Visible = !pass2Visible }) {
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
    }
}

class DialogExportView @JvmOverloads constructor(
    context: Context,
    initialPassword1: String? = null,
    initialPassword2: String? = null,
    val debug: Boolean = false,
    val onValidStateListener: OnValidChangedListener? = null,
    val onDoneListener: Runnable? = null
) {
    val password1State: MutableState<String> = mutableStateOf(initialPassword1 ?: "")
    val password2State: MutableState<String> = mutableStateOf(initialPassword2 ?: "")

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            FairEmailTheme {
                DialogExportContent(
                    password1State = password1State,
                    password2State = password2State,
                    debug = debug,
                    onValidationChanged = { valid -> onValidStateListener?.onChanged(valid) },
                    onDone = { onDoneListener?.run() }
                )
            }
        }
    }

    var password1: String
        get() = password1State.value
        set(value) {
            password1State.value = value
        }

    var password2: String
        get() = password2State.value
        set(value) {
            password2State.value = value
        }

    val isValid: Boolean
        get() = (debug || password1State.value.isNotEmpty()) && password1State.value == password2State.value

    fun interface OnValidChangedListener {
        fun onChanged(valid: Boolean)
    }
}

@Preview(showBackground = true)
@Composable
fun DialogExportPreview() {
    FairEmailTheme {
        DialogExportContent(
            password1State = remember { mutableStateOf("") },
            password2State = remember { mutableStateOf("") }
        )
    }
}
