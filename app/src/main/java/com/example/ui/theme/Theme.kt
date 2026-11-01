package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColorScheme = darkColorScheme(
    primary = StudioAccent,
    onPrimary = StudioText,
    primaryContainer = StudioCard,
    onPrimaryContainer = StudioText,
    secondary = StudioAccentHover,
    onSecondary = StudioText,
    background = StudioBg,
    onBackground = StudioText,
    surface = StudioPanel,
    onSurface = StudioText,
    surfaceVariant = StudioCard,
    onSurfaceVariant = StudioMuted,
    outline = StudioBorder,
    error = StudioDanger,
    onError = StudioText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep the authentic studio theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
