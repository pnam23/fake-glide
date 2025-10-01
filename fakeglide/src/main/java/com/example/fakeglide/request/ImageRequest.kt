package com.example.fakeglide.request

data class ImageRequest(
    val url: String,
    val reqWidth: Int,
    val reqHeight: Int
)