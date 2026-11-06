package eu.faircode.email

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2026 by Marcel Bokhorst (M66B)
*/

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogForwardRawContent(
    progress: Int,
    max: Int,
    remaining: String,
    autoConfirm: Boolean,
    onAutoConfirmChange: (Boolean) -> Unit,
    noInternet: Boolean,
    onOptionClick: () -> Unit,
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
                painter = painterResource(id = R.drawable.twotone_attachment_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = R.string.title_raw_send),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = stringResource(id = R.string.title_eml_remark),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )

        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable(onClick = onOptionClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.title_eml_option),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(id = R.drawable.twotone_settings_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }

        val progressRatio = if (max > 0) (progress.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = { progressRatio },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )

        Text(
            text = remaining,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 12.dp)
        )

        if (noInternet) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.twotone_warning_24),
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.title_no_internet),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF9800)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clickable { onAutoConfirmChange(!autoConfirm) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = autoConfirm,
                onCheckedChange = onAutoConfirmChange
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.title_auto_confirm),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

class DialogForwardRawView(
    context: Context,
    initialMax: Int = 0,
    initialRemaining: String = "",
    initialAutoConfirm: Boolean = false,
    val onOptionClick: Runnable? = null,
    val onAutoConfirmChange: OnAutoConfirmChangeListener? = null
) {
    val progressState: MutableState<Int> = mutableStateOf(0)
    val maxState: MutableState<Int> = mutableStateOf(initialMax)
    val remainingState: MutableState<String> = mutableStateOf(initialRemaining)
    val autoConfirmState: MutableState<Boolean> = mutableStateOf(initialAutoConfirm)
    val noInternetState: MutableState<Boolean> = mutableStateOf(false)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogForwardRawContent(
                    progress = progressState.value,
                    max = maxState.value,
                    remaining = remainingState.value,
                    autoConfirm = autoConfirmState.value,
                    onAutoConfirmChange = { checked ->
                        autoConfirmState.value = checked
                        onAutoConfirmChange?.onAutoConfirmChanged(checked)
                    },
                    noInternet = noInternetState.value,
                    onOptionClick = { onOptionClick?.run() }
                )
            }
        }
    }

    fun setProgress(progress: Int) {
        progressState.value = progress
    }

    fun setProgress(progress: Int, max: Int) {
        progressState.value = progress
        maxState.value = max
    }

    fun setMax(max: Int) {
        maxState.value = max
    }

    fun setRemaining(remaining: String) {
        remainingState.value = remaining
    }

    fun setNoInternet(noInternet: Boolean) {
        noInternetState.value = noInternet
    }

    val isAutoConfirm: Boolean
        get() = autoConfirmState.value

    fun setAutoConfirm(autoConfirm: Boolean) {
        autoConfirmState.value = autoConfirm
    }

    fun interface OnAutoConfirmChangeListener {
        fun onAutoConfirmChanged(checked: Boolean)
    }
}

@Preview(showBackground = true)
@Composable
fun DialogForwardRawPreview() {
    FairEmailTheme {
        DialogForwardRawContent(
            progress = 2,
            max = 5,
            remaining = "Downloaded 2 of 5",
            autoConfirm = false,
            onAutoConfirmChange = {},
            noInternet = true,
            onOptionClick = {}
        )
    }
}
