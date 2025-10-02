package com.example.fakeglide.cache

import android.graphics.Bitmap

class CacheRepositoryImpl(
    val memoryCache: MemoryCache,
    val diskCache: DiskCache,
) : CacheRepository {
    override suspend fun put(url: String, bitmap: Bitmap) {
        memoryCache.put(url, bitmap)
        diskCache.put(url, bitmap)
    }

    override suspend fun get(url: String): Bitmap? {
        return memoryCache.get(url) ?: diskCache.get(url)?.also { memoryCache.put(url, it) }
    }

    override suspend fun clear() {
        memoryCache.clear()
        diskCache.clear()
    }
}