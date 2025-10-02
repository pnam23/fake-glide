package com.example.fakeglide.compose

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.fakeglide.core.ImageLoader
import com.example.fakeglide.request.ImageRequest
import com.example.fakeglide.request.RequestBuilder
import com.example.fakeglide.transformation.Transformation
import com.example.fakeglide.util.ComposeSizeResolver

@Composable
fun FakeGlideImage(
    model: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    loading: Painter? = null,
    failure: Painter? = null,
    reqWidth: Int? = null,
    reqHeight: Int? = null,
    contentScale: ContentScale = ContentScale.Fit,
    requestBuilderTransform: (RequestBuilder.() -> RequestBuilder)? = null,
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.getInstance(context) }
    val bitmapState = remember { mutableStateOf<ImageBitmap?>(null) }
    val state = remember { mutableStateOf<ImageState>(ImageState.Loading) }
    val size = remember(modifier) {
        mutableStateOf<IntSize?>(null)
    }

    val (width, height) =
        ComposeSizeResolver.resolve(reqWidth, reqHeight, size.value)

    val finalReqWidth by remember { mutableStateOf(width) }
    val finalReqHeight by remember { mutableStateOf(height) }



    // build request with transform
    val request = remember(model, finalReqWidth, finalReqHeight) {
        var builder = RequestBuilder(model).override(finalReqWidth, finalReqHeight)
        if (requestBuilderTransform != null) {
            builder = builder.requestBuilderTransform()
        }
        builder.build()
    }

    LaunchedEffect(model) {
        state.value = ImageState.Loading

        val bitmap = imageLoader.load2(request)

        if (bitmap != null) {
            bitmapState.value = bitmap.asImageBitmap()
            state.value = ImageState.Success
        } else {
            state.value = ImageState.Error
        }
    }

    when (state.value) {
        is ImageState.Loading -> loading?.let {
            Image(painter = it, contentDescription = "Loading", modifier = modifier)
        }

        is ImageState.Success -> bitmapState.value?.let {
            Image(
                bitmap = it,
                contentDescription = contentDescription,
                modifier = modifier.onSizeChanged {
                    size.value = it
                },
                contentScale = contentScale
            )
        }

        is ImageState.Error -> failure?.let {
            Image(painter = it, contentDescription = "Error", modifier = modifier)
        }
    }
}

sealed class ImageState {
    object Loading : ImageState()
    object Success : ImageState()
    object Error : ImageState()
}