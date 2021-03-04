/*
 * Copyright 2021 The Android Open Source Project
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
package com.example.androiddevchallenge

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class MainViewModel : ViewModel() {
    private var job: Job? = null
    private val _timeRemaining = MutableStateFlow(Duration.ZERO)
    val timeRemaining = _timeRemaining.asStateFlow()

    private val _totalTime = MutableStateFlow(Duration.ZERO)
    val totalTime = _totalTime.asStateFlow()

    fun setTimer(duration: Duration) {
        _totalTime.value = duration
        _timeRemaining.value = duration
    }

    fun startTimer() {
        val initialTime = LocalDateTime.now()
        job?.cancel()
        job = viewModelScope.launch {
            while (_timeRemaining.value > Duration.ZERO) {
                ensureActive()
                val elapsed = Duration.between(initialTime, LocalDateTime.now())
                _timeRemaining.value = (_totalTime.value - elapsed).coerceAtLeast(Duration.ZERO)
                Log.d("ViewModel", "Time remaining = ${_timeRemaining.value} Elapsed = $elapsed")
                delay(200)
            }
        }
    }
}
