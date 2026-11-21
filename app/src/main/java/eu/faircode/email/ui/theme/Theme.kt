package eu.faircode.email.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class FairEmailColors(
    val warning: Color,
    val accent: Color,
    val read: Color,
    val unread: Color
)

private val LightFairEmailColors = FairEmailColors(
    warning = Color(0xFFFF4F00),
    accent = Color(0xFFFFB300),
    read = Color(0xB3000000),
    unread = Color(0xFF000000)
)

private val DarkFairEmailColors = FairEmailColors(
    warning = Color(0xFFFFAE42),
    accent = Color(0xFFFFB300),
    read = Color(0xB3FFFFFF),
    unread = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF039BE5),
    onPrimary = Color.White,
    secondary = Color(0xFFFFB300),
    onSecondary = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF039BE5),
    onPrimary = Color.White,
    secondary = Color(0xFFFFB300),
    onSecondary = Color.Black
)

val LocalFairEmailColors = staticCompositionLocalOf { LightFairEmailColors }

object FairEmailTheme {
    val colors: FairEmailColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFairEmailColors.current
}

@Composable
fun FairEmailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val fairEmailColors = if (darkTheme) DarkFairEmailColors else LightFairEmailColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalFairEmailColors provides fairEmailColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
