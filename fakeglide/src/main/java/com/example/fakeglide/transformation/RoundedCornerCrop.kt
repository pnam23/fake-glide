package com.example.fakeglide.transformation


import android.graphics.Bitmap
import androidx.core.graphics.createBitmap

class RoundedCornerCrop(private val radius: Float): Transformation {
    override fun key(): String = "ROUNDED_CORNER_CROP_$radius"

    override fun transform(bitmap: Bitmap): Bitmap {
        val output = createBitmap(bitmap.width, bitmap.height)
        val canvas = android.graphics.Canvas(output)

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            shader = android.graphics.BitmapShader(bitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
        }

        val rect = android.graphics.RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        canvas.drawRoundRect(rect, radius, radius, paint)

//        bitmap.recycle()
        return output
    }
}


