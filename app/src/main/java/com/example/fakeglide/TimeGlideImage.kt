package com.example.fakeglide

import android.R
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SingleImageWithTime_GI(
    model: String,
    modifier: Modifier = Modifier,
) {
    var loadTime by remember { mutableStateOf<Long?>(null) }
    var displayTime by remember { mutableStateOf<Long?>(null) }
    val startTime = remember(model) { System.currentTimeMillis() }
    var source by remember { mutableStateOf<String?>(null) }
    var loadCompleted by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlideImage(
            model = model,
            contentDescription = "Glide image",
            modifier = modifier.drawWithContent {
                drawContent()
                if (loadCompleted && displayTime == null) {
                    // Ảnh thực sự được vẽ ra UI
                    displayTime = System.currentTimeMillis() - startTime
                }
            },
            loading = placeholder(R.drawable.ic_menu_gallery),
            requestBuilderTransform = { requestBuilder ->
                requestBuilder.listener(
                    object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean,
                        ): Boolean {
                            source = "Failed"
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean,
                        ): Boolean {
                            source = dataSource.name
                            loadTime = System.currentTimeMillis() - startTime
                            loadCompleted = true
                            return false
                        }
                    }
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when {
            displayTime != null -> Text("GI - Displayed in ${displayTime} ms ($source)")
            loadTime != null -> Text("⏳ Loaded in ${loadTime} ms ($source)")
            else -> Text("Loading...")
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageListTotalTime_GI(
    urls: List<String>,
    modifier: Modifier = Modifier,
) {
    val startTime = remember { System.currentTimeMillis() }
    var allDisplayedTime by remember { mutableStateOf<Long?>(null) }
    val displayedSet = remember { mutableStateSetOf<String>() }

    Column {

        allDisplayedTime?.let {
            Text("Total time: $it ms")
        }
        Spacer(modifier = Modifier.height(12.dp))

        urls.forEach { url ->
            var displayTime by remember { mutableStateOf<Long?>(null) }
            var loadCompleted by remember { mutableStateOf(false) }

            GlideImage(
                model = url,
                contentDescription = null,
                modifier = modifier
                    .drawWithContent {
                        drawContent()
                        if (loadCompleted && displayTime == null) {
                            displayTime = System.currentTimeMillis() - startTime
                            displayedSet.add(url)
                            if (displayedSet.size == urls.size) {
                                // tất cả ảnh đã display
                                allDisplayedTime = System.currentTimeMillis() - startTime
                            }
                        }
                    },
                loading = placeholder(android.R.drawable.ic_menu_gallery),
                requestBuilderTransform = { rb ->
                    rb.listener(
                        object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>,
                                isFirstResource: Boolean,
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                model: Any,
                                target: Target<Drawable>,
                                dataSource: DataSource,
                                isFirstResource: Boolean,
                            ): Boolean {
                                loadCompleted = true
                                return false
                            }
                        }
                    )
                }
            )
        }


    }
}









