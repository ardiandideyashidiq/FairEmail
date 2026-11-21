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

    @JvmStatic
    fun createLogItemView(context: Context): LogItemComposeView {
        return LogItemComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createDarkDialogView(context: Context, onIssue: View.OnClickListener): View {
        return ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                FairEmailTheme {
                    DarkDialogContent(onIssue = { onIssue.onClick(this) })
                }
            }
        }
    }

    @JvmStatic
    fun createAttachmentDeleteView(context: Context): AttachmentDeleteComposeView {
        return AttachmentDeleteComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createImportProgressView(context: Context): ImportProgressComposeView {
        return ImportProgressComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createPinSetDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_pin_24,
            titleRes = eu.faircode.email.R.string.title_advanced_pin,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createPinAskDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_pin_24,
            titleRes = eu.faircode.email.R.string.app_name,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword,
            isPassword = true
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setLeadingIconRes(eu.faircode.email.R.drawable.twotone_hourglass_top_24)
        }
    }

    @JvmStatic
    fun createKeywordAddDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_label_important_24,
            titleRes = eu.faircode.email.R.string.title_add_keyword
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createEditSubjectDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_edit_24,
            titleRes = eu.faircode.email.R.string.title_edit_subject,
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
            autoCorrectEnabled = true
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createShortcutLabelDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_edit_24,
            titleRes = eu.faircode.email.R.string.title_pin_label,
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createPromptDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_sticky_note_2_24,
            titleRes = eu.faircode.email.R.string.title_advanced_summarize_prompt,
            singleLine = false,
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences,
            autoCorrectEnabled = true
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    @JvmStatic
    fun createEditNameDialogView(context: Context): SingleInputDialogComposeView {
        return SingleInputDialogComposeView(
            context = context,
            titleIconRes = eu.faircode.email.R.drawable.twotone_edit_24,
            titleRes = eu.faircode.email.R.string.title_advanced_edit_name,
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
            checkboxLabelRes = eu.faircode.email.R.string.title_primary
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }
}
