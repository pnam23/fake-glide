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
import com.example.fakeglide.transformation.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume

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

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()


    suspend fun load2(request: ImageRequest): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // key for original image
            val baseKey = genCacheKey(
                url = request.url,
                reqWidth = request.reqWidth,
                reqHeight = request.reqHeight
            )

            val finalKey = if (request.transformations.isNotEmpty()) {
                genCacheKey(
                    url = request.url,
                    reqWidth = request.reqWidth,
                    reqHeight = request.reqHeight,
                    transformations = request.transformations
                )
            } else baseKey

            cache.get(finalKey)?.let { return@withContext it }

            // if finalKey == baseKey and transformations not empty -> apply transformations on original
            if (request.transformations.isNotEmpty()) {
                cache.get(baseKey)?.let { original ->
                    val transformed = applyTransformations(original, request.transformations)
                    cache.memoryCache.put(finalKey, transformed)
                    return@withContext transformed
                }
            }

            // fetch from network
            val networkBitmap = fetchFromNetwork(request.url, request.reqWidth, request.reqHeight)
                ?: return@withContext null

            // save to mem + disk cache
            cache.put(baseKey, networkBitmap)

            // apply transformations if fetch from network
            if (request.transformations.isNotEmpty()) {
                val transformed = applyTransformations(networkBitmap, request.transformations)
                cache.memoryCache.put(finalKey, transformed)
                return@withContext transformed
            }

            return@withContext networkBitmap

        } catch (e: CancellationException) {
            Log.e(TAG, "Cancelled: ${request.url}")
            throw e
        }
    }


    private fun applyTransformations(
        bitmap: Bitmap,
        transformations: List<Transformation>,
    ): Bitmap {
        var result = bitmap
        transformations.forEach { t ->
            result = t.transform(result)
        }
        return result
    }

    private suspend fun fetchFromNetwork(url: String, reqWidth: Int, reqHeight: Int): Bitmap? =
        suspendCancellableCoroutine { cont ->
            val request = Request.Builder().url(url).build()
            val startTime = System.currentTimeMillis()
            val call = okHttpClient.newCall(request)

            cont.invokeOnCancellation {
                call.cancel() // khi coroutine bị cancel thì hủy HTTP call
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!cont.isCancelled) cont.resume(null)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        cont.resume(null)
                        return
                    }
                    val body = response.body ?: return cont.resume(null)

                    try {
                        // Đọc body → file tạm
                        val tempFile = File.createTempFile("img_cache", null)
                        body.byteStream().use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }

                        // Decode bounds
                        val boundsOptions =
                            BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        BitmapFactory.decodeFile(tempFile.absolutePath, boundsOptions)

                        val sampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)

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

                        cont.resume(bitmap)
                    } catch (e: Exception) {
                        cont.resume(null)
                    }
                }
            })
        }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
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

    private fun genCacheKey(
        url: String, reqWidth: Int, reqHeight: Int,
        transformations: List<Transformation> = emptyList(),
    ): String {
        val transformedKey = transformations?.joinToString(separator = "_") { it.key() }
        val key = if (transformedKey.isNullOrEmpty()) {
            "$url,${reqWidth}x${reqHeight}"
        } else {
            "$url,${reqWidth}x${reqHeight},$transformedKey"
        }

        return key
    }
}
