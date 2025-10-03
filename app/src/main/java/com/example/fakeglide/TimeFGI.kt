package com.example.fakeglide

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.fakeglide.compose.ImageState
import com.example.fakeglide.core.ImageLoader
import com.example.fakeglide.request.RequestBuilder
import com.example.fakeglide.util.ComposeSizeResolver

@Composable
fun TimedFakeGlideImage(
    model: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    loading: Painter? = null,
    failure: Painter? = null,
    reqWidth: Int? = null,
    reqHeight: Int? = null,
    contentScale: ContentScale = ContentScale.Fit,
    requestBuilderTransform: (RequestBuilder.() -> RequestBuilder)? = null,
    onFinished: ((loadDuration: Long, displayDuration: Long?, success: Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader.getInstance(context) }

    var bitmapState by remember { mutableStateOf<ImageBitmap?>(null) }
    var state by remember { mutableStateOf<ImageState>(ImageState.Loading) }
    var size by remember { mutableStateOf<IntSize?>(null) }

    // để giữ thời điểm bắt đầu và load xong
    val startTime = remember(model) { System.currentTimeMillis() }
    var loadCompleteTime by remember { mutableStateOf<Long?>(null) }

    val (finalReqWidth, finalReqHeight) = ComposeSizeResolver.resolve(
        reqWidth,
        reqHeight,
        size
    )

    // build request with transform
    val request = remember(model, finalReqWidth, finalReqHeight) {
        var builder = RequestBuilder(model).override(finalReqWidth, finalReqHeight)
        requestBuilderTransform?.let { builder = builder.it() }
        builder.build()
    }

    LaunchedEffect(model) {
        state = ImageState.Loading
        val bitmap = imageLoader.load2(request)

        val now = System.currentTimeMillis()
        val loadDuration = now - startTime

        if (bitmap != null) {
            bitmapState = bitmap.asImageBitmap()
            state = ImageState.Success
            loadCompleteTime = now
            // báo trước thời gian load xong bitmap
            onFinished?.invoke(loadDuration, null, true)
        } else {
            state = ImageState.Error
            onFinished?.invoke(loadDuration, null, false)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (state) {
            is ImageState.Loading -> loading?.let {
                Image(painter = it, contentDescription = "Loading", modifier = modifier)
            }

            is ImageState.Success -> bitmapState?.let { bmp ->
                Image(
                    bitmap = bmp,
                    contentDescription = contentDescription,
                    modifier = modifier
                        .onSizeChanged { newSize -> size = newSize }
                        .drawWithContent {
                            drawContent()
                            // chỉ callback khi lần đầu render
                            loadCompleteTime?.let { loadDoneAt ->
                                val displayDuration = System.currentTimeMillis() - startTime
                                onFinished?.invoke(
                                    loadDoneAt - startTime,
                                    displayDuration,
                                    true
                                )
                                loadCompleteTime = null // tránh gọi nhiều lần
                            }
                        },
                    contentScale = contentScale
                )
            }

            is ImageState.Error -> failure?.let {
                Image(painter = it, contentDescription = "Error", modifier = modifier)
            }
        }
    }
}


@Composable
fun SingleImageWithTime_FGI(url: String, modifier: Modifier = Modifier) {
    var duration by remember { mutableStateOf<Long?>(null) }
    var success by remember { mutableStateOf<Boolean?>(null) }
    var displayed by remember { mutableStateOf<Boolean>(false) }

    Column {
        TimedFakeGlideImage(
            model = url,
            modifier = modifier,
            loading = painterResource(android.R.drawable.ic_menu_gallery),
            onFinished = { loadDuration, displayDuration, isSuccess ->
                if (isSuccess) {
                    if (displayDuration == null) {
                        // mới chỉ load bitmap xong
                        duration = loadDuration
                        success = true
                        displayed = false
                    } else {
                        // đã render trên UI
                        duration = displayDuration
                        success = true
                        displayed = true
                    }
                } else {
                    duration = loadDuration
                    success = false
                    displayed = false
                }
            }
        )

        if (duration != null) {
            Text(
                text = when {
                    success == true && displayed -> "FGI - Displayed in ${duration} ms"
                    success == true -> "Loaded in ${duration} ms"
                    else -> "Failed after ${duration} ms"
                }
            )
        }
    }
}



@Composable
fun ImageListTotalTime_FGI(urls: List<String>, modifier: Modifier = Modifier) {
    // thời điểm bắt đầu (tính lúc composable render lần đầu)
    val startTime = remember { System.currentTimeMillis() }
    var allDisplayedTime by remember { mutableStateOf<Long?>(null) }
    val displayedSet = remember { mutableStateSetOf<String>() }

    Column {

        allDisplayedTime?.let {
            Text("Total time: $it ms")
        }
        Spacer(modifier = Modifier.height(16.dp))


        urls.forEach { url ->
            TimedFakeGlideImage(
                model = url,
                modifier = modifier,
                loading = painterResource(android.R.drawable.ic_menu_gallery),
                failure = painterResource(android.R.drawable.ic_delete),
                onFinished = { _, displayDuration, success ->
                    if (success && displayDuration != null) {
                        displayedSet.add(url)
                        if (displayedSet.size == urls.size) {
                            // tất cả ảnh đã display -> lấy thời gian hiện tại
                            allDisplayedTime = System.currentTimeMillis() - startTime
                        }
                    }
                }
            )
        }


    }
}

