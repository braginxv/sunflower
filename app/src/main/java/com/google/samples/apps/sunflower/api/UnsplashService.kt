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

import com.google.gson.GsonBuilder
import com.google.samples.apps.sunflower.BuildConfig
import com.google.samples.apps.sunflower.data.UnsplashSearchResponse
import org.techlook.net.client.kotlin.ConnectionLifetime
import org.techlook.net.client.kotlin.NetgymHttpClient
import java.net.URL

/**
 * Used to connect to the Unsplash API to fetch photos
 */
class UnsplashService {
    private val client = NetgymHttpClient(URL(BASE_URL), ConnectionLifetime.Pipelining)

    suspend fun searchPhotos(query: String, page: Int, perPage: Int,
                             clientId: String = BuildConfig.UNSPLASH_ACCESS_KEY): UnsplashSearchResponse {
        val parameters = mapOf(
            "query" to query,
            "page" to page.toString(),
            "per_page" to perPage.toString(),
            "client_id" to clientId
        )

        val response = client.get(PHOTOS, parameters = parameters)

        if (response.code >= 400) {
            throw IllegalAccessException("requested photos aren't available")
        }

        val gson = GsonBuilder().create()
        return gson.fromJson(response.content, UnsplashSearchResponse::class.java)
    }

    suspend fun downloadPhoto(url: String): ByteArray {
        return client.rawGet(url).content
    }

    companion object {
        private const val BASE_URL = "https://api.unsplash.com/"
        private const val PHOTOS = "search/photos"

        fun create(): UnsplashService {
            return UnsplashService()
        }
    }
}
