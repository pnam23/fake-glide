package com.example.fakeglide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fakeglide.compose.FakeGlideImage
import com.example.fakeglide.ui.theme.FakeGlideTheme
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FakeGlideTheme {
                FakeLoaderDemo()
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
                model = "https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00023.png",
                modifier = Modifier,
                contentDescription = "Demo Image",
                loading = painterResource(android.R.drawable.ic_menu_gallery),
                failure = painterResource(android.R.drawable.ic_delete),
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

    LazyColumn {
        items(
            items = imageUrls,
            key = { it }
        ) { url ->

            FakeGlideImage(
                model = url,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(300.dp)
                    .padding(bottom = 10.dp),
                contentDescription = "Demo Image",
                loading = painterResource(android.R.drawable.ic_menu_gallery),
                failure = painterResource(android.R.drawable.ic_delete),
            )

        }
    }
}

@Composable
fun FakeLoaderDemo() {
    var model by remember { mutableStateOf("https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00049.png") }

var log by remember { mutableStateOf("Idle") }

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row {
            Button(onClick = { model = "https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00049.png" }) {
                Text("Load URL 1")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { model = "https://svs.gsfc.nasa.gov/vis/a020000/a020200/a020255/frames/3840x2160_16x9_60p/Shot48/Shot48Frames/Shot48.00002.png" }) {
                Text("Load URL 2")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status: $log")
        Spacer(modifier = Modifier.height(16.dp))
        FakeGlideImage(
            model = model,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            loading = painterResource(id = android.R.drawable.ic_menu_gallery),
            failure = painterResource(id = android.R.drawable.ic_delete)
        )

    }
}
