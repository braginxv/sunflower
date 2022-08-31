package com.google.samples.apps.sunflower.compose.utils

import android.content.res.Resources
import android.graphics.drawable.VectorDrawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.samples.apps.sunflower.R
import com.google.samples.apps.sunflower.data.Plant

object ComposableEffects {
    private fun imageTheme(resources: Resources): Resources.Theme {
        val theme = resources.newTheme()
        theme.applyStyle(R.style.Base_Theme_Sunflower, true)

        return theme
    }

    fun default(resources: Resources) = (ResourcesCompat.getDrawable(
        resources,
        R.drawable.ic_my_garden_inactive, imageTheme(resources)
    ) as VectorDrawable).toBitmap().asImageBitmap()

    fun wrong(resources: Resources) = (ResourcesCompat.getDrawable(
        resources,
        R.drawable.ic_my_garden_active, imageTheme(resources)
    ) as VectorDrawable).toBitmap().asImageBitmap()

    fun fetchPlantImage(
        plant: Plant.PlantWithImage,
        resources: Resources
    ): LiveData<ImageBitmap> {
        return liveData {
            try {
                val fetchedImage = plant.fetchImage()
                emit(fetchedImage ?: default(resources))
            } catch (_: Exception) {
                emit(wrong(resources))
            }
        }
    }
}
