/*
 * Copyright 2021 Yashovardhan Dhanania
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

import android.app.Application
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CancellationException

class MainViewModel(application: Application) : AndroidViewModel(application) {
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

    fun restart() {
        _timerState.value = TimerState.PAUSED
        viewModelScope.launch {
            job?.cancelAndJoin()
            _timeRemaining.value = _totalTime.value
            startTimer()
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
        if (_timerState.value == TimerState.STARTED) {
            job?.cancel()
            _timerState.value = TimerState.PAUSED
        } else {
            job?.cancel()
            job = runTimer(_timeRemaining.value)
        }
    }

    private fun runTimer(totalRemaining: Duration): Job {
        val initialTime = LocalDateTime.now()
        return viewModelScope.launch {
            _timerState.value = TimerState.STARTED
            while (_timeRemaining.value > Duration.ZERO && isActive) {
                Log.d("MainViewModel", "RunTimer: Timer Started at: $initialTime")
                val elapsed = Duration.between(initialTime, LocalDateTime.now())
                _timeRemaining.value = (totalRemaining - elapsed).coerceAtLeast(Duration.ZERO)
                _delay.value = when {
                    _timeRemaining.value.toMinutes() > 0 -> 500
                    else -> 200
                }
                delay(_delay.value.toLong())
            }
            if (isActive) {
                Log.d("MainViewModel", "Finished")
                _timerState.value = TimerState.FINISHED
                showFinishNotification()
            }
        }
    }

    private fun showFinishNotification() {
        val channel =
            NotificationChannelCompat.Builder("timer", NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Timers")
                .setVibrationEnabled(true)
                .setShowBadge(true)
                .build()
        NotificationManagerCompat.from(getApplication()).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(getApplication(), channel.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer completed!")
            .setContentText(totalTime.value.format())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(getApplication())
            .notify(100, notification)
    }

    private fun Duration.format(): String {
        val hours = toHours()
        val minutes = minusHours(hours).toMinutes()
        val seconds = minusHours(hours).minusMinutes(minutes).seconds
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }

    enum class TimerState {
        NOT_SET,
        STARTED,
        PAUSED,
        FINISHED
    }
}
