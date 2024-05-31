/*
 *
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.apps.hellouwb.data

import android.content.ContentResolver
import android.content.Context
import com.google.apps.hellouwb.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AppContainerImpl(
  private val context: Context,
  afterLoading: () -> Unit,
) : AppContainer {

  private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    override val rangingResultSource: UwbRangingControlSource
        get() =
            _rangingResultSource
                ?: throw IllegalStateException("rangingResultSource only can be accessed after loading.")

    private var _rangingResultSource: UwbRangingControlSource? = null

    override val settingsStore = SettingsStoreImpl(context, coroutineScope)

    override val contentResolver: ContentResolver = context.contentResolver

    init {
        coroutineScope.launch {
            settingsStore.appSettings.collect {
                val endpointId = it.deviceDisplayName + "|" + it.deviceUuid  // endpointId를 구성하는 것, 기기 고유의uuid를 사용
                if (_rangingResultSource == null) {   // UwbRangingControlSourceImpl 인스턴스가 아직 생성되지 않은 경우
                    _rangingResultSource =
                        UwbRangingControlSourceImpl(context, endpointId, coroutineScope)  // 인스턴스를 생성하고
                    afterLoading()   // 로딩이 완료됨을 알림
                } else {
                    rangingResultSource.deviceType = it.deviceType  // 이미 생성된 경우 endpointId를 업데이트함
          rangingResultSource.updateEndpointId(endpointId)
        }
      }
    }
  }
}
