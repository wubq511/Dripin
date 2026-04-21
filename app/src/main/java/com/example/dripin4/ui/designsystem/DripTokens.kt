package com.example.dripin4.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object GlassPalette {
    val SurfaceGlass = Color.White.copy(alpha = 0.55f)
    val SurfaceGlassStrong = Color.White.copy(alpha = 0.70f)
    val OutlineGlass = Color.White.copy(alpha = 0.35f)
    val HighlightTop = Color.White.copy(alpha = 0.80f)
    val ShadowNear = Color.Black.copy(alpha = 0.08f)
    val ShadowAmbient = Color(0xFF1F7A4A).copy(alpha = 0.18f)
    val AccentMint = Color(0xFF1F7A4A)
    val AccentMintBright = Color(0xFF4FC98A)
    val AccentLime = Color(0xFF9FE07A)
    val AccentMintSoft = Color(0xFF7FE5A8).copy(alpha = 0.22f)
    val BloomAccentMint = Color(0x427FE5A8)
    val DockShadowAmbient = Color(0x241F7A4A)
    val DockSelectedTintStart = Color(0x477FE5A8)
    val DockSelectedTintEnd = Color(0x38D4F57A)
    val HeroShadowAmbient = Color(0x381F7A4A)
    val HeroVignette = Color(0x1A0B2A1C)
    val TextPrimary = Color(0xFF0B0B0B)
    val TextSecondary = Color(0xFF6B6B6B)
    val TextOnGlass = Color(0xFF0B2A1C)
    val TextTodaySelection = Color(0xFF1A232B)
    val TextTodaySubtitle = Color(0xFF5F6F7C)
    val TextTodayItemTitle = Color(0xFF112129)
    val TextTodayItemBadge = Color(0xFF3A3A3A)
    val TextTodayItemMeta = Color(0xFF70818E)
    val TextHeroBody = Color(0xFF0B2A1C)
    val TextHeroOverline = Color(0xFF38505F)
    val TextHeroMeta = Color(0xFF315E43)
    val TextMutedStrong = Color(0xFF55636F)
    val SurfaceHeroPill = Color.White.copy(alpha = 0.68f)

    val HeroBlobMint = Color(0xFF7FE5A8)
    val HeroBlobLime = Color(0xFFD4F57A)

    val AtmosphereBgStart = Color(0xFFF6F7FB)
    val AtmosphereBgMid = Color(0xFFF5F7FB)
    val AtmosphereBgEnd = Color(0xFFF4F6FA)

    val AtmosphereOrb1Start = Color(0x99DDFBF5)
    val AtmosphereOrb1Mid = Color(0x87D4F2FF)
    val AtmosphereOrb1End = Color(0x80F0E6FF)

    val AtmosphereOrb2Start = Color(0x8ADDEBFF)
    val AtmosphereOrb2Mid = Color(0x88D8FFE8)

    val AtmosphereOrb3Start = Color(0x66D8DFFF)
    val AtmosphereOrb3Mid = Color(0x57EAF7FF)

    val AtmosphereOrb4Start = Color(0x52E9FFE8)
    val AtmosphereOrb4Mid = Color(0x4AE8F2FF)

    val TodayItemSurfaceStart = Color(0x4BFFFFFF)
    val TodayItemSurfaceMid = Color(0x36F5F9FF)
    val TodayItemSurfaceEnd = Color(0x2EEEF8F2)
    val TodayItemBgStart = Color(0x42FFFFFF)
    val TodayItemBgMid = Color(0x31F4F8FD)
    val TodayItemBgEnd = Color(0x2BEFF6F2)
    val TodayItemInnerBgStart = Color(0x42FFFFFF)
    val TodayItemInnerBgMid = Color(0x2BEFF6FF)
    val TodayItemInnerBgEnd = Color(0x24E7F0FF)

    val AuroraGreen = Color(0xFFB8F5C8)
    val AuroraBlue = Color(0xFFC9E8FF)
    val AuroraPurple = Color(0xFFE6D4FF)
    val AuroraPeach = Color(0xFFFFE4B8)
    val AuroraMint = Color(0xFFD4F5E0)
    
    val BloomGradientStart = Color.White
    val BloomGradientMid = Color(0xFFD4F9E4)
    val BloomGradientEnd = Color(0xFFB5F3CE)

    val AmbientSpotBright = Color(0xFF7FEFC8)
    val AmbientSpotSoft = Color(0xFFAFFFD3)
    val SurfaceGlassSubtle = Color(0x15FFFFFF)
    val SurfaceGlassFaint = Color(0x0AFFFFFF)

    val SurfaceTintStart = Color(0xFFF7F8FC)
    val SurfaceTintMid = Color(0xFFF5F7FB)
    val SurfaceTintEnd = Color(0xFFF3F6FB)

    val OrbHighlightGreen = Color(0xFFE4FAEE)
    val OrbHighlightPurple = Color(0xFFEDEBFF)
    val OrbHighlightMint = Color(0xFFE3F9EA)
}

object DripColors {
    val PureWhite = Color.White
    val PureBlack = Color.Black
    val FogIvory = Color(0xFFF2F4EC)
    val MistWhite = Color(0xFFF6F7F2)
    val SoftPearl = Color(0xFFF8F8F4)
    val WarmHaze = Color(0xFFEEF2E8)
    val HazeGreen = Color(0xFFE2F6D4)
    val FrostLime = Color(0xFFDDF7C7)
    val SoftGlowGreen = Color(0xFFCFFD9A)
    val GlowGreen = SoftGlowGreen
    val Ink = Color(0xFF1E232B)
    val Graphite = Color(0xFF404651)
    val SoftGray = Color(0xFF7B828C)
    val MistGray = Color(0xFF8E96A2)
    val HairlineDark = Ink.copy(alpha = 0.12f)
    val HairlineSoft = Ink.copy(alpha = 0.05f)
    val FrostWhiteStroke = Color.White.copy(alpha = 0.85f)
}

object DripRadius {
    const val HeroCard = 28
    const val ScreenCard = 18
    const val SecondaryCard = 14
    const val Pill = 999
    val HeroCardDp: Dp = HeroCard.dp
    val ScreenCardDp: Dp = ScreenCard.dp
    val SecondaryCardDp: Dp = SecondaryCard.dp
    val PillDp: Dp = Pill.dp
}

object DripSpacing {
    const val XXSmallDp = 4f
    const val XSmallDp = 8f
    const val SmallDp = 12f
    const val MediumDp = 16f
    const val LargeDp = 20f
    const val XLargeDp = 24f
    const val XXLargeDp = 28f
    const val HeroBodyGapDp = 14f
    const val SectionGapDp = 12f
    const val HeroPaddingDp = 24f
    const val CardPaddingDp = 18f
    const val PanelPaddingHorizontalDp = 16f
    const val PanelPaddingVerticalDp = 14f
    const val ScreenHorizontalDp = 24f
    const val ScreenTopDp = 120f
    const val ScreenBottomChromeDp = 142f

    val XXSmall: Dp = XXSmallDp.dp
    val XSmall: Dp = XSmallDp.dp
    val Small: Dp = SmallDp.dp
    val Medium: Dp = MediumDp.dp
    val Large: Dp = LargeDp.dp
    val XLarge: Dp = XLargeDp.dp
    val XXLarge: Dp = XXLargeDp.dp
    val HeroBodyGap: Dp = HeroBodyGapDp.dp
    val SectionGap: Dp = SectionGapDp.dp
    val HeroPadding: Dp = HeroPaddingDp.dp
    val CardPadding: Dp = CardPaddingDp.dp
    val PanelPaddingHorizontal: Dp = PanelPaddingHorizontalDp.dp
    val PanelPaddingVertical: Dp = PanelPaddingVerticalDp.dp
    val ScreenHorizontal: Dp = ScreenHorizontalDp.dp
    val ScreenTop: Dp = ScreenTopDp.dp
    val ScreenBottomChrome: Dp = ScreenBottomChromeDp.dp
}

object DripGlass {
    const val SurfaceAlphaMin = 0.42f
    const val SurfaceAlpha = 0.56f
    const val SurfaceAlphaMax = 0.68f
    const val CardTopAlpha = 0.64f
    const val CardMidAlpha = 0.5f
    const val CardBottomAlpha = 0.58f
    const val PanelTopAlpha = 0.56f
    const val PanelBottomAlpha = 0.48f
    const val BorderHighlightAlpha = 0.28f
    const val PanelBorderAlpha = 0.84f
    const val GridLineAlpha = 0.014f
    const val GridSpacingDp = 28f
    const val GridStrokeDp = 0.8f
    const val GlowOrbPrimaryAlpha = 0.45f
    const val GlowOrbSecondaryAlpha = 0.2f
    const val GlowOrbTertiaryAlpha = 0.35f
    const val GlowOrbBlurDp = 80f
    const val BlurMinDp = 16f
    const val SurfaceBlurDp = 22f
    const val BlurMaxDp = 28f
    const val BorderPrimaryDp = 1f
    const val BorderSecondaryDp = 0.5f
    const val HighlightAlpha = 0.18f
    val GridLineColor: Color = DripColors.Ink.copy(alpha = GridLineAlpha)
    val GridSpacing: Dp = GridSpacingDp.dp
    val GridStroke: Dp = GridStrokeDp.dp
    val GlowOrbBlur: Dp = GlowOrbBlurDp.dp
    val BlurMin: Dp = BlurMinDp.dp
    val SurfaceBlur: Dp = SurfaceBlurDp.dp
    val BlurMax: Dp = BlurMaxDp.dp
    val BorderPrimary: Dp = BorderPrimaryDp.dp
    val BorderSecondary: Dp = BorderSecondaryDp.dp
}

object DripShadow {
    val Soft = Color(0xFF161C22).copy(alpha = 0.05f)
    const val ElevationDp = 4f
    val Elevation: Dp = ElevationDp.dp
}

object DripMotion {
    const val QuickMs = 140
    const val StandardMs = 220
    const val SlowMs = 280
    const val PressedScaleMin = 0.95f
    const val PressedScale = 0.97f
    const val PressedScaleMax = 0.98f
}
