package com.dripin.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val DripinLightColors = lightColorScheme(
    primary = DripinAccent,
    onPrimary = DripinPaper,
    primaryContainer = DripinAccentSoft,
    onPrimaryContainer = DripinInk,
    secondary = DripinWarn,
    onSecondary = DripinPaper,
    secondaryContainer = Color(0xFFF1E6D8),
    onSecondaryContainer = DripinInk,
    tertiary = DripinRose,
    onTertiary = DripinPaper,
    tertiaryContainer = DripinSky,
    onTertiaryContainer = DripinInk,
    background = DripinCanvas,
    onBackground = DripinInk,
    surface = DripinPaper,
    onSurface = DripinInk,
    surfaceVariant = DripinMist,
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
    tertiary = DripinRose,
    onTertiary = DripinPaper,
    tertiaryContainer = Color(0xFF523635),
    onTertiaryContainer = DripinPaper,
    background = DripinInk,
    onBackground = DripinPaper,
    surface = Color(0xFF202420),
    onSurface = DripinPaper,
    surfaceVariant = Color(0xFF2B302C),
    onSurfaceVariant = Color(0xFFD2CCC1),
    outline = Color(0xFF4A5259),
)

private val DripinShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(20.dp),
    medium = RoundedCornerShape(26.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

@Composable
fun DripinTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DripinDarkColors else DripinLightColors,
        typography = DripinTypography,
        shapes = DripinShapes,
        content = content,
    )
}
