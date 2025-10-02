package com.example.fakeglide

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DemoLoad1ImageScreen() {

    val url by remember { mutableStateOf("https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00049.png") }
//    val url by remember { mutableStateOf("https://images.pexels.com/photos/2662116/pexels-photo-2662116.jpeg") }

    var reloaded by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        key(reloaded) {
            SingleImageWithTime_FGI(
                url = url,
                modifier = Modifier.size(200.dp),
            )

        }

        key(reloaded) {
            SingleImageWithTime_GI(
                model = url,
                modifier = Modifier.size(200.dp),
            )

        }

        Button(
            onClick = {
                reloaded = !reloaded
            },
            modifier = Modifier.padding(20.dp)
        ) {
            Text("Reload")
        }
    }

}
