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

package com.google.samples.apps.sunflower.data

import android.graphics.BitmapFactory
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.samples.apps.sunflower.api.UnsplashService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val UNSPLASH_STARTING_PAGE_INDEX = 1

class UnsplashPagingSource(
    private val service: UnsplashService,
    private val query: String
) : PagingSource<Int, DownloadablePhoto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, DownloadablePhoto> {
        val page = params.key ?: UNSPLASH_STARTING_PAGE_INDEX
        return try {
            val response = service.searchPhotos(query, page, params.loadSize)
            val photos = response.results

            val downloadablePhotos = coroutineScope {
                photos.map { photo ->
                    val bitmap = async {
                        val rawContent = service.downloadPhoto(photo.urls.small)
                        BitmapFactory.decodeByteArray(rawContent, 0, rawContent.size)
                    }

                    DownloadablePhoto(photo, bitmap)
                }

            }

            LoadResult.Page(
                data = downloadablePhotos,
                prevKey = if (page == UNSPLASH_STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (page == response.totalPages) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, DownloadablePhoto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            // This loads starting from previous page, but since PagingConfig.initialLoadSize spans
            // multiple pages, the initial load will still load items centered around
            // anchorPosition. This also prevents needing to immediately launch prepend due to
            // prefetchDistance.
            state.closestPageToPosition(anchorPosition)?.prevKey
        }
    }
}
