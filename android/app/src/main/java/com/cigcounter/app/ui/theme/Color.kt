package com.cigcounter.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Static Dark Color Values
val DarkBackgroundVal = Color(0xFF101012)
val DarkSurfaceVal = Color(0xFF18181B)
val DarkSurfaceVariantVal = Color(0xFF27272A)
val DarkOutlineVal = Color(0xFF3F3F46)
val TextPrimaryDarkVal = Color(0xFFF4F4F5)
val TextSecondaryDarkVal = Color(0xFFA1A1AA)
val TextMutedDarkVal = Color(0xFF71717A)

// Static Light Color Values
val LightBackgroundVal = Color(0xFFF8F9FA)
val LightSurfaceVal = Color(0xFFFFFFFF)
val LightSurfaceVariantVal = Color(0xFFF1F3F5)
val LightOutlineVal = Color(0xFFE2E8F0)
val TextPrimaryLightVal = Color(0xFF0F172A)
val TextSecondaryLightVal = Color(0xFF475569)
val TextMutedLightVal = Color(0xFF94A3B8)

// Accent Colors
val AccentCoral = Color(0xFFFF6E40)
val AccentCoralVariant = Color(0xFFFF8A65)
val AccentAmber = Color(0xFFFFB74D)

val SuccessGreen = Color(0xFF4ADE80)
val ErrorRed = Color(0xFFF87171)

// Dynamic theme-aware accessors for Composables
val DarkBackground: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) DarkBackgroundVal else LightBackgroundVal

val DarkSurface: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) DarkSurfaceVal else LightSurfaceVal

val DarkSurfaceVariant: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) DarkSurfaceVariantVal else LightSurfaceVariantVal

val DarkOutline: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) DarkOutlineVal else LightOutlineVal

val TextPrimary: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) TextPrimaryDarkVal else TextPrimaryLightVal

val TextSecondary: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) TextSecondaryDarkVal else TextSecondaryLightVal

val TextMuted: Color
    @Composable
    get() = if (LocalIsDarkTheme.current) TextMutedDarkVal else TextMutedLightVal
