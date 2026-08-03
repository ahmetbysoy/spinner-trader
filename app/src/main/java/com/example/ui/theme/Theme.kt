package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkTerminalColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = DarkObsidian,
    secondary = QuantGold,
    onSecondary = DarkObsidian,
    tertiary = NeonPurple,
    onTertiary = TextPrimary,
    background = DarkObsidian,
    onBackground = TextPrimary,
    surface = SurfaceCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantBorder,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceVariantBorder
)

@Composable
fun CryptoQuantTheme(
    darkTheme: Boolean = true, // Default to terminal dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkTerminalColorScheme,
        typography = Typography,
        content = content
    )
}
