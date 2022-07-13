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

package com.google.samples.apps.sunflower.adapters

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.samples.apps.sunflower.GalleryFragment
import com.google.samples.apps.sunflower.adapters.GalleryAdapter.GalleryViewHolder
import com.google.samples.apps.sunflower.data.DownloadablePhoto
import com.google.samples.apps.sunflower.databinding.ListItemPhotoBinding

/**
 * Adapter for the [RecyclerView] in [GalleryFragment].
 */

class GalleryAdapter : PagingDataAdapter<DownloadablePhoto, GalleryViewHolder>(GalleryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        return GalleryViewHolder(
            ListItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val photo = getItem(position)
        if (photo != null) {
            holder.bind(photo)
        }
    }

    class GalleryViewHolder(
        private val binding: ListItemPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private fun awaitPhotoToBeDownloaded(photo: DownloadablePhoto): LiveData<Bitmap> = liveData {
            emit(photo.futureContent.await())
        }

        fun bind(item: DownloadablePhoto) {
            Log.d(javaClass.simpleName, "bind gallery view")
            binding.apply {
                photo = item

                setClickListener { view ->
                    val uri = Uri.parse(item.unsplashPhoto.user.attributionUrl)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    view.context.startActivity(intent)
                }

                lifecycleOwner?.let {
                    awaitPhotoToBeDownloaded(item).observe(it) { bitmap ->
                        plantPhoto.setImageBitmap(bitmap)
                    }
                }
                executePendingBindings()
            }
        }
    }
}

private class GalleryDiffCallback : DiffUtil.ItemCallback<DownloadablePhoto>() {
    override fun areItemsTheSame(oldItem: DownloadablePhoto, newItem: DownloadablePhoto): Boolean {
        return oldItem.unsplashPhoto.id == newItem.unsplashPhoto.id
    }

    override fun areContentsTheSame(oldItem: DownloadablePhoto, newItem: DownloadablePhoto): Boolean {
        return oldItem.unsplashPhoto == newItem.unsplashPhoto
    }
}
