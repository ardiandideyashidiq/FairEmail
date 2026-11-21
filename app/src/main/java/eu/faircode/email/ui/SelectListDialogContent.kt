package eu.faircode.email.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.FairEmailTheme

data class SelectOption(
    val colorArgb: Int?,
    val title: String,
    val subtitle: String? = null
)

@Composable
fun SelectListDialogContent(
    loading: Boolean,
    options: List<SelectOption>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        } else {
            val vpad = if (options.size > 10) 6.dp else 12.dp
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .padding(top = 12.dp)
            ) {
                itemsIndexed(options) { index, item ->
                    SelectItem(
                        stripeColor = item.colorArgb?.let { Color(it) } ?: Color.Transparent,
                        title = item.title,
                        subtitle = item.subtitle,
                        verticalPadding = vpad,
                        modifier = Modifier.clickable { onSelect(index) }
                    )
                }
            }
        }
    }
}

class SelectListDialogComposeView(context: Context) : AbstractComposeView(context) {
    fun interface OnItemSelectedListener {
        fun onSelected(index: Int)
    }

    private var loadingState by mutableStateOf(true)
    private var optionsState by mutableStateOf<List<SelectOption>>(emptyList())
    private var listener: OnItemSelectedListener? = null

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    fun setLoading(loading: Boolean) {
        loadingState = loading
    }

    fun setOptions(options: List<SelectOption>, listener: OnItemSelectedListener?) {
        optionsState = options
        this.listener = listener
    }

    @Composable
    override fun Content() {
        FairEmailTheme {
            SelectListDialogContent(
                loading = loadingState,
                options = optionsState,
                onSelect = { index -> listener?.onSelected(index) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectListDialogContentPreview() {
    FairEmailTheme {
        SelectListDialogContent(
            loading = false,
            options = listOf(
                SelectOption(0xFF039BE5.toInt(), "Gmail", "user@gmail.com"),
                SelectOption(null, "Work", "work@gmail.com")
            ),
            onSelect = {}
        )
    }
}
