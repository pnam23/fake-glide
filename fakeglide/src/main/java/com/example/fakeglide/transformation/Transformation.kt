package com.example.fakeglide.transformation

import android.graphics.Bitmap

interface Transformation {
    fun key(): String
    fun transform(bitmap: Bitmap): Bitmap
}