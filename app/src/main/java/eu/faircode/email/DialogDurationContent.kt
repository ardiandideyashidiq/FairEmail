package eu.faircode.email

import android.content.Context
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun DialogDurationContent(
    durationTextState: MutableState<String>,
    durationColorState: MutableState<Int>,
    showSnoozeDozeState: MutableState<Boolean>,
    on1HourClick: () -> Unit,
    on1DayClick: () -> Unit,
    onMoreClick: (View) -> Unit,
    onSnoozeDozeClick: () -> Unit,
    timePickerFactory: (Context) -> TimePicker,
    datePickerFactory: (Context) -> DatePicker,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = on1HourClick) {
                Text(text = stringResource(id = R.string.title_1hour))
            }
            Button(onClick = on1DayClick) {
                Text(text = stringResource(id = R.string.title_1day))
            }
            AndroidView(
                factory = { ctx ->
                    android.widget.Button(ctx, null, android.R.attr.buttonStyleSmall).apply {
                        setText(R.string.title_more_overflow)
                        setOnClickListener { onMoreClick(this) }
                    }
                }
            )
        }

        Text(
            text = durationTextState.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(durationColorState.value),
            modifier = Modifier.padding(top = 12.dp)
        )

        AndroidView(
            factory = { ctx -> timePickerFactory(ctx) },
            modifier = Modifier.padding(top = 8.dp)
        )

        AndroidView(
            factory = { ctx -> datePickerFactory(ctx) },
            modifier = Modifier.padding(top = 8.dp)
        )

        if (showSnoozeDozeState.value) {
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable { onSnoozeDozeClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.title_snooze_doze),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    painter = painterResource(id = R.drawable.twotone_settings_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 6.dp)
                )
            }
        }
    }
}

class DialogDurationView(
    val context: Context,
    initialDurationText: String,
    initialDurationColorInt: Int,
    initialShowSnoozeDoze: Boolean,
    val on1HourClick: Runnable,
    val on1DayClick: Runnable,
    val onMoreClick: MoreClickListener,
    val onSnoozeDozeClick: Runnable,
    val timePickerFactory: TimePickerFactory,
    val datePickerFactory: DatePickerFactory
) {
    fun interface MoreClickListener {
        fun onMoreClick(anchor: View)
    }

    fun interface TimePickerFactory {
        fun createTimePicker(context: Context): TimePicker
    }

    fun interface DatePickerFactory {
        fun createDatePicker(context: Context): DatePicker
    }

    val durationTextState: MutableState<String> = mutableStateOf(initialDurationText)
    val durationColorState: MutableState<Int> = mutableStateOf(initialDurationColorInt)
    val showSnoozeDozeState: MutableState<Boolean> = mutableStateOf(initialShowSnoozeDoze)

    fun updateDuration(text: String, colorInt: Int) {
        durationTextState.value = text
        durationColorState.value = colorInt
    }

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        setContent {
            FairEmailTheme {
                DialogDurationContent(
                    durationTextState = durationTextState,
                    durationColorState = durationColorState,
                    showSnoozeDozeState = showSnoozeDozeState,
                    on1HourClick = { on1HourClick.run() },
                    on1DayClick = { on1DayClick.run() },
                    onMoreClick = { view -> onMoreClick.onMoreClick(view) },
                    onSnoozeDozeClick = { onSnoozeDozeClick.run() },
                    timePickerFactory = { ctx -> timePickerFactory.createTimePicker(ctx) },
                    datePickerFactory = { ctx -> datePickerFactory.createDatePicker(ctx) }
                )
            }
        }
    }
}
