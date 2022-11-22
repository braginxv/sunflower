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
import androidx.room.PrimaryKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.techlook.net.client.kotlin.ConnectionLifetime
import org.techlook.net.client.kotlin.NetgymHttpClient
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
    inner class PlantWithImage internal constructor(
        private val fetching: Pair<String, NetgymHttpClient>?
    ) {
        val plantId = this@Plant.plantId
        val name = this@Plant.name
        val description = this@Plant.description
        val growZoneNumber = this@Plant.growZoneNumber
        val wateringInterval = this@Plant.wateringInterval

        suspend fun fetchImage(): ImageBitmap? {
            val response = withContext(Dispatchers.IO) {
                fetching?.let { (imagePath, httpClient) ->
                    httpClient.rawGet(imagePath)
                }
            } ?: return null

            if (HttpURLConnection.HTTP_OK != response.code) {
                throw IllegalStateException(ERROR_RESPONSE.format(imageUrl, response.code))
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

    fun withImageLoader(
        baseURL: String?,
        httpClient: NetgymHttpClient?
    ): PlantWithImage {
        val imagePath = baseURL?.let { baseUrl ->
            imageUrl?.replace(baseUrl, "")
        }

        return PlantWithImage(imagePath?.let {
            imagePath to
                    (httpClient ?: NetgymHttpClient(URL(baseURL), ConnectionLifetime.Pipelining))
        } ?: imageUrl?.let {
            val url = URL(imageUrl)
            url.file to
                    NetgymHttpClient(URL("${url.protocol}://${url.host}"),
                        ConnectionLifetime.Closable)
        })
    }

    /**
     * Determines if the plant should be watered.  Returns true if [since]'s date > date of last
     * watering + watering Interval; false otherwise.
     */
    fun shouldBeWatered(since: Calendar, lastWateringDate: Calendar) =
        since > lastWateringDate.apply { add(DAY_OF_YEAR, wateringInterval) }

    override fun toString() = name

    companion object {
        const val ERROR_RESPONSE = "Image '%s' wasn't loaded. The server returned a bad response: %d"
    }
}

