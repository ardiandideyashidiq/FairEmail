package eu.faircode.email.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun UnsupportedScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(R.string.app_unsupported),
                color = FairEmailTheme.colors.warning,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(top = 12.dp)
            )
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
private fun UnsupportedScreenPreview() {
    FairEmailTheme(darkTheme = false) {
        UnsupportedScreen(onContinue = {})
    }
}
