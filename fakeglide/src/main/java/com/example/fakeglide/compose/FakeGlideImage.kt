package com.example.fakeglide.compose

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.example.fakeglide.core.ImageLoader
import com.example.fakeglide.request.ImageRequest
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
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.getInstance(context) }
    val bitmapState = remember { mutableStateOf<ImageBitmap?>(null) }
    val state = remember { mutableStateOf<ImageState>(ImageState.Loading) }
    val size = remember(modifier) {
        mutableStateOf<IntSize?>(null)
    }

    val (finalReqWidth, finalReqHeight) =
        ComposeSizeResolver.resolve(reqWidth, reqHeight, size.value)


    LaunchedEffect(model, size.value) {
        state.value = ImageState.Loading

        val request = ImageRequest(
            url = model,
            reqWidth = finalReqWidth,
            reqHeight = finalReqHeight
        )

        val bitmap = imageLoader.load(request)

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
                })
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