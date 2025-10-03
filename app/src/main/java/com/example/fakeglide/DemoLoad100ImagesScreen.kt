package com.example.fakeglide


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun DemoLoad100ImagesScreen() {
    val imageUrls = remember {
        (1..150).map { i ->
            "https://picsum.photos/id/$i/200/300"
        }
    }

    var reloaded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Button(
            onClick = { reloaded = !reloaded },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Reload")
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FakeGlide Column
            key(reloaded) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("FGI", modifier = Modifier.padding(8.dp))
                    ImageListTotalTime_FGI(urls = imageUrls)
                }
            }

            // Glide Column
            key(reloaded) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("GI", modifier = Modifier.padding(8.dp))
                    ImageListTotalTime_GI(urls = imageUrls)
                }
            }
        }
    }
}

