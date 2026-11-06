package eu.faircode.email

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActivityAnswerContent(
    answers: List<EntityAnswer>,
    isLoading: Boolean,
    onAnswerClick: (EntityAnswer) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(answers, key = { it.id ?: it.uuid }) { answer ->
                    Text(
                        text = answer.name,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAnswerClick(answer) }
                            .padding(16.dp)
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

class ActivityAnswerView(
    context: Context,
    val onAnswerClick: OnAnswerClickListener
) {
    val answersList = mutableStateListOf<EntityAnswer>()
    val loadingState: MutableState<Boolean> = mutableStateOf(true)

    val composeView: ComposeView = ComposeView(context).apply {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
        )
        setContent {
            FairEmailTheme {
                ActivityAnswerContent(
                    answers = answersList,
                    isLoading = loadingState.value,
                    onAnswerClick = { answer -> onAnswerClick.onAnswerClick(answer) }
                )
            }
        }
    }

    fun setAnswers(answers: List<EntityAnswer>?) {
        answersList.clear()
        if (answers != null)
            answersList.addAll(answers)
        loadingState.value = false
    }

    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }

    fun interface OnAnswerClickListener {
        fun onAnswerClick(answer: EntityAnswer)
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityAnswerPreview() {
    FairEmailTheme {
        ActivityAnswerContent(
            answers = listOf(
                EntityAnswer().apply { id = 1L; name = "Thanks for your email" },
                EntityAnswer().apply { id = 2L; name = "Received, will review soon" }
            ),
            isLoading = false,
            onAnswerClick = {}
        )
    }
}
