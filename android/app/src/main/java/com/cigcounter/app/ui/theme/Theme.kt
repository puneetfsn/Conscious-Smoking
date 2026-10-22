package com.cigcounter.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { true }

private val DarkColorScheme = darkColorScheme(
    primary = AccentCoral,
    onPrimary = DarkBackgroundVal,
    primaryContainer = DarkSurfaceVariantVal,
    onPrimaryContainer = AccentCoralVariant,
    secondary = AccentAmber,
    onSecondary = DarkBackgroundVal,
    background = DarkBackgroundVal,
    onBackground = TextPrimaryDarkVal,
    surface = DarkSurfaceVal,
    onSurface = TextPrimaryDarkVal,
    surfaceVariant = DarkSurfaceVariantVal,
    onSurfaceVariant = TextSecondaryDarkVal,
    outline = DarkOutlineVal,
    error = ErrorRed,
    onError = DarkBackgroundVal
)

private val LightColorScheme = lightColorScheme(
    primary = AccentCoral,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceVariantVal,
    onPrimaryContainer = AccentCoral,
    secondary = AccentAmber,
    onSecondary = Color.White,
    background = LightBackgroundVal,
    onBackground = TextPrimaryLightVal,
    surface = LightSurfaceVal,
    onSurface = TextPrimaryLightVal,
    surfaceVariant = LightSurfaceVariantVal,
    onSurfaceVariant = TextSecondaryLightVal,
    outline = LightOutlineVal,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun CigCounterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
