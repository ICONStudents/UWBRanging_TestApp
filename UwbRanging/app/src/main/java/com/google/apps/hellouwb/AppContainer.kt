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

package com.google.apps.hellouwb

import android.content.ContentResolver
import com.google.apps.hellouwb.data.SettingsStore
import com.google.apps.hellouwb.data.UwbRangingControlSource

interface AppContainer {
    val rangingResultSource: UwbRangingControlSource   // interface이기 때문에 초기화는 구현단계에서
    val settingsStore: SettingsStore
    val contentResolver: ContentResolver
}
