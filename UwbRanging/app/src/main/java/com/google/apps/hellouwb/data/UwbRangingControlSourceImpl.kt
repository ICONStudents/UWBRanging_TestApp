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

import android.content.Context
import androidx.core.uwb.RangingParameters
import com.google.apps.uwbranging.EndpointEvents
import com.google.apps.uwbranging.UwbConnectionManager
import com.google.apps.uwbranging.UwbEndpoint
import com.google.apps.uwbranging.UwbSessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.properties.Delegates

internal class UwbRangingControlSourceImpl(
    context: Context,
    endpointId: String,
    private val coroutineScope: CoroutineScope,
    private val uwbConnectionManager: UwbConnectionManager =
    UwbConnectionManager.getInstance(context),
) : UwbRangingControlSource {

  private var uwbEndpoint = UwbEndpoint(endpointId, SecureRandom.getSeed(8))  // uwbEndpoint를 endpointId와 random한 값으로 설정

  private var uwbSessionScope: UwbSessionScope =     // UwbSessionScope 인스턴스로, UWB 세션의 범위를 나타냄
    getSessionScope(DeviceType.CONTROLLER, ConfigType.CONFIG_UNICAST_DS_TWR)    // TWR을 이용함

  private var rangingJob: Job? = null   // ranging 작업을 나타냄, default=null

  private val resultFlow = MutableSharedFlow<EndpointEvents>(   // ranging 결과를 나타냄, 밑의 부분은 초기화 설정을 위한 변수
    replay = 0,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
    extraBufferCapacity = 1
  )

  private val runningStateFlow = MutableStateFlow(false)   // kotlin MutableStateFlow에 대해 알아야함

  override val isRunning = runningStateFlow.asStateFlow()

  private fun getSessionScope(deviceType: DeviceType, configType: ConfigType): UwbSessionScope {  //
    return when (deviceType) {
      DeviceType.CONTROLEE -> uwbConnectionManager.controleeUwbScope(uwbEndpoint)
      DeviceType.CONTROLLER ->
        uwbConnectionManager.controllerUwbScope(uwbEndpoint, when (configType) {
          ConfigType.CONFIG_UNICAST_DS_TWR -> RangingParameters.CONFIG_UNICAST_DS_TWR
          ConfigType.CONFIG_MULTICAST_DS_TWR -> RangingParameters.CONFIG_MULTICAST_DS_TWR
          else -> throw java.lang.IllegalStateException()
        })
      else -> throw IllegalStateException()
    }
  }

  override fun observeRangingResults(): Flow<EndpointEvents> {
    return resultFlow
  }

  override var deviceType: DeviceType by
  Delegates.observable(DeviceType.CONTROLLER) { _, oldValue, newValue ->
    if (oldValue != newValue) {
      stop()
      uwbSessionScope = getSessionScope(newValue, configType)
    }
  }

  override var configType: ConfigType by
  Delegates.observable(ConfigType.CONFIG_UNICAST_DS_TWR) { _, oldValue, newValue ->
    if (oldValue != newValue) {
      stop()
      uwbSessionScope = getSessionScope(deviceType, newValue)
    }
  }

  override fun updateEndpointId(id: String) {
    if (id != uwbEndpoint.id) {
      stop()
      uwbEndpoint = UwbEndpoint(id, SecureRandom.getSeed(8))
      uwbSessionScope = getSessionScope(deviceType, configType)
    }
  }

  override fun start() {
    if (rangingJob == null) {
      rangingJob =
        coroutineScope.launch {
          uwbSessionScope.prepareSession().collect {
            resultFlow.tryEmit(it)
          }
        }
      runningStateFlow.update { true }
    }
  }

  override fun stop() {
    val job = rangingJob ?: return
    job.cancel()
    rangingJob = null
    runningStateFlow.update { false }
  }

  override fun sendOobMessage(endpoint: UwbEndpoint, message: ByteArray) {
    uwbSessionScope.sendMessage(endpoint, message)
  }
}
