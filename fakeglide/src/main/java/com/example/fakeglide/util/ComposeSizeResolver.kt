package com.example.fakeglide.util


import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize

object ComposeSizeResolver {
    @Composable
    fun resolve(
        reqWidth: Int?,
        reqHeight: Int?,
        sizeFromLayout: IntSize?
    ): Pair<Int, Int> {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val screenHeight = configuration.screenHeightDp

        val width = reqWidth
            ?: sizeFromLayout?.width?.takeIf { it > 0 }
            ?: screenWidth

        val height = reqHeight
            ?: sizeFromLayout?.height?.takeIf { it > 0 }
            ?: screenHeight

        return width to height
    }
}
