package com.example.fakeglide

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fakeglide.compose.FakeGlideImage
import com.example.fakeglide.ui.theme.FakeGlideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FakeGlideTheme {
                DemoLoad1ImageScreen()
//                DemoLoad100ImagesScreen()
            }
        }
    }
}


@Composable
fun FakeGlideApp() {
    var reloaded by remember { mutableStateOf(false) }
    Column {
        if (!reloaded) {
            FakeGlideImage(
                model = "https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00011.png",
                modifier = Modifier
                    .size(300.dp)
                    .background(Color.Red),
                contentDescription = "Demo Image",
                loading = painterResource(R.drawable.ic_menu_gallery),
                failure = painterResource(R.drawable.ic_delete),
//                reqWidth = 200,
//                reqHeight = 200,
            )
        }
        Button(
            onClick = { reloaded = !reloaded },
            modifier = Modifier.padding(20.dp)
        ) {
            Text(if (reloaded) "Reload" else "Cancel")
        }
    }

}

@Composable
fun FakeGlideGallery() {
    val imageUrls = remember {
        (1..100).map { i ->
            "https://picsum.photos/id/$i/200/300"
        }
    }

    LazyColumn(
        modifier = Modifier.padding(20.dp)
    ) {
        items(
            items = imageUrls,
            key = { it }
        ) { url ->

            FakeGlideImage(
                model = url,
                modifier = Modifier
                    .background(Color.LightGray)
                    .size(300.dp)
                    .padding(bottom = 10.dp),
                contentDescription = "Demo Image",
                loading = painterResource(R.drawable.ic_menu_gallery),
                failure = painterResource(R.drawable.ic_delete),
                contentScale = ContentScale.Crop,
                requestBuilderTransform = { circleCrop() }
            )

        }
    }
}

@Composable
fun FakeLoaderDemo() {
    var model by remember { mutableStateOf("https://images.pexels.com/photos/2662116/pexels-photo-2662116.jpeg") }

    var clicked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row {
            Button(onClick = {
                model = "https://images.pexels.com/photos/2662116/pexels-photo-2662116.jpeg"
            }) {
                Text("Load URL 1")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                model =
                    "https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00002.png"
            }) {
                Text("Load URL 2")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
        key(clicked) {
            FakeGlideImage(
                model = model,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(Color.Red),
                loading = painterResource(id = R.drawable.ic_menu_gallery),
                failure = painterResource(id = R.drawable.ic_delete),
                contentDescription = "My image",
                contentScale = ContentScale.Crop
                //            requestBuilderTransform = {
                //                    circleCrop()
                //            }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { clicked = !clicked }
        ) {
            Text("Click me")
        }
    }
}
