package com.example.dripin4.ui.designsystem.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

private var noiseBitmapCache: ImageBitmap? = null

internal fun getNoiseBitmap(): ImageBitmap {
    if (noiseBitmapCache == null) {
        val size = 128
        val pixels = IntArray(size * size)
        for (i in pixels.indices) {
            val r = Random.nextInt(256)
            pixels[i] = AndroidColor.argb(255, r, r, r)
        }
        val bitmap = Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
        noiseBitmapCache = bitmap.asImageBitmap()
    }
    return noiseBitmapCache!!
}
