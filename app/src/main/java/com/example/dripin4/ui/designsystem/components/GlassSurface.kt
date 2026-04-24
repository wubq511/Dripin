package com.example.dripin4.ui.designsystem.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dripin4.ui.designsystem.DripColors
import com.example.dripin4.ui.designsystem.DripRadius
import com.example.dripin4.ui.designsystem.DripSpacing
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.features.today.AuroraBackground
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight

enum class GlassCardTone {
    Hero,
    Neutral
}

private val TodayItemShape = RoundedCornerShape(DripRadius.ScreenCardDp)
private val TodayItemInnerShape = RoundedCornerShape(DripRadius.SecondaryCardDp)

@Composable
fun GlassBackdropAtmosphereLayer(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .layerBackdrop(backdrop)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        GlassPalette.AtmosphereBgStart,
                        GlassPalette.AtmosphereBgMid,
                        GlassPalette.AtmosphereBgEnd,
                    ),
                ),
            ),
    ) {
        AuroraBackground(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = 1.5f,
                    transformOrigin = TransformOrigin(1f, 0.5f),
                )
                .height(650.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb1Start,
                            GlassPalette.AtmosphereOrb1Mid,
                            GlassPalette.AtmosphereOrb1End,
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 8.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb2Start,
                            GlassPalette.AtmosphereOrb2Mid,
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 68.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb3Start,
                            GlassPalette.AtmosphereOrb3Mid,
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 190.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb4Start,
                            GlassPalette.AtmosphereOrb4Mid,
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun GlassScaffoldBackground(
    modifier: Modifier = Modifier,
    todayMode: Boolean = false
) {
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereBgStart,
                            GlassPalette.AtmosphereBgMid,
                            GlassPalette.AtmosphereBgEnd,
                        ),
                    ),
                ),
        )
        AuroraBackground(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(
                    scaleX = 1.5f,
                    transformOrigin = TransformOrigin(1f, 0.5f),
                )
                .height(650.dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb1Start,
                            GlassPalette.AtmosphereOrb1Mid,
                            GlassPalette.AtmosphereOrb1End,
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.TopEnd)
                .padding(top = 36.dp, end = 8.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb2Start,
                            GlassPalette.AtmosphereOrb2Mid,
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 68.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb3Start,
                            GlassPalette.AtmosphereOrb3Mid,
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 190.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassPalette.AtmosphereOrb4Start,
                            GlassPalette.AtmosphereOrb4Mid,
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    tone: GlassCardTone = GlassCardTone.Neutral,
    cornerRadius: Dp = DripRadius.ScreenCardDp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val outerShape = RoundedCornerShape(cornerRadius)

    when (tone) {
        GlassCardTone.Hero -> HeroGlassCard(
            modifier = modifier,
            shape = outerShape,
            backdrop = backdrop,
            contentPadding = contentPadding,
            content = content,
        )

        GlassCardTone.Neutral -> NeutralGlassCard(
            modifier = modifier,
            shape = outerShape,
            backdrop = backdrop,
            contentPadding = contentPadding,
            content = content,
        )
    }
}

@Composable
private fun HeroGlassCard(
    modifier: Modifier,
    shape: Shape,
    backdrop: Backdrop?,
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    val surfaceModifier = if (backdrop != null) {
        Modifier.liquidGlassSurface(
            backdrop = backdrop,
            shape = shape,
            fillAlpha = 0.62f,
            blurRadius = 28.dp,
            withTopHighlight = true,
            withHairline = true,
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.62f), shape)
    }

    Box(
        modifier = modifier
            .todayShadow(
                color = DripColors.PureBlack,
                alpha = 0.10f,
                offsetY = 20.dp,
                blur = 44.dp,
                shape = shape,
            )
            .todayShadow(
                color = DripColors.PureBlack,
                alpha = 0.07f,
                offsetY = 6.dp,
                blur = 18.dp,
                shape = shape,
            )
            .clip(shape)
            .then(surfaceModifier),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content,
        )
    }
}

@Composable
private fun NeutralGlassCard(
    modifier: Modifier,
    shape: Shape,
    backdrop: Backdrop?,
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val outerSurfaceModifier = if (backdrop != null && isBlurSupported) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(radius = 32f)
                lens(
                    refractionHeight = 5.dp.toPx(),
                    refractionAmount = 6.dp.toPx(),
                )
            },
            highlight = {
                Highlight.Plain.copy(
                    alpha = 0.88f,
                    width = 0.8.dp,
                )
            },
            onDrawSurface = {
                drawRect(color = DripColors.PureWhite.copy(alpha = 0.62f))
            },
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.62f), shape)
    }

    Box(
        modifier = modifier
            .todayShadow(
                color = DripColors.PureBlack,
                alpha = 0.06f,
                offsetY = 6.dp,
                blur = 16.dp,
                shape = shape,
            )
            .then(outerSurfaceModifier)
            .border(
                width = 0.5.dp,
                color = DripColors.PureWhite.copy(alpha = 0.35f),
                shape = shape,
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DripColors.PureWhite.copy(alpha = 0.8f),
                        Color.Transparent,
                    ),
                ),
                shape = shape,
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(TodayItemInnerShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            GlassPalette.TodayItemInnerBgStart,
                            GlassPalette.TodayItemInnerBgMid,
                            GlassPalette.TodayItemInnerBgEnd,
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = DripColors.PureWhite.copy(alpha = 0.58f),
                    shape = TodayItemInnerShape,
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                content = content,
            )
        }
    }
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = DripRadius.SecondaryCardDp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        GlassPalette.TodayItemInnerBgStart,
                        GlassPalette.TodayItemInnerBgMid,
                        GlassPalette.TodayItemInnerBgEnd,
                    ),
                ),
            )
            .border(
                width = 1.dp,
                color = DripColors.PureWhite.copy(alpha = 0.58f),
                shape = shape,
            ),
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

private fun Modifier.todayShadow(
    color: Color,
    alpha: Float,
    offsetX: Dp = 0.dp,
    offsetY: Dp,
    blur: Dp,
    shape: Shape,
): Modifier = drawBehind {
    val shadowColor = color.copy(alpha = alpha).toArgb()
    val transparentColor = color.copy(alpha = 0f).toArgb()
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            blur.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor,
        )
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply { addOutline(outline) }
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), frameworkPaint)
    }
}
