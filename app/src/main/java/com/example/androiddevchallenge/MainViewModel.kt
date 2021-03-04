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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CancellationException

class MainViewModel : ViewModel() {
    private var job: Job? = null

    private val _timerState = MutableStateFlow(TimerState.NOT_SET)
    val timerState = _timerState.asStateFlow()

    private val _timeRemaining = MutableStateFlow(Duration.ZERO)
    val timeRemaining = _timeRemaining.asStateFlow()

    private val _totalTime = MutableStateFlow(Duration.ZERO)
    val totalTime = _totalTime.asStateFlow()

    private val _delay = MutableStateFlow(0)
    val updateDelay = _delay.asStateFlow()

    fun setTimer(duration: Duration) {
        _totalTime.value = duration
        _timeRemaining.value = duration
        startTimer()
    }

    fun goToSettings() {
        job?.cancel(CancellationException())
        _timerState.value = TimerState.NOT_SET
    }

    private fun startTimer() {
        job?.cancel(CancellationException())
        job = runTimer(_totalTime.value)
    }

    fun restart() { // FIXME: 05/03/21  
        _timerState.value = TimerState.PAUSED
        viewModelScope.launch {
            job?.cancelAndJoin()
            _timeRemaining.value = _totalTime.value
            job = runTimer(_totalTime.value)
        }
    }

    fun stopTimer() {
        viewModelScope.launch {
            job?.cancelAndJoin()
            _timerState.value = TimerState.NOT_SET
        }
    }

    fun togglePause() {
        Log.d("MainViewModel", "Toggle pause called. Timer state = ${_timerState.value}")
        viewModelScope.launch {
            if (_timerState.value == TimerState.STARTED) {
                job?.cancelAndJoin()
                _timerState.value = TimerState.PAUSED
            } else {
                job?.cancelAndJoin()
                job = runTimer(_timeRemaining.value)
            }
        }
    }

    private fun runTimer(totalRemaining: Duration): Job {
        val initialTime = LocalDateTime.now()
        return viewModelScope.launch {
            viewModelScope.launch {
                _timerState.value = TimerState.STARTED
                while (_timeRemaining.value > Duration.ZERO && _timerState.value == TimerState.STARTED) {
                    ensureActive()
                    val elapsed = Duration.between(initialTime, LocalDateTime.now())
                    _timeRemaining.value = (totalRemaining - elapsed).coerceAtLeast(Duration.ZERO)
                    _delay.value = when {
                        _timeRemaining.value.toMinutes() > 0 -> 500
                        else -> 200
                    }
                    delay(_delay.value.toLong())
                }
                if (_timerState.value == TimerState.STARTED) _timerState.value = TimerState.FINISHED
            }
        }
    }

    enum class TimerState {
        NOT_SET,
        STARTED,
        PAUSED,
        FINISHED
    }
}
