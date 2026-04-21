package com.example.dripin4.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DripTokensTest {
    @Test
    fun keyTokens_matchSpecValues() {
        assertEquals(Color(0xFFF2F4EC), DripColors.FogIvory)
        assertEquals(Color(0xFFF6F7F2), DripColors.MistWhite)
        assertEquals(Color(0xFF8E96A2), DripColors.MistGray)
        assertEquals(Color(0x1F1E232B), DripColors.HairlineDark)
        assertEquals(18, DripRadius.ScreenCard)
        assertEquals(14, DripRadius.SecondaryCard)
        assertEquals(220, DripMotion.StandardMs)
    }

    @Test
    fun materialTokens_followSpecRanges() {
        assertTrue(DripGlass.SurfaceAlpha in 0.42f..0.68f)
        assertTrue(DripGlass.SurfaceBlurDp in DripGlass.BlurMinDp..DripGlass.BlurMaxDp)
        assertTrue(DripRadius.ScreenCard in 18..18)
        assertTrue(DripRadius.SecondaryCard in 14..14)
        assertEquals(999, DripRadius.Pill)
        assertTrue(DripMotion.StandardMs in 120..240)
        assertTrue(DripMotion.PressedScale in DripMotion.PressedScaleMin..DripMotion.PressedScaleMax)
    }

    @Test
    fun themeColorScheme_mapsToTokenPalette() {
        assertEquals(DripColors.FogIvory, DripLightColorScheme.background)
        assertEquals(DripColors.MistWhite, DripLightColorScheme.surface)
        assertEquals(DripColors.Ink, DripLightColorScheme.primary)
        assertEquals(DripColors.HairlineDark, DripLightColorScheme.outline)
        assertEquals(DripColors.HairlineSoft, DripLightColorScheme.outlineVariant)
    }

    @Test
    fun typography_distinguishesBrandLatinAndContentCjkFamilies() {
        assertEquals(FontFamily.SansSerif, DripBrandLatinFontFamily)
        assertEquals(FontFamily.Default, DripContentCjkFontFamily)

        assertEquals(DripBrandLatinFontFamily, DripTypography.displaySmall.fontFamily)
        assertEquals(DripBrandLatinFontFamily, DripTypography.titleLarge.fontFamily)
        assertEquals(DripBrandLatinFontFamily, DripTypography.titleMedium.fontFamily)
        assertEquals(DripBrandLatinFontFamily, DripTypography.titleSmall.fontFamily)
        assertEquals(DripBrandLatinFontFamily, DripTypography.labelLarge.fontFamily)
        assertEquals(DripBrandLatinFontFamily, DripTypography.labelMedium.fontFamily)
        assertEquals(DripBrandLatinFontFamily, DripTypography.labelSmall.fontFamily)

        assertEquals(DripContentCjkFontFamily, DripTypography.bodyLarge.fontFamily)
        assertEquals(DripContentCjkFontFamily, DripTypography.bodyMedium.fontFamily)
        assertEquals(DripContentCjkFontFamily, DripTypography.bodySmall.fontFamily)

        assertNotEquals(
            DripBrandLatinFontFamily,
            DripContentCjkFontFamily
        )
    }

    @Test
    fun typedSizeTokens_alignWithScalarSizeTokens() {
        assertEquals(DripRadius.ScreenCard.dp, DripRadius.ScreenCardDp)
        assertEquals(DripRadius.SecondaryCard.dp, DripRadius.SecondaryCardDp)
        assertEquals(DripRadius.Pill.dp, DripRadius.PillDp)

        assertEquals(DripGlass.BlurMinDp.dp, DripGlass.BlurMin)
        assertEquals(DripGlass.SurfaceBlurDp.dp, DripGlass.SurfaceBlur)
        assertEquals(DripGlass.BlurMaxDp.dp, DripGlass.BlurMax)
        assertEquals(DripGlass.BorderPrimaryDp.dp, DripGlass.BorderPrimary)
        assertEquals(DripGlass.BorderSecondaryDp.dp, DripGlass.BorderSecondary)

        assertEquals(DripShadow.ElevationDp.dp, DripShadow.Elevation)
    }
}
