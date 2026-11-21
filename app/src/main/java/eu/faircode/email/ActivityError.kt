package eu.faircode.email

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import eu.faircode.email.ui.theme.FairEmailTheme

internal class ActivityError : ActivityBase() {
    companion object {
        const val PI_ERROR = 1
        const val PI_ALERT = 2
    }

    private var errorTitle by mutableStateOf("")
    private var errorMessage by mutableStateOf("")
    private var provider by mutableStateOf<String?>(null)
    private var accountId by mutableStateOf(-1L)
    private var protocol by mutableStateOf(-1)
    private var authType by mutableStateOf(-1)
    private var host by mutableStateOf<String?>(null)
    private var faqId by mutableStateOf(-1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadFromIntent(intent)
        setContent {
            FairEmailTheme {
                ErrorScreen(
                    title = errorTitle,
                    message = errorMessage,
                    accountId = accountId,
                    protocol = protocol,
                    authType = authType,
                    host = host,
                    provider = provider,
                    faqId = faqId,
                    onBack = { finish() },
                    onReauthorize = {
                        startActivity(
                            Intent(this, ActivityComposeMain::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra("open_tab", "accounts")
                        )
                        finish()
                    },
                    onReload = {
                        ServiceSynchronize.reload(this, accountId, true, "retry")
                        finish()
                    },
                    onFaq = {
                        val isCert = errorMessage.contains("CertificateException")
                        Helper.viewFAQ(this, if (isCert) 4 else faqId)
                    },
                    onSupport = {
                        val sb = StringBuilder()
                        sb.append("auth_type=").append(ServiceAuthenticator.getAuthTypeName(authType)).append("\n")
                        if (!TextUtils.isEmpty(host)) sb.append("host=").append(host).append("\n")
                        if (accountId > 0) {
                            sb.append("protocol=").append(if (protocol == EntityAccount.TYPE_IMAP) "imap" else "pop3").append("\n")
                        }
                        if (!TextUtils.isEmpty(provider)) sb.append("provider=").append(provider).append("\n")
                        if (!TextUtils.isEmpty(errorMessage)) sb.append(Helper.limit(errorMessage, 384))
                        val uri = Helper.getSupportUri(this, "Sync:error")
                            .buildUpon()
                            .appendQueryParameter("message", sb.toString())
                            .build()
                        Helper.view(this, uri, true)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadFromIntent(intent)
    }

    private fun loadFromIntent(intent: Intent?) {
        if (intent == null) return
        errorTitle = intent.getStringExtra("title").orEmpty()
        errorMessage = intent.getStringExtra("message").orEmpty()
        provider = intent.getStringExtra("provider")
        accountId = intent.getLongExtra("account", -1L)
        protocol = intent.getIntExtra("protocol", -1)
        authType = intent.getIntExtra("auth_type", -1)
        host = intent.getStringExtra("host")
        faqId = intent.getIntExtra("faq", -1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorScreen(
    title: String,
    message: String,
    accountId: Long,
    protocol: Int,
    authType: Int,
    host: String?,
    provider: String?,
    faqId: Int,
    onBack: () -> Unit,
    onReauthorize: () -> Unit,
    onReload: () -> Unit,
    onFaq: () -> Unit,
    onSupport: () -> Unit
) {
    val isCert = message.contains("CertificateException")
    val isPassword = authType == ServiceAuthenticator.AUTH_TYPE_PASSWORD

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.twotone_close_24),
                            contentDescription = "Close"
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.title_setup_error),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    if (faqId > 0) {
                        IconButton(onClick = onFaq) {
                            Icon(
                                painter = painterResource(R.drawable.twotone_info_24),
                                contentDescription = "FAQ"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (title.isNotBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Text(
                        text = message.ifBlank { "Unknown sync or account error." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (isCert && !SSLHelper.customTrustManager()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Server certificate verification failed. Check your connection or trusted certificates.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            if (accountId >= 0) {
                Button(
                    onClick = onReauthorize,
                    shape = CircleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        stringResource(
                            if (isPassword) R.string.title_password else R.string.title_setup_oauth_authorize
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (accountId > 0) {
                    FilledTonalButton(
                        onClick = onReload,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Retry Sync")
                    }
                }
                OutlinedButton(
                    onClick = onSupport,
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Support")
                }
            }
        }
    }
}
