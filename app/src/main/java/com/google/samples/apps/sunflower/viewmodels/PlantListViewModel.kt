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

package com.google.samples.apps.sunflower.viewmodels

import androidx.lifecycle.*
import com.google.samples.apps.sunflower.PlantListFragment
import com.google.samples.apps.sunflower.data.Plant
import com.google.samples.apps.sunflower.data.PlantRepository
import com.google.samples.apps.sunflower.network.client.SimpleHttpClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject
import kotlin.math.*

/**
 * The ViewModel for [PlantListFragment].
 */
@HiltViewModel
class PlantListViewModel @Inject internal constructor(
    plantRepository: PlantRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val growZone: MutableStateFlow<Int> = MutableStateFlow(
        savedStateHandle[GROW_ZONE_SAVED_STATE_KEY] ?: NO_GROW_ZONE
    )

    val plants: LiveData<List<Plant.PlantWithImage>> = growZone.flatMapLatest { zone ->
        val actualPlants = if (zone == NO_GROW_ZONE) {
            plantRepository.getPlants()
        } else {
            plantRepository.getPlantsWithGrowZoneNumber(zone)
        }

        actualPlants.map { plants ->
            val baseUrl = plants.fold(plants.first().imageUrl) { baseUrl, plant ->
                val urlLength = fun(): Int {
                    val lengthToCompare = min(baseUrl.length, plant.imageUrl.length)
                    for (index in 0 until lengthToCompare) {
                        if (baseUrl[index] != plant.imageUrl[index]) {
                            return index + 1
                        }
                    }
                    return lengthToCompare
                }()


                baseUrl.take(urlLength)
            }

            val httpClient = SimpleHttpClient(URL(baseUrl))

            plants.map { plant ->
                plant.withImageLoader(httpClient)
            }
        }
    }.asLiveData()

    init {

        /**
         * When `growZone` changes, store the new value in `savedStateHandle`.
         *
         * There are a few ways to write this; all of these are equivalent. (This info is from
         * https://github.com/android/sunflower/pull/671#pullrequestreview-548900174)
         *
         * 1) A verbose version:
         *
         *    viewModelScope.launch {
         *        growZone.onEach { newGrowZone ->
         *            savedStateHandle.set(GROW_ZONE_SAVED_STATE_KEY, newGrowZone)
         *        }
         *    }.collect()
         *
         * 2) A simpler version of 1). Since we're calling `collect`, we can consume
         *    the elements in the `collect`'s lambda block instead of using the `onEach` operator.
         *    This is the version that's used in the live code below.
         *
         * 3) We can avoid creating a new coroutine using the `launchIn` terminal operator. In this
         *    case, `onEach` is needed because `launchIn` doesn't take a lambda to consume the new
         *    element in the Flow; it takes a `CoroutineScope` that's used to create a coroutine
         *    internally.
         *
         *    growZone.onEach { newGrowZone ->
         *        savedStateHandle.set(GROW_ZONE_SAVED_STATE_KEY, newGrowZone)
         *    }.launchIn(viewModelScope)
         */
        viewModelScope.launch {
            growZone.collect { newGrowZone ->
                savedStateHandle.set(GROW_ZONE_SAVED_STATE_KEY, newGrowZone)
            }
        }
    }

    fun setGrowZoneNumber(num: Int) {
        growZone.value = num
    }

    fun clearGrowZoneNumber() {
        growZone.value = NO_GROW_ZONE
    }

    fun isFiltered() = growZone.value != NO_GROW_ZONE

    companion object {
        private const val NO_GROW_ZONE = -1
        private const val GROW_ZONE_SAVED_STATE_KEY = "GROW_ZONE_SAVED_STATE_KEY"
    }
}
