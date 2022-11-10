/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.samples.apps.sunflower.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository module for handling data operations.
 *
 * Collecting from the Flows in [PlantDao] is main-safe.  Room supports Coroutines and moves the
 * query execution off of the main thread.
 */
@Singleton
class PlantRepository @Inject constructor(private val plantDao: PlantDao) {
    private val plants: List<Plant> = listOf(
        Plant(
            "1", "magnolia",
            "beautiful magnolia", 5,
            imageUrl = "https://192.168.22.9/example/images/60544-cvetenie_vetka_rozovyj.jpg"
        ),
        Plant(
            "2",
            "magnolia excellent",
            "very beautiful magnolia",
            4,
            imageUrl = "https://192.168.22.9/example/images/61216-zavtrak_pirozhnoe_mindal.jpg"
        ),
        Plant(
            "3", "plant",
            "simple plant", 4,
            imageUrl = "https://192.168.22.9/example/images/61440-kamni_ostrov_okean.jpg"
        ),
        Plant(
            "4",
            "yet another plant",
            "plant",
            6,
            imageUrl = "https://192.168.22.9/example/images/61709-minimalizm_poezd_gory.jpg"
        ),
    )

    fun getPlants(): Flow<List<Plant>> = flowOf(plants)

    fun getPlant(plantId: String): Flow<Plant> = flowOf(plants.find { it.plantId == plantId }!!)

    fun getPlantsWithGrowZoneNumber(growZoneNumber: Int): Flow<List<Plant>> =
        flowOf(plants.filter { it.growZoneNumber == growZoneNumber })

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: PlantRepository? = null

        fun getInstance(plantDao: PlantDao): PlantRepository =
            instance ?: synchronized(this) {
                instance ?: PlantRepository(plantDao).also { instance = it }
            }
    }
}
