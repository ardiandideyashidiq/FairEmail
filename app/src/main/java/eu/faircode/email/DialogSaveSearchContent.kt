package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun DialogSaveSearchContent(
    nameState: MutableState<String>,
    orderState: MutableState<String>,
    colorState: MutableState<Int?>,
    onColorPickClick: () -> Unit,
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
                painter = painterResource(id = R.drawable.twotone_search_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp)
            )
            Text(
                text = stringResource(id = R.string.title_save_search),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = nameState.value,
            onValueChange = { nameState.value = it },
            label = { Text(text = stringResource(id = R.string.title_save_search_name)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        OutlinedTextField(
            value = orderState.value,
            onValueChange = { orderState.value = it.filter { ch -> ch.isDigit() }.take(9) },
            label = { Text(text = stringResource(id = R.string.title_save_search_order)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        val selectedColor = colorState.value
        Row(
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable(onClick = onColorPickClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (selectedColor == null || selectedColor == 0) Color.Transparent else Color(selectedColor),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            )
            Text(
                text = stringResource(id = R.string.title_save_search_color),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

class DialogSaveSearchView(
    context: Context,
    initialName: String?,
    initialOrder: String?,
    initialColor: Int?,
    onColorPickClick: Runnable
) {
    val nameState: MutableState<String> = mutableStateOf(initialName ?: "")
    val orderState: MutableState<String> = mutableStateOf(initialOrder ?: "")
    val colorState: MutableState<Int?> = mutableStateOf(initialColor)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogSaveSearchContent(
                    nameState = nameState,
                    orderState = orderState,
                    colorState = colorState,
                    onColorPickClick = { onColorPickClick.run() }
                )
            }
        }
    }

    val name: String
        get() = nameState.value

    val order: String
        get() = orderState.value

    var color: Int
        get() = colorState.value ?: 0
        set(value) {
            colorState.value = value
        }
}

@Preview(showBackground = true)
@Composable
fun DialogSaveSearchPreview() {
    FairEmailTheme {
        DialogSaveSearchContent(
            nameState = remember { mutableStateOf("Starred receipts") },
            orderState = remember { mutableStateOf("1") },
            colorState = remember { mutableStateOf(0xFF4285F4.toInt()) },
            onColorPickClick = {}
        )
    }
}
