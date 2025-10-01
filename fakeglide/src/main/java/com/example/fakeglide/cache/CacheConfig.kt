package com.example.fakeglide.cache

class CacheConfig(
    val maxMemory: Long = Runtime.getRuntime().maxMemory() / 1024, // in KB
    val memoryCacheSize: Int = when {
        maxMemory < 64 * 1024 -> (maxMemory / 8).toInt() // < 64MB use 1/8th of memory
        maxMemory < 256 * 1024 -> (maxMemory / 6).toInt() // < 256MB use 1/6th of memory
        else -> (maxMemory / 4).toInt()
    },
    val diskCacheSize: Long = 5 * 1024 * 1024,
)