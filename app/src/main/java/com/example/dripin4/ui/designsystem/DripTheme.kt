package com.example.dripin4.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val DripLightColorScheme = lightColorScheme(
    primary = DripColors.Ink,
    onPrimary = DripColors.MistWhite,
    primaryContainer = DripColors.HazeGreen,
    onPrimaryContainer = DripColors.Ink,
    secondary = DripColors.Graphite,
    onSecondary = DripColors.MistWhite,
    secondaryContainer = DripColors.WarmHaze,
    onSecondaryContainer = DripColors.Ink,
    tertiary = DripColors.FrostLime,
    onTertiary = DripColors.Ink,
    tertiaryContainer = DripColors.SoftGlowGreen,
    onTertiaryContainer = DripColors.Ink,
    background = DripColors.FogIvory,
    onBackground = DripColors.Ink,
    surface = DripColors.MistWhite,
    onSurface = DripColors.Ink,
    surfaceVariant = DripColors.SoftPearl,
    onSurfaceVariant = DripColors.Graphite,
    surfaceTint = DripColors.HazeGreen,
    outline = DripColors.HairlineDark,
    outlineVariant = DripColors.HairlineSoft,
    inverseSurface = DripColors.Ink,
    inverseOnSurface = DripColors.MistWhite,
    inversePrimary = DripColors.FrostLime,
    scrim = DripColors.Ink.copy(alpha = 0.28f)
)

@Composable
fun DripTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DripLightColorScheme,
        typography = DripTypography,
        content = content
    )
}
