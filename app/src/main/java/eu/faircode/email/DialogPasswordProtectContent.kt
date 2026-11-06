package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogPasswordProtectContent(
    pass1State: MutableState<String>,
    pass2State: MutableState<String>,
    onInfoClick: () -> Unit,
    onValidationChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val p1 = pass1State.value
    val p2 = pass2State.value
    val isMatch = p1.isNotEmpty() && p1 == p2
    val isMismatch = p2.isNotEmpty() && p1 != p2

    onValidationChanged(isMatch)

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
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_style_protect),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = p1,
            onValueChange = { pass1State.value = it },
            label = { Text(text = stringResource(id = R.string.title_setup_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = p2,
            onValueChange = { pass2State.value = it },
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
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        Button(
            onClick = onInfoClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(id = R.string.title_info))
                Icon(
                    painter = painterResource(id = R.drawable.twotone_info_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 6.dp)
                )
            }
        }
    }
}

class DialogPasswordProtectView(
    context: Context,
    onInfoClick: Runnable,
    val onValidStateListener: OnValidChangedListener
) {
    val pass1State: MutableState<String> = mutableStateOf("")
    val pass2State: MutableState<String> = mutableStateOf("")

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogPasswordProtectContent(
                    pass1State = pass1State,
                    pass2State = pass2State,
                    onInfoClick = { onInfoClick.run() },
                    onValidationChanged = { valid -> onValidStateListener.onChanged(valid) }
                )
            }
        }
    }

    val password: String
        get() = pass1State.value

    fun interface OnValidChangedListener {
        fun onChanged(valid: Boolean)
    }
}

@Preview(showBackground = true)
@Composable
fun DialogPasswordProtectPreview() {
    FairEmailTheme {
        DialogPasswordProtectContent(
            pass1State = remember { mutableStateOf("secret") },
            pass2State = remember { mutableStateOf("secret") },
            onInfoClick = {},
            onValidationChanged = {}
        )
    }
}
