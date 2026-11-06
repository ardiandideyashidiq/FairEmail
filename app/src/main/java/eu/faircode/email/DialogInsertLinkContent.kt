package eu.faircode.email

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun DialogInsertLinkContent(
    linkState: MutableState<String>,
    titleState: MutableState<String>,
    imageState: MutableState<Boolean>,
    uploadEnabled: Boolean,
    dlimitState: MutableState<Int>,
    tlimitState: MutableState<Int>,
    isLoadingMetadataState: MutableState<Boolean>,
    isUploadingState: MutableState<Boolean>,
    uploadProgressState: MutableState<Int>,
    onMetadataClick: (() -> Unit)? = null,
    onUploadClick: (() -> Unit)? = null,
    onDLimitChanged: ((Int) -> Unit)? = null,
    onTLimitChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val uri = remember(linkState.value) {
        try {
            Uri.parse(linkState.value)
        } catch (_: Throwable) {
            null
        }
    }
    val isHyperLink = uri != null && UriHelper.isHyperLink(uri)
    val isSecure = uri != null && UriHelper.isSecure(uri)
    val isInsecure = isHyperLink && !isSecure

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_insert_link_45_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.title_style_link),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedTextField(
            value = linkState.value,
            onValueChange = { linkState.value = it },
            label = { Text(text = stringResource(id = R.string.title_style_link_address)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        if (isInsecure) {
            Text(
                text = stringResource(id = R.string.title_insecure_link),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .clickable { imageState.value = !imageState.value },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = imageState.value,
                onCheckedChange = { imageState.value = it }
            )
            Text(
                text = stringResource(id = R.string.title_style_link_image),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        OutlinedTextField(
            value = titleState.value,
            onValueChange = { titleState.value = it },
            label = { Text(text = stringResource(id = R.string.title_style_link_title)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Button(
                onClick = { onMetadataClick?.invoke() },
                enabled = isSecure && !isLoadingMetadataState.value
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.title_style_link_metadata))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.twotone_get_app_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (isLoadingMetadataState.value) {
                Spacer(modifier = Modifier.width(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Text(
            text = stringResource(id = R.string.title_style_link_metadata_remark),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        if (uploadEnabled) {
            Button(
                onClick = { onUploadClick?.invoke() },
                enabled = !isUploadingState.value,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.title_style_link_send))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.twotone_file_upload_24),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isUploadingState.value) {
                LinearProgressIndicator(
                    progress = { (uploadProgressState.value / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            val dlimit = dlimitState.value + 1
            Text(
                text = stringResource(id = R.string.title_style_link_send_dlimit, dlimit.toString()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp)
            )
            Slider(
                value = dlimitState.value.toFloat(),
                onValueChange = {
                    val intVal = it.roundToInt().coerceIn(0, 99)
                    if (intVal != dlimitState.value) {
                        dlimitState.value = intVal
                        onDLimitChanged?.invoke(intVal)
                    }
                },
                valueRange = 0f..99f,
                enabled = !isUploadingState.value,
                modifier = Modifier.fillMaxWidth()
            )

            val tlimitRaw = tlimitState.value + 1
            val tlimitText = if (tlimitRaw < 24) {
                stringResource(
                    id = R.string.title_style_link_send_tlimit,
                    pluralStringResource(id = R.plurals.title_hours, count = tlimitRaw, tlimitRaw)
                )
            } else {
                val days = tlimitRaw - 24 + 1
                stringResource(
                    id = R.string.title_style_link_send_tlimit,
                    pluralStringResource(id = R.plurals.title_days, count = days, days)
                )
            }
            Text(
                text = tlimitText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp)
            )
            Slider(
                value = tlimitState.value.toFloat(),
                onValueChange = {
                    val intVal = it.roundToInt().coerceIn(0, 52)
                    if (intVal != tlimitState.value) {
                        tlimitState.value = intVal
                        onTLimitChanged?.invoke(intVal)
                    }
                },
                valueRange = 0f..52f,
                enabled = !isUploadingState.value,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

class DialogInsertLinkView @JvmOverloads constructor(
    context: Context,
    initialLink: String? = null,
    initialTitle: String? = null,
    initialImage: Boolean = false,
    val uploadEnabled: Boolean = false,
    initialDLimit: Int = 0,
    initialTLimit: Int = 0,
    private val onMetadataClick: Runnable? = null,
    private val onUploadClick: Runnable? = null,
    private val onDLimitChanged: OnLimitChangedListener? = null,
    private val onTLimitChanged: OnLimitChangedListener? = null
) {
    val linkState: MutableState<String> = mutableStateOf(initialLink ?: "")
    val titleState: MutableState<String> = mutableStateOf(initialTitle ?: "")
    val imageState: MutableState<Boolean> = mutableStateOf(initialImage)
    val dlimitState: MutableState<Int> = mutableStateOf(initialDLimit.coerceIn(0, 99))
    val tlimitState: MutableState<Int> = mutableStateOf(initialTLimit.coerceIn(0, 52))
    val isLoadingMetadataState: MutableState<Boolean> = mutableStateOf(false)
    val isUploadingState: MutableState<Boolean> = mutableStateOf(false)
    val uploadProgressState: MutableState<Int> = mutableStateOf(0)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            FairEmailTheme {
                DialogInsertLinkContent(
                    linkState = linkState,
                    titleState = titleState,
                    imageState = imageState,
                    uploadEnabled = uploadEnabled,
                    dlimitState = dlimitState,
                    tlimitState = tlimitState,
                    isLoadingMetadataState = isLoadingMetadataState,
                    isUploadingState = isUploadingState,
                    uploadProgressState = uploadProgressState,
                    onMetadataClick = onMetadataClick?.let { { it.run() } },
                    onUploadClick = onUploadClick?.let { { it.run() } },
                    onDLimitChanged = onDLimitChanged?.let { { limit -> it.onChanged(limit) } },
                    onTLimitChanged = onTLimitChanged?.let { { limit -> it.onChanged(limit) } }
                )
            }
        }
    }

    val link: String
        get() = linkState.value

    fun setLink(link: String?) {
        linkState.value = link ?: ""
    }

    val title: String
        get() = titleState.value

    fun setTitle(title: String?) {
        titleState.value = title ?: ""
    }

    val isImage: Boolean
        get() = imageState.value

    fun setImage(image: Boolean) {
        imageState.value = image
    }

    val dlimit: Int
        get() = dlimitState.value

    fun setDLimit(limit: Int) {
        dlimitState.value = limit.coerceIn(0, 99)
    }

    val tlimit: Int
        get() = tlimitState.value

    fun setTLimit(limit: Int) {
        tlimitState.value = limit.coerceIn(0, 52)
    }

    val isLoadingMetadata: Boolean
        get() = isLoadingMetadataState.value

    fun setLoadingMetadata(loading: Boolean) {
        isLoadingMetadataState.value = loading
    }

    val isUploading: Boolean
        get() = isUploadingState.value

    fun setUploading(uploading: Boolean) {
        isUploadingState.value = uploading
    }

    val uploadProgress: Int
        get() = uploadProgressState.value

    fun setUploadProgress(progress: Int) {
        uploadProgressState.value = progress
    }

    fun interface OnLimitChangedListener {
        fun onChanged(progress: Int)
    }
}

@Preview(showBackground = true)
@Composable
fun DialogInsertLinkPreview() {
    FairEmailTheme {
        DialogInsertLinkContent(
            linkState = remember { mutableStateOf("https://example.com") },
            titleState = remember { mutableStateOf("Example Site") },
            imageState = remember { mutableStateOf(false) },
            uploadEnabled = true,
            dlimitState = remember { mutableStateOf(0) },
            tlimitState = remember { mutableStateOf(0) },
            isLoadingMetadataState = remember { mutableStateOf(false) },
            isUploadingState = remember { mutableStateOf(false) },
            uploadProgressState = remember { mutableStateOf(25) }
        )
    }
}
