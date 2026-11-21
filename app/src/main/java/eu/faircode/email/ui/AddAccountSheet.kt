package eu.faircode.email.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.faircode.email.GmailAuthHelper
import eu.faircode.email.GmailAuthHelper.findActivity
import eu.faircode.email.Log
import eu.faircode.email.R
import eu.faircode.email.ui.theme.ExpressiveMotion
import kotlinx.coroutines.launch

internal enum class AddAccountMode {
    OAuth,
    AndroidAccount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddAccountSheet(
    initialMode: AddAccountMode,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var displayName by remember { mutableStateOf("") }
    var emailHint by remember { mutableStateOf("") }
    var updateExisting by remember { mutableStateOf(true) }
    var isWorking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val oauthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) {
            isWorking = false
            if (result.resultCode != Activity.RESULT_CANCELED) {
                errorMessage = "OAuth authorization was cancelled or returned no data."
            }
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            isWorking = true
            errorMessage = null
            try {
                val addedEmail = GmailAuthHelper.completeGmailOAuth(
                    context = context,
                    data = data,
                    displayName = displayName,
                    emailHint = emailHint,
                    updateExisting = updateExisting
                )
                onSuccess("Gmail account connected: $addedEmail")
            } catch (ex: Throwable) {
                Log.e(ex)
                errorMessage = GmailAuthHelper.formatError(ex)
            } finally {
                isWorking = false
            }
        }
    }

    val androidAccountLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode != Activity.RESULT_OK || data == null) {
            isWorking = false
            if (result.resultCode != Activity.RESULT_CANCELED) {
                errorMessage = context.getString(R.string.title_no_account)
            }
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            isWorking = true
            errorMessage = null
            try {
                val addedEmail = GmailAuthHelper.completeAndroidGmailAccount(
                    context = context,
                    activity = activity,
                    data = data,
                    displayName = displayName,
                    updateExisting = updateExisting
                )
                onSuccess("Gmail account connected: $addedEmail")
            } catch (ex: Throwable) {
                Log.e(ex)
                errorMessage = GmailAuthHelper.formatError(ex)
            } finally {
                isWorking = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isWorking) onDismiss() },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.twotone_account_circle_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Add Gmail Account",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (initialMode == AddAccountMode.OAuth)
                            "Recommended: Google OAuth 2.0 sign-in"
                        else
                            "Use an existing Google account on this device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Your name (optional)") },
                singleLine = true,
                enabled = !isWorking,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emailHint,
                onValueChange = { emailHint = it },
                label = { Text("Gmail address (optional hint for OAuth)") },
                singleLine = true,
                enabled = !isWorking,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = updateExisting,
                    onCheckedChange = { updateExisting = it },
                    enabled = !isWorking
                )
                Text(
                    text = "Update existing account if already added",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(ExpressiveMotion.effectsDefault()) +
                    expandVertically(ExpressiveMotion.spatialFast()),
                exit = fadeOut(ExpressiveMotion.effectsDefault()) +
                    shrinkVertically(ExpressiveMotion.spatialFast())
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (isWorking) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Connecting & verifying Gmail...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    errorMessage = null
                    isWorking = true
                    try {
                        val intent = GmailAuthHelper.buildGmailOAuthIntent(context, emailHint)
                        oauthLauncher.launch(intent)
                    } catch (ex: Throwable) {
                        Log.e(ex)
                        isWorking = false
                        errorMessage = GmailAuthHelper.formatError(ex)
                    }
                },
                enabled = !isWorking,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Sign in with Google (OAuth)")
            }

            FilledTonalButton(
                onClick = {
                    errorMessage = null
                    isWorking = true
                    try {
                        val intent = GmailAuthHelper.buildAndroidAccountPickerIntent()
                        androidAccountLauncher.launch(intent)
                    } catch (ex: Throwable) {
                        Log.e(ex)
                        isWorking = false
                        errorMessage = GmailAuthHelper.formatError(ex)
                    }
                },
                enabled = !isWorking,
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Use Android Google Account")
            }
        }
    }
}
