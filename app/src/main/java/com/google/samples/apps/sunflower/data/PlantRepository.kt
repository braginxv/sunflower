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
import kotlinx.coroutines.flow.map
import org.techlook.net.client.kotlin.NetgymHttpClient
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

    fun getPlants(): Flow<List<Plant>> = plantDao.getPlants()

    fun plantImageBaseUrl(): Flow<String?> = getPlants().map { loadedPlants ->
        NetgymHttpClient.baseUrlFor(loadedPlants.mapNotNull { it.imageUrl })
    }

    fun getPlant(plantId: String) = plantDao.getPlant(plantId)

    fun getPlantsWithGrowZoneNumber(growZoneNumber: Int): Flow<List<Plant>> =
        plantDao.getPlantsWithGrowZoneNumber(growZoneNumber)

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: PlantRepository? = null

        fun getInstance(plantDao: PlantDao) =
            instance ?: synchronized(this) {
                instance ?: PlantRepository(plantDao).also { instance = it }
            }
    }
}
