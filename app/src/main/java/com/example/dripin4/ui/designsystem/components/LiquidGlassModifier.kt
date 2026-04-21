package com.example.dripin4.ui.designsystem.components

import android.os.Build
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.dripin4.ui.designsystem.GlassPalette
import com.example.dripin4.ui.designsystem.DripColors
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.effects.lens
import kotlin.random.Random

@Composable
fun Modifier.liquidGlassSurface(
    backdrop: Backdrop,
    shape: Shape,
    fillAlpha: Float = GlassPalette.SurfaceGlass.alpha,
    blurRadius: Dp = 22.dp,
    withTopHighlight: Boolean = true,
    withHairline: Boolean = true,
    customHighlightBrush: Brush? = null,
): Modifier {
    val noiseBitmap = remember { getNoiseBitmap() }
    
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val baseModifier = if (isBlurSupported) {
        Modifier.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                blur(blurRadius.value)
                vibrancy()
                lens(
                    refractionHeight = 4f,
                    refractionAmount = 4f,
                )
            }
        )
    } else {
        Modifier.background(DripColors.PureWhite.copy(alpha = 0.65f), shape)
    }
    
    return this.then(
        baseModifier
            .background(GlassPalette.SurfaceGlass.copy(alpha = fillAlpha), shape)
            .drawWithCache {
                val highlightBrush = customHighlightBrush ?: Brush.linearGradient(
                    colors = listOf(
                        GlassPalette.HighlightTop,
                        Color.Transparent
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height * 0.4f)
                )
                onDrawWithContent {
                    
                    val noiseW = noiseBitmap.width
                    val noiseH = noiseBitmap.height
                    val tileXCount = (size.width / noiseW).toInt() + 1
                    val tileYCount = (size.height / noiseH).toInt() + 1
                    
                    for (x in 0 until tileXCount) {
                        for (y in 0 until tileYCount) {
                            drawImage(
                                image = noiseBitmap,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(noiseW, noiseH),
                                dstOffset = IntOffset(x * noiseW, y * noiseH),
                                dstSize = IntSize(noiseW, noiseH),
                                alpha = 0.04f,
                                blendMode = BlendMode.Overlay
                            )
                        }
                    }
                    
                    drawContent()
                    
                    val outline = shape.createOutline(size, layoutDirection, this)
                    
                    val path = Path().apply { addOutline(outline) }
                    
                    if (withHairline) {
                        drawPath(
                            path = path,
                            color = GlassPalette.OutlineGlass,
                            style = Stroke(width = 0.5.dp.toPx())
                        )
                    }
                    if (withTopHighlight) {
                        drawPath(
                            path = path,
                            brush = highlightBrush,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }
                }
            }
    )
}


fun Modifier.glassBloom(
    color: Color,
    spread: Dp,
    alpha: Float = 1f,
    shape: Shape = androidx.compose.ui.graphics.RectangleShape
): Modifier = this.drawBehind {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val spreadPx = spread.toPx()
        val bloomColor = color.copy(alpha = color.alpha * alpha).toArgb()
        
        if (spreadPx > 0f) {
            val paint = Paint().asFrameworkPaint().apply {
                this.color = bloomColor
                maskFilter = android.graphics.BlurMaskFilter(spreadPx, android.graphics.BlurMaskFilter.Blur.NORMAL)
            }
            drawIntoCanvas { canvas ->
                val outline = shape.createOutline(size, layoutDirection, this)
                val path = Path().apply { addOutline(outline) }
                canvas.nativeCanvas.drawPath(
                    path.asAndroidPath(),
                    paint
                )
            }
        }
    }
}

