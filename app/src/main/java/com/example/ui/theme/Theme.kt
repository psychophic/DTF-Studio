package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ForestGreenColorScheme = lightColorScheme(
    primary = StudioAccent,
    onPrimary = Color.White,
    primaryContainer = StudioAccentLight,
    onPrimaryContainer = StudioAccent,
    secondary = StudioAccentMedium,
    onSecondary = Color.White,
    secondaryContainer = StudioAccentLight,
    onSecondaryContainer = StudioAccent,
    background = StudioBg,
    onBackground = StudioText,
    surface = StudioPanel,
    onSurface = StudioText,
    surfaceVariant = StudioCardAlt,
    onSurfaceVariant = StudioMuted,
    outline = StudioBorder,
    outlineVariant = StudioBorderStrong,
    error = StudioDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ForestGreenColorScheme,
        typography = Typography,
        content = content
    )
}
