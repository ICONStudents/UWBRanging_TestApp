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

package com.google.apps.hellouwb.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.apps.hellouwb.HelloUwbApplication


private const val PERMISSION_REQUEST_CODE = 1234  //PERMISSION_REQUEST_CODE는 권한 요청과 그 결과를 연결하는 역할, 앱 내에서 고유해야함

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestPermissions()    // 밑에서 정의하는 요청 받아오는 코드
    (application as HelloUwbApplication).initContainer {  // application instance를 HelloUwbApplication으로 캐스팅하고, initContainer 메서드를 호출
      runOnUiThread { setContent { HelloUwbApp((application as HelloUwbApplication).container) } }  // 이부분에서 HelloUwbApp을 request
    }

    /**
     * Check if device supports Ultra-wideband
     */
    val packageManager: PackageManager = applicationContext.packageManager
    val deviceSupportsUwb = packageManager.hasSystemFeature("android.hardware.uwb")  // Check if device supports UWB, boolean value

    if (!deviceSupportsUwb ) {
      Log.e("UWB Sample", "Device does not support Ultra-wideband")
      Toast.makeText(applicationContext, "Device does not support UWB", Toast.LENGTH_SHORT).show()
      //TODO: Uncomment this if you want to see it running on a non-supported device
      finishAndRemoveTask();
    }
    else {
      Toast.makeText(applicationContext, "Device supports UWB", Toast.LENGTH_SHORT).show()
    }
  }

  private fun requestPermissions() {
    if (!arePermissionsGranted()) {  // Request permissions if not granted
      requestPermissions(PERMISSIONS_REQUIRED, PERMISSION_REQUEST_CODE)  //첫 번째 매개변수로 요청할 권한의 배열을, 두 번째 매개변수로 권한 요청에 대한 고유한 식별자를 받음
    }
  }

  private fun arePermissionsGranted(): Boolean {
    for (permission in PERMISSIONS_REQUIRED) {
      if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
        return false
      }
    }
    return true
  }

  override fun onRequestPermissionsResult(   // 권한을 받아오는 실질적인 부분
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray,
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    for (result in grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        requestPermissions()
      }
    }
  }

  companion object {   // 버젼에 따른 추가 권한 사항 처리

    private val PERMISSIONS_REQUIRED_BEFORE_T =  // Android T 버전 이전에 필요한 권한을 정의
      listOf(
        // Permissions needed by Nearby Connection
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,

        // permission required by UWB API
        Manifest.permission.UWB_RANGING
      )

    private val PERMISSIONS_REQUIRED_T =   // Android T 버전에서 추가로 필요한 권한을 정의
      arrayOf(
        Manifest.permission.NEARBY_WIFI_DEVICES,
      )

    private val PERMISSIONS_REQUIRED =
      PERMISSIONS_REQUIRED_BEFORE_T.toMutableList()  // 가변 리스트로 변환
        .apply {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {  // Andriod가 T version이상이면 추가
            addAll(PERMISSIONS_REQUIRED_T)
          }
        }
        .toTypedArray()
  }
}
