package eu.faircode.email.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import eu.faircode.email.ui.theme.FairEmailTheme

object ComposeContentHelper {
    @JvmStatic
    fun setUnsupportedContent(activity: ComponentActivity, onContinue: View.OnClickListener) {
        activity.setContent {
            FairEmailTheme(darkTheme = false) {
                UnsupportedScreen(onContinue = { onContinue.onClick(null) })
            }
        }
    }

    @JvmStatic
    fun setSpaceContent(
        activity: ComponentActivity,
        remainingText: String,
        requiredText: String,
        onFix: View.OnClickListener,
        onContinue: View.OnClickListener
    ) {
        activity.setContent {
            FairEmailTheme(darkTheme = false) {
                SpaceScreen(
                    remainingText = remainingText,
                    requiredText = requiredText,
                    onFix = { onFix.onClick(null) },
                    onContinue = { onContinue.onClick(null) }
                )
            }
        }
    }

    @JvmStatic
    fun createMessagePlaceholderView(context: Context): View {
        return ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                FairEmailTheme {
                    MessagePlaceholderItem()
                }
            }
        }
    }

    @JvmStatic
    fun createCheckboxRowView(context: Context, verticalPaddingDp: Int): CheckboxRowComposeView {
        return CheckboxRowComposeView(context, verticalPaddingDp).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
