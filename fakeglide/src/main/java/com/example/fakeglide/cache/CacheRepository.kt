package com.example.fakeglide.cache

import android.graphics.Bitmap

interface CacheRepository {
    suspend fun put(url: String, bitmap: Bitmap)
    suspend fun get(url: String): Bitmap?
    suspend fun clear()
}