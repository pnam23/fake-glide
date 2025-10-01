package com.example.fakeglide.cache

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.fakedisklrucache.DiskLruCache
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

private const val TAG = "FG_DiskCache"

class DiskCache(context: Context, maxSizeBytes: Long) : CacheRepository {

    private val cache: DiskLruCache

    init {
        val cacheDir = File(context.cacheDir, "fake_glide_disk_cache")
        cache = DiskLruCache(cacheDir, maxSizeBytes)
    }

    override suspend fun put(url: String, bitmap: Bitmap) {
        val key = hashKeyForDisk(url)
        cache.put(key, bitmap)
    }

    override suspend fun get(url: String): Bitmap? {
        val key = hashKeyForDisk(url)
        val startTime = System.currentTimeMillis()

        val bitmap =
            cache.get(key)

        val endTime = System.currentTimeMillis()
        if (bitmap != null) {
            Log.d(TAG, "Disk cache hit for: $url, time: ${endTime - startTime}ms")
        }

        return bitmap
    }

    override suspend fun clear() {
        cache.clear()

    }

    private fun hashKeyForDisk(key: String): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(key.toByteArray())
        return BigInteger(1, md.digest()).toString(16).padStart(32, '0')
    }
}