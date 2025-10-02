package com.example.fakeglide.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.example.fakeglide.cache.CacheConfig
import com.example.fakeglide.cache.CacheRepositoryImpl
import com.example.fakeglide.cache.DiskCache
import com.example.fakeglide.cache.MemoryCache
import com.example.fakeglide.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "FG_ImageLoader"

class ImageLoader private constructor(context: Context, config: CacheConfig) {
    companion object {
        @Volatile
        private var instance: ImageLoader? = null

        fun getInstance(context: Context, config: CacheConfig = CacheConfig()): ImageLoader {
            return instance ?: synchronized(this) {
                instance ?: ImageLoader(context.applicationContext, config).also { instance = it }
            }
        }
    }

    private val cache = CacheRepositoryImpl(
        memoryCache = MemoryCache(config.memoryCacheSize),
        diskCache = DiskCache(context, config.diskCacheSize)
    )

    private val okHttpClient = OkHttpClient()

    suspend fun load(request: ImageRequest): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val key = genCacheKey(
                    url = request.url,
                    reqWidth = request.reqWidth,
                    reqHeight = request.reqHeight
                )

                // check cache
                cache.get(key)?.let { bitmap ->
                    return@withContext bitmap
                }

                // fetch from network
                val bitmap =
                    fetchFromNetwork(request.url, request.reqWidth, request.reqHeight)
                        ?: return@withContext null

                // put to cache
                cache.put(key, bitmap)

                return@withContext bitmap

            } catch (e: CancellationException) {
                Log.e(TAG, "cancelled: ${request.url}")
                throw e
            }
        }
    }

    private suspend fun fetchFromNetwork(url: String, reqWidth: Int, reqHeight: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()

            val startTime = System.currentTimeMillis()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val body = response.body ?: return@withContext null

                // Ghi xuống file cache tạm để decode 2 lần
                val tempFile = File.createTempFile("img_cache", null)
                body.byteStream().use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Decode lần 1: bounds
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(tempFile.absolutePath, boundsOptions)

                // Tính sample size
                val sampleSize = calculateInSampleSize(
                    options = boundsOptions,
                    reqWidth = reqWidth,
                    reqHeight = reqHeight
                )

                // Decode lần 2
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565
                }

                val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath, decodeOptions)

                tempFile.delete()
                val endTime = System.currentTimeMillis()
                Log.d(
                    TAG,
                    "Network: $url, time=${endTime - startTime}ms, original=${boundsOptions.outWidth}x${boundsOptions.outHeight}, sampleSize=$sampleSize, decoded=${bitmap?.width}x${bitmap?.height}"
                )
                return@withContext bitmap
            }
        }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun genCacheKey(url: String, reqWidth: Int, reqHeight: Int): String {
        return "$url,$reqWidth,$reqHeight"
    }
}
