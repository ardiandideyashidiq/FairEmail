package eu.faircode.email

import android.os.Bundle
import androidx.activity.compose.setContent
import eu.faircode.email.ui.theme.FairEmailTheme

internal class ActivityComposeMain : ActivityBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FairEmailTheme {
                MainComposeApp()
            }
        }
    }
}
