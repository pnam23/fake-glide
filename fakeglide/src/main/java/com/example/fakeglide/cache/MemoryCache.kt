package com.example.fakeglide.cache

import android.graphics.Bitmap
import android.util.Log
import android.util.LruCache

private const val TAG = "FG_MemoryCache"

class MemoryCache(private val maxSize: Int) : CacheRepository {
    private val cacheMap: LruCache<String, Bitmap> = object : LruCache<String, Bitmap>(maxSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024 // size in KB
        }
    }

    override suspend fun put(url: String, bitmap: Bitmap) {
        cacheMap.put(url, bitmap)
    }

    override suspend fun get(url: String): Bitmap? {
        val startTime = System.currentTimeMillis()
        val bitmap = cacheMap.get(url)
        if (bitmap != null) {
            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Memory cache hit for: $url, time: ${endTime - startTime}ms")
        }
        return bitmap
    }

    override suspend fun clear() {
        cacheMap.evictAll()
    }

}