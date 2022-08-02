
package com.google.samples.apps.sunflower.compose.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.google.samples.apps.sunflower.R
import com.google.samples.apps.sunflower.data.Plant
import kotlinx.coroutines.launch

@Composable
fun fetchPlantImage(plant: Plant.PlantWithImage): ImageBitmap {
    val scope = rememberCoroutineScope()
    val default = ImageBitmap.imageResource(R.mipmap.ic_launcher)
    var image by remember { mutableStateOf(default) }

    SideEffect {
        scope.launch { plant.fetchImage()?.let { image = it } }
    }

    return image
}
