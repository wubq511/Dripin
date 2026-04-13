package com.dripin.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DripinLightColors = lightColorScheme(
    primary = DripinAccent,
    onPrimary = DripinPaper,
    primaryContainer = DripinAccentSoft,
    onPrimaryContainer = DripinInk,
    secondary = DripinWarn,
    onSecondary = DripinPaper,
    secondaryContainer = DripinAccentSoft,
    onSecondaryContainer = DripinInk,
    background = DripinCanvas,
    onBackground = DripinInk,
    surface = DripinPaper,
    onSurface = DripinInk,
    surfaceVariant = DripinAccentSoft,
    onSurfaceVariant = DripinInkMuted,
    outline = DripinLine,
)

private val DripinDarkColors = darkColorScheme(
    primary = DripinAccentSoft,
    onPrimary = DripinInk,
    primaryContainer = DripinAccent,
    onPrimaryContainer = DripinPaper,
    secondary = DripinWarn,
    onSecondary = DripinPaper,
    secondaryContainer = DripinAccent,
    onSecondaryContainer = DripinPaper,
    background = DripinInk,
    onBackground = DripinPaper,
    surface = Color(0xFF1E2226),
    onSurface = DripinPaper,
    surfaceVariant = Color(0xFF2A3035),
    onSurfaceVariant = Color(0xFFD2CCC1),
    outline = Color(0xFF4A5259),
)

@Composable
fun DripinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DripinDarkColors else DripinLightColors,
        typography = DripinTypography,
        content = content,
    )
}
