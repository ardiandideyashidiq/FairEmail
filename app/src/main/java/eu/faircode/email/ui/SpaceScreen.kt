package eu.faircode.email.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun SpaceScreen(
    remainingText: String,
    requiredText: String,
    onFix: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.app_cake),
                color = FairEmailTheme.colors.warning,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = remainingText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = requiredText,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
            Button(
                onClick = onFix,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(text = stringResource(R.string.title_fix))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.twotone_settings_24),
                    contentDescription = null
                )
            }
            Button(
                onClick = onContinue,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(text = stringResource(R.string.app_continue))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.twotone_check_24),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpaceScreenPreview() {
    FairEmailTheme(darkTheme = false) {
        SpaceScreen(
            remainingText = "Remaining: 10 MB",
            requiredText = "Required: 100 MB",
            onFix = {},
            onContinue = {}
        )
    }
}
