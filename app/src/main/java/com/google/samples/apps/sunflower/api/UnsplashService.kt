/*
 * Copyright 2020 Google LLC
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

package com.google.samples.apps.sunflower.api

import com.google.gson.FieldNamingStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.samples.apps.sunflower.BuildConfig
import com.google.samples.apps.sunflower.data.UnsplashSearchResponse
import com.google.samples.apps.sunflower.network.client.SimpleHttpClient
import java.net.URL

/**
 * Used to connect to the Unsplash API to fetch photos
 */
class UnsplashService {
    private val client = SimpleHttpClient(URL(BASE_URL))

    suspend fun searchPhotos(query: String, page: Int, perPage: Int,
                             clientId: String = BuildConfig.UNSPLASH_ACCESS_KEY): UnsplashSearchResponse {
        val parameters: Set<Pair<String, String>> = setOf(
            "query" to query,
            "page" to page.toString(),
            "per_page" to perPage.toString(),
            "client_id" to clientId
        )

        val response = client.GET(PHOTOS, parameters = parameters)

        if (response.code >= 400) {
            throw IllegalAccessException("requested photos aren't available")
        }

        val gson = GsonBuilder().create()
        return gson.fromJson(response.content, UnsplashSearchResponse::class.java)
    }

    suspend fun downloadPhoto(url: String): ByteArray {
        return client.rawGET(url).content
    }

    companion object {
        private const val BASE_URL = "https://api.unsplash.com/"
        private const val PHOTOS = "search/photos"

        fun create(): UnsplashService {
            return UnsplashService()
        }
    }
}
