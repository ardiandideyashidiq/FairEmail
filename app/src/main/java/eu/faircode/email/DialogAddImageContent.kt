package eu.faircode.email

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAddImageContent(
    @StringRes titleRes: Int,
    @DrawableRes typeIconRes: Int,
    addInlineState: MutableState<Boolean>,
    hasSettings: Boolean,
    onSettingsClick: () -> Unit,
    resizeImagesState: MutableState<Boolean>,
    onInfoClick: () -> Unit,
    resizeNames: List<String>,
    resizeValues: List<Int>,
    resizeIndexState: MutableState<Int>,
    resizeWidthOnlyState: MutableState<Boolean>,
    privacyImagesState: MutableState<Boolean>,
    notAgainState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val expanded = remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        // Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            Icon(
                painter = painterResource(typeIconRes),
                contentDescription = stringResource(titleRes),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Radio Group with optional Settings button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { addInlineState.value = true }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = addInlineState.value,
                        onClick = { addInlineState.value = true }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.title_add_image_inline),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { addInlineState.value = false }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = !addInlineState.value,
                        onClick = { addInlineState.value = false }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.title_add_image_attach),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (hasSettings) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.twotone_settings_24),
                        contentDescription = stringResource(R.string.title_setup),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resize Checkbox & Info button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { resizeImagesState.value = !resizeImagesState.value }
            ) {
                Checkbox(
                    checked = resizeImagesState.value,
                    onCheckedChange = { resizeImagesState.value = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.title_add_image_resize),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.twotone_info_24),
                    contentDescription = stringResource(R.string.title_info),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Resize Spinner & pixel indicator
        val isResizeEnabled = resizeImagesState.value
        val currentIndex = resizeIndexState.value
        val currentName = resizeNames.getOrNull(currentIndex) ?: ""
        val currentPixels = resizeValues.getOrNull(currentIndex)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded.value && isResizeEnabled,
                    onExpandedChange = { if (isResizeEnabled) expanded.value = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = {},
                        readOnly = true,
                        enabled = isResizeEnabled,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value && isResizeEnabled)
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded.value && isResizeEnabled,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        resizeNames.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(text = name) },
                                onClick = {
                                    resizeIndexState.value = index
                                    expanded.value = false
                                }
                            )
                        }
                    }
                }

                if (currentPixels != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.title_add_resize_pixels, currentPixels),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Resize width only CheckBox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isResizeEnabled) {
                        if (isResizeEnabled) {
                            resizeWidthOnlyState.value = !resizeWidthOnlyState.value
                        }
                    }
            ) {
                Checkbox(
                    checked = resizeWidthOnlyState.value,
                    onCheckedChange = { resizeWidthOnlyState.value = it },
                    enabled = isResizeEnabled
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.title_add_image_resize_width_only),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Resize remark
        Text(
            text = stringResource(R.string.title_add_image_resize_remark, "JPEG, PNG, WebP"),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Privacy CheckBox & remark
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { privacyImagesState.value = !privacyImagesState.value }
        ) {
            Checkbox(
                checked = privacyImagesState.value,
                onCheckedChange = { privacyImagesState.value = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.title_add_image_privacy),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Text(
            text = stringResource(R.string.title_add_image_privacy_remark),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(top = 6.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Do not ask again CheckBox & hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { notAgainState.value = !notAgainState.value }
        ) {
            Checkbox(
                checked = notAgainState.value,
                onCheckedChange = { notAgainState.value = it }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.title_no_ask_again),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (notAgainState.value) {
            Text(
                text = stringResource(R.string.title_dialog_hint),
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Select remark
        Text(
            text = stringResource(R.string.title_add_image_select_remark),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic
        )
    }
}

class DialogAddImageView(
    context: Context,
    @StringRes titleRes: Int,
    @DrawableRes typeIconRes: Int,
    initialAddInline: Boolean,
    hasSettings: Boolean,
    onSettingsClick: Runnable?,
    initialResizeImages: Boolean,
    onInfoClick: Runnable?,
    val resizeNames: List<String>,
    val resizeValues: List<Int>,
    initialResizeIndex: Int,
    initialResizeWidthOnly: Boolean,
    initialPrivacyImages: Boolean,
    initialNotAgain: Boolean,
    onAddInlineChanged: ((Boolean) -> Unit)? = null,
    onResizeImagesChanged: ((Boolean) -> Unit)? = null,
    onResizeIndexChanged: ((Int) -> Unit)? = null,
    onResizeWidthOnlyChanged: ((Boolean) -> Unit)? = null,
    onPrivacyImagesChanged: ((Boolean) -> Unit)? = null,
    onNotAgainChanged: ((Boolean) -> Unit)? = null
) {
    val addInlineState: MutableState<Boolean> = mutableStateOf(initialAddInline)
    val resizeImagesState: MutableState<Boolean> = mutableStateOf(initialResizeImages)
    val resizeIndexState: MutableState<Int> = mutableIntStateOf(initialResizeIndex)
    val resizeWidthOnlyState: MutableState<Boolean> = mutableStateOf(initialResizeWidthOnly)
    val privacyImagesState: MutableState<Boolean> = mutableStateOf(initialPrivacyImages)
    val notAgainState: MutableState<Boolean> = mutableStateOf(initialNotAgain)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                DialogAddImageContent(
                    titleRes = titleRes,
                    typeIconRes = typeIconRes,
                    addInlineState = addInlineState,
                    hasSettings = hasSettings,
                    onSettingsClick = { onSettingsClick?.run() },
                    resizeImagesState = resizeImagesState,
                    onInfoClick = { onInfoClick?.run() },
                    resizeNames = resizeNames,
                    resizeValues = resizeValues,
                    resizeIndexState = resizeIndexState,
                    resizeWidthOnlyState = resizeWidthOnlyState,
                    privacyImagesState = privacyImagesState,
                    notAgainState = notAgainState
                )
            }
        }
    }

    val isAddInline: Boolean get() = addInlineState.value
    val isResizeImages: Boolean get() = resizeImagesState.value
    val selectedResize: Int get() = resizeValues.getOrNull(resizeIndexState.value) ?: 0
    val isResizeWidthOnly: Boolean get() = resizeWidthOnlyState.value
    val isPrivacyImages: Boolean get() = privacyImagesState.value
    val isNotAgain: Boolean get() = notAgainState.value
}

@Preview(showBackground = true)
@Composable
fun DialogAddImagePreview() {
    FairEmailTheme {
        DialogAddImageContent(
            titleRes = R.string.title_add_image,
            typeIconRes = R.drawable.twotone_image_24,
            addInlineState = remember { mutableStateOf(true) },
            hasSettings = true,
            onSettingsClick = {},
            resizeImagesState = remember { mutableStateOf(true) },
            onInfoClick = {},
            resizeNames = listOf("Tiny", "Small", "Medium", "Large"),
            resizeValues = listOf(480, 720, 1080, 1440),
            resizeIndexState = remember { mutableIntStateOf(2) },
            resizeWidthOnlyState = remember { mutableStateOf(false) },
            privacyImagesState = remember { mutableStateOf(false) },
            notAgainState = remember { mutableStateOf(false) }
        )
    }
}
