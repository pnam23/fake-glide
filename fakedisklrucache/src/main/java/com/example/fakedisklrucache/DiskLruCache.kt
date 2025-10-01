package com.example.fakedisklrucache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.*

class DiskLruCache(
    private val cacheDir: File,
    private val maxSizeBytes: Long,
) {
    private val cacheMap = object : LinkedHashMap<String, CacheEntry>(0, 0.75f, true) {}
    private var currentSize: Long = 0L
    private val mutex = Mutex()

    private val journalFile = File(cacheDir, "journal.txt")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        loadJournal()
    }

    /**
     * Đọc lại journal khi khởi tạo
     */
    private fun loadJournal() {
        if (!journalFile.exists()) return

        val dirtyKeys = mutableSetOf<String>()

        try {
            journalFile.forEachLine { line ->
                val parts = line.split(" ")
                if (parts.isEmpty()) return@forEachLine

                when (parts[0]) {
                    "DIRTY" -> {
                        if (parts.size >= 2) dirtyKeys.add(parts[1])
                    }

                    "CLEAN" -> {
                        if (parts.size >= 3) {
                            val key = parts[1]
                            val size = parts[2].toLongOrNull() ?: 0L
                            val file = File(cacheDir, key)
                            if (file.exists()) {
                                cacheMap[key] = CacheEntry(file, size)
                                currentSize += size
                                dirtyKeys.remove(key)
                            }
                        }
                    }

                    "REMOVE" -> {
                        if (parts.size >= 2) {
                            val key = parts[1]
                            cacheMap.remove(key)
                            File(cacheDir, key).delete()
                            dirtyKeys.remove(key)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.e("DiskLruCache", "Error reading journal", e)
        }

        // Xóa các file còn đang DIRTY mà không CLEAN
        for (dirtyKey in dirtyKeys) {
            File(cacheDir, dirtyKey).delete()
        }

        evictIfNeeded()
    }

    private fun appendJournal(line: String) {
        try {
            journalFile.appendText(line + "\n")
        } catch (e: IOException) {
            Log.e("DiskLruCache", "Error writing journal", e)
        }
    }

    /**
     * Lưu bitmap xuống cache
     */
    suspend fun put(key: String, bitmap: Bitmap) = mutex.withLock {
        val tempFile = File(cacheDir, "$key.tmp")
        val realFile = File(cacheDir, key)

        try {
            appendJournal("DIRTY $key")

            FileOutputStream(tempFile).use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    throw IOException("Bitmap compress failed for key=$key")
                }
            }

            if (tempFile.renameTo(realFile)) {
                val size = realFile.length()
                cacheMap[key] = CacheEntry(realFile, size)
                currentSize += size
                appendJournal("CLEAN $key $size")
                evictIfNeeded()
            } else {
                throw IOException("Rename failed for key=$key")
            }
        } catch (e: IOException) {
            tempFile.delete()
            appendJournal("REMOVE $key")
            Log.e("DiskLruCache", "Error writing bitmap for key=$key", e)
        }
    }

    /**
     * Đọc bitmap từ cache
     */
    suspend fun get(key: String): Bitmap? = mutex.withLock {
        val entry = cacheMap[key] ?: return null
        return safeDecode(entry.file)
    }

    /**
     * Kiểm tra key có tồn tại không
     */
    suspend fun containsKey(key: String): Boolean = mutex.withLock {
        cacheMap.containsKey(key)
    }

    /**
     * Xóa 1 entry
     */
    suspend fun remove(key: String) = mutex.withLock {
        val entry = cacheMap.remove(key) ?: return@withLock
        currentSize -= entry.size
        entry.file.delete()
        appendJournal("REMOVE $key")
    }

    /**
     * Xóa toàn bộ cache
     */
    suspend fun clear() = mutex.withLock {
        cacheMap.values.forEach { it.file.delete() }
        cacheMap.clear()
        currentSize = 0L
        journalFile.delete()
        journalFile.createNewFile()
    }

    /**
     * Nếu vượt quá maxSize thì xóa file cũ
     */
    private fun evictIfNeeded() {
        if (currentSize <= maxSizeBytes) return

        val iterator = cacheMap.entries.iterator()
        while (iterator.hasNext() && currentSize > maxSizeBytes) {
            val entry = iterator.next()
            currentSize -= entry.value.size
            entry.value.file.delete()
            iterator.remove()
            appendJournal("REMOVE ${entry.key}")
        }
    }

    private fun safeDecode(file: File): Bitmap? {
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            file.delete()
            appendJournal("REMOVE ${file.name}")
            Log.e("DiskLruCache", "Error decoding file=${file.name}", e)
            null
        }
    }

    private data class CacheEntry(
        val file: File,
        val size: Long,
    )
}
