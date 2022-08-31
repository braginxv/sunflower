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

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.samples.apps.sunflower.network.client.NetgymHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.Calendar.DAY_OF_YEAR

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey @ColumnInfo(name = "id") val plantId: String,
    val name: String,
    val description: String,
    val growZoneNumber: Int,
    val wateringInterval: Int = 7, // how often the plant should be watered, in days
    val imageUrl: String? = null
) {
    inner class PlantWithImage(private val httpClient: NetgymHttpClient? = imageUrl?.let { NetgymHttpClient(it) }) {
        val plantId = this@Plant.plantId
        val name = this@Plant.name
        val description = this@Plant.description
        val growZoneNumber = this@Plant.growZoneNumber
        val wateringInterval = this@Plant.wateringInterval
        val resource = imageUrl
            ?.takeIf { httpClient != null && it.startsWith(httpClient.baseUrl) }
            ?.drop(httpClient!!.baseUrl.length)

        suspend fun fetchImage(): ImageBitmap? {
            val response = withContext(Dispatchers.IO) {
                resource?.let { httpClient!!.rawGET(it) }
            } ?: return null

            if (HttpURLConnection.HTTP_OK != response.code) {
                throw IllegalStateException(ERROR_RESPONSE.format(response.code))
            }

            val rawContent = response.content

            return BitmapFactory
                .decodeByteArray(rawContent, 0, rawContent.size)
                .asImageBitmap()
        }

        override fun equals(other: Any?): Boolean {
            return other is PlantWithImage && plant == other.plant
        }

        override fun hashCode(): Int {
            return plant.hashCode()
        }

        private val plant = this@Plant
    }

    fun withImageLoader(httpClient: NetgymHttpClient? = imageUrl?.let { NetgymHttpClient(it) }): PlantWithImage {
        return PlantWithImage(httpClient)
    }

    /**
     * Determines if the plant should be watered.  Returns true if [since]'s date > date of last
     * watering + watering Interval; false otherwise.
     */
    fun shouldBeWatered(since: Calendar, lastWateringDate: Calendar) =
        since > lastWateringDate.apply { add(DAY_OF_YEAR, wateringInterval) }

    override fun toString() = name

    companion object {
        const val ERROR_RESPONSE = "Bad response during the image downloading: %d"
    }
}

