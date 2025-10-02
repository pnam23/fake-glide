package com.example.fakeglide.request

import com.example.fakeglide.transformation.Transformation

data class ImageRequest(
    val url: String,
    val reqWidth: Int,
    val reqHeight: Int,
    val transformations: List<Transformation> = emptyList()
)