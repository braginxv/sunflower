package com.google.samples.apps.sunflower.compose.utils

import android.content.res.Resources
import android.graphics.drawable.VectorDrawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.samples.apps.sunflower.R
import com.google.samples.apps.sunflower.data.Plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object ComposableEffects {
    fun default(resources: Resources) = (ResourcesCompat.getDrawable(
        resources,
        R.drawable.ic_my_garden_inactive, null
    ) as VectorDrawable).toBitmap().asImageBitmap()

    fun wrong(resources: Resources) = (ResourcesCompat.getDrawable(
        resources,
        R.drawable.ic_my_garden_active, null
    ) as VectorDrawable).toBitmap().asImageBitmap()


}

fun fetchPlantImage(
    plant: Plant.PlantWithImage,
    resources: Resources,
    scope: CoroutineScope
): LiveData<ImageBitmap> {
    val result = MutableLiveData<ImageBitmap>()

    scope.launch {
        result.value = try {
            plant.fetchImage() ?: ComposableEffects.default(resources)
        } catch (_: Exception) {
            ComposableEffects.wrong(resources)
        }
    }

    return result
}
