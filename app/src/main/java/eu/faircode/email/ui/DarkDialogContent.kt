package eu.faircode.email.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun DarkDialogContent(
    onIssue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.title_compat_dark),
            style = MaterialTheme.typography.bodyLarge
        )
        Button(
            onClick = onIssue,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(text = stringResource(R.string.title_info))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DarkDialogContentPreview() {
    FairEmailTheme {
        DarkDialogContent(onIssue = {})
    }
}
