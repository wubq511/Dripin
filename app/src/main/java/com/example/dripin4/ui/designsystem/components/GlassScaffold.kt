package com.example.dripin4.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripRadius
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.kyant.backdrop.backdrops.LayerBackdrop

val LocalGlassBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

enum class GlassHeroAccent {
    Mint,
    Ink,
}

private data class GlassHeroStyle(
    val titleGradient: List<Color>,
    val pillTint: Color,
)

@Composable
fun ProvideGlassBackdrop(
    backdrop: LayerBackdrop?,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalGlassBackdrop provides backdrop, content = content)
}

@Composable
fun GlassScaffold(
    testTag: String,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = DripSpacing.ScreenHorizontal,
        top = DripSpacing.ScreenTop,
        end = DripSpacing.ScreenHorizontal,
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + DripSpacing.ScreenBottomChrome,
    ),
    header: @Composable () -> Unit,
    content: LazyListScope.() -> Unit,
) {
    val backdrop = LocalGlassBackdrop.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag(testTag),
    ) {
        if (backdrop != null) {
            GlassBackdropAtmosphereLayer(
                backdrop = backdrop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(DripSpacing.SectionGap),
        ) {
            item("header") {
                header()
            }
            content()
        }
    }
}

@Composable
fun GlassHeroHeader(
    eyebrow: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accent: GlassHeroAccent = GlassHeroAccent.Mint,
    metaAtStart: Boolean = false,
    meta: String? = null,
    metaIcon: ImageVector? = null,
    supportingContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val style = accent.style()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DripSpacing.Small),
    ) {
        Column {
            Text(
                text = eyebrow,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.35).sp,
                    color = GlassPalette.TextTodaySelection,
                ),
            )
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 190.dp),
            tone = GlassCardTone.Hero,
            cornerRadius = DripRadius.HeroCardDp,
            contentPadding = PaddingValues(DripSpacing.HeroPadding),
        ) {
            if (meta != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (metaAtStart) Arrangement.Start else Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HeroMetaLabel(
                        text = meta,
                        icon = metaIcon,
                        tint = style.pillTint,
                    )
                }
                Spacer(modifier = Modifier.height(DripSpacing.Small))
            }

            Text(
                text = title,
                style = TextStyle(
                    brush = Brush.linearGradient(style.titleGradient),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = GlassPalette.TextHeroBody,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )

            if (supportingContent != null) {
                Spacer(modifier = Modifier.height(DripSpacing.HeroBodyGap))
                supportingContent()
            }
        }
    }
}

@Composable
fun GlassPageHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.35).sp,
                color = GlassPalette.TextTodaySelection,
            ),
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = GlassPalette.TextTodaySubtitle,
                    letterSpacing = 0.12.sp,
                ),
            )
        }
    }
}

@Composable
fun GlassSectionHeading(
    title: String,
    body: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DripSpacing.XSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = GlassPalette.TextTodaySelection,
        )
        if (body != null) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = GlassPalette.TextMutedStrong,
            )
        }
    }
}

@Composable
private fun HeroMetaLabel(
    text: String,
    icon: ImageVector?,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.height(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                color = tint,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

@Composable
fun GlassInfoPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = GlassPalette.AccentMint,
) {
    val shape = RoundedCornerShape(DripRadius.PillDp)
    val backdrop = LocalGlassBackdrop.current
    val surfaceModifier = if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = shape,
            fillAlpha = 0.44f,
            blurRadius = 14.dp,
            withTopHighlight = true,
            withHairline = true,
        )
    } else {
        Modifier
            .background(GlassPalette.SurfaceHeroPill, shape)
            .border(0.5.dp, DripColors.PureWhite.copy(alpha = 0.74f), shape)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(surfaceModifier)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        GlassPalette.SurfaceHeroPill,
                        GlassPalette.SurfaceHeroPill.copy(alpha = 0.52f),
                    ),
                ),
                shape = shape,
            )
            .padding(horizontal = DripSpacing.Small, vertical = DripSpacing.XSmall),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                )
                Spacer(modifier = Modifier.width(DripSpacing.XSmall))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
            )
        }
    }
}

private fun GlassHeroAccent.style(): GlassHeroStyle = when (this) {
    GlassHeroAccent.Mint -> GlassHeroStyle(
        titleGradient = listOf(
            GlassPalette.AccentMint,
            GlassPalette.AccentMintBright,
            GlassPalette.AccentLime,
        ),
        pillTint = GlassPalette.AccentMint,
    )
    GlassHeroAccent.Ink -> GlassHeroStyle(
        titleGradient = listOf(
            GlassPalette.TextTodaySelection,
            GlassPalette.TextTodaySelection,
            GlassPalette.TextTodaySelection,
        ),
        pillTint = GlassPalette.TextTodaySelection,
    )
}
