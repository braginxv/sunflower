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

package com.google.samples.apps.sunflower

import android.app.Application
import androidx.databinding.ktx.BuildConfig
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.techlook.net.client.ClientSystem

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
    }

    override fun onTerminate() {
        ClientSystem.client().shutdown()
        ClientSystem.client().awaitTerminating()

        super.onTerminate()
    }
}
