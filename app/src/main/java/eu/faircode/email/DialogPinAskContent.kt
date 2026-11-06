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
fun DialogPinAskContent(
    pinState: MutableState<String>,
    hintState: MutableState<String>,
    enabledState: MutableState<Boolean>,
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
                painter = painterResource(id = R.drawable.twotone_pin_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = pinState.value,
            onValueChange = { pinState.value = it },
            enabled = enabledState.value,
            label = {
                Text(
                    text = hintState.value.ifEmpty {
                        stringResource(id = R.string.title_advanced_pin)
                    }
                )
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }
}

class DialogPinAskView(context: Context) {
    val pinState: MutableState<String> = mutableStateOf("")
    val hintState: MutableState<String> = mutableStateOf("")
    val enabledState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogPinAskContent(
                    pinState = pinState,
                    hintState = hintState,
                    enabledState = enabledState
                )
            }
        }
    }

    val pin: String
        get() = pinState.value

    fun setHint(hint: String) {
        hintState.value = hint
    }

    fun setEnabled(enabled: Boolean) {
        enabledState.value = enabled
    }
}

@Preview(showBackground = true)
@Composable
fun DialogPinAskPreview() {
    FairEmailTheme {
        DialogPinAskContent(
            pinState = remember { mutableStateOf("") },
            hintState = remember { mutableStateOf("12:00:00") },
            enabledState = remember { mutableStateOf(false) }
        )
    }
}
