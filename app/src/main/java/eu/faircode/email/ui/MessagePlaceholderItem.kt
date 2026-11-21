package eu.faircode.email.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import eu.faircode.email.R
import eu.faircode.email.ui.theme.FairEmailTheme

@Composable
fun MessagePlaceholderItem(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.title_loading),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagePlaceholderItemPreview() {
    FairEmailTheme {
        MessagePlaceholderItem()
    }
}
