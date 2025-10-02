package com.example.fakeglide.transformation

import android.graphics.*
import android.graphics.Shader.TileMode
import androidx.core.graphics.createBitmap

class CircleCrop : Transformation {

    override fun key(): String = "CIRCLE_CROP"

    override fun transform(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)

        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2

        val squared = Bitmap.createBitmap(bitmap, x, y, size, size)

        val output = createBitmap(size, size)

        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(squared, TileMode.CLAMP, TileMode.CLAMP)
        }

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        squared.recycle()

        return output
    }
}