package com.example.fakeglide.request

import com.example.fakeglide.transformation.CircleCrop
import com.example.fakeglide.transformation.RoundedCornerCrop
import com.example.fakeglide.transformation.Transformation

class RequestBuilder(private val url: String) {

    private var reqWidth: Int = 0
    private var reqHeight: Int = 0
    private val transformations = mutableListOf<Transformation>()

    fun override(width: Int, height: Int): RequestBuilder {
        reqWidth = width
        reqHeight = height
        return this
    }

    fun circleCrop(): RequestBuilder {
        transformations += CircleCrop()
        return this
    }

    fun roundedCornerCrop(radius: Float): RequestBuilder{
        transformations += RoundedCornerCrop(radius)
        return this
    }

    fun transform(t: Transformation): RequestBuilder {
        transformations += t
        return this
    }

    fun build(): ImageRequest {
        return ImageRequest(
            url = url,
            reqWidth = reqWidth,
            reqHeight = reqHeight,
            transformations = transformations.toList()
        )
    }
}
