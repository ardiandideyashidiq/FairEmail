package eu.faircode.email.ui.theme

import android.animation.ValueAnimator
import android.os.Build
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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
    primaryContainer = Color(0xFFCDE9FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFFFFB300),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFFFE082),
    onSecondaryContainer = Color(0xFF261900)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF003351),
    primaryContainer = Color(0xFF004B73),
    onPrimaryContainer = Color(0xFFCDE9FF),
    secondary = Color(0xFFFFCA28),
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFF5A4300),
    onSecondaryContainer = Color(0xFFFFE082)
)

private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

object ExpressiveMotion {
    fun isReducedMotion(): Boolean = !ValueAnimator.areAnimatorsEnabled()

    fun <T> spatialFast(): FiniteAnimationSpec<T> =
        if (isReducedMotion()) snap()
        else spring(dampingRatio = 0.65f, stiffness = 800f)

    fun <T> spatialDefault(): FiniteAnimationSpec<T> =
        if (isReducedMotion()) snap()
        else spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)

    fun <T> effectsDefault(): FiniteAnimationSpec<T> =
        if (isReducedMotion()) snap()
        else spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
}

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
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val fairEmailColors = if (darkTheme) DarkFairEmailColors else LightFairEmailColors
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    CompositionLocalProvider(LocalFairEmailColors provides fairEmailColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}
