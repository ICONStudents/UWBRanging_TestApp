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

package com.google.apps.hellouwb.ui.ranging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.apps.hellouwb.data.UwbRangingControlSource
import kotlinx.coroutines.flow.*

class RangingViewModel(private val uwbRangingControlSource: UwbRangingControlSource) : ViewModel() {  // class RangningViewModel은 uwbRangingControlSource 형식의 그것을 받음
                                                                                                      // ViewModel을 상속받아 UI 상태를 관리함
  private val _uiState: MutableStateFlow<Boolean> = MutableStateFlow(false)  // _uiState는 변수로 false로 초기화 되지만, UI의 상태가 변화할 때 업데이트 됨

  val uiState = _uiState.asStateFlow()  // 이 변수를 통해 UI에서 상태를 관찰 <- 관찰만 하고 변할 수 없게 하기 위해 나눈 듯

  init {
    uwbRangingControlSource.isRunning.onEach { _uiState.update { it } }.launchIn(viewModelScope)   // uwbRangingControlSource.isRunning의 상태를 관찰, 변경되면 _uiState update / viewModelScope에서 실행
  }

  fun startRanging() {
    uwbRangingControlSource.start()  // 받은 매개변수를 통해 Ranging 시작
  }

  fun stopRanging() {
    uwbRangingControlSource.stop()  // stop한다.
  }

  companion object {      // RangingViewModel 인스턴스를 생성하는 팩토리를 제공하는 역할
    fun provideFactory(
      uwbRangingControlSource: UwbRangingControlSource,
    ): ViewModelProvider.Factory =    // provideFactory함수는 ViewModelProvider.Factory를 반환함
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return RangingViewModel(uwbRangingControlSource) as T
        }
      }
  }
}
