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
package com.example.androiddevchallenge.ui.layouts

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.MainViewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.time.Duration

@Composable
fun TimerFace(viewModel: MainViewModel) {
    val totalDuration by viewModel.totalTime.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    Log.d("TimerFace", "Total duration = $totalDuration")
    Log.d("TimerFace", "Time remaining = $timeRemaining")
    val hours by derivedStateOf { timeRemaining.toHours() }
    val minutes by derivedStateOf { timeRemaining.minusHours(hours).toMinutes() }
    val seconds by
    derivedStateOf {
        timeRemaining.minusHours(hours).minusMinutes(minutes).seconds
    }
    Log.d("TimerFace", "H = $hours M = $minutes S = $seconds")
    val progress by animateFloatAsState(
        targetValue = 1 - timeRemaining.toNanos().toFloat() / totalDuration.toNanos().toFloat()
            .coerceAtLeast(1f)
    )
    Box(
        modifier = Modifier.wrapContentSize(Alignment.Center),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            Modifier.drawBehind {
                drawCircle(
                    Color.LightGray,
                    radius = size.minDimension / 2.08f,
                    style = Stroke(width = 4.dp.toPx())
                )
            }.sizeIn(minWidth = 200.dp, minHeight = 200.dp),
            strokeWidth = 8.dp
        )
        DurationDisplay(hours = hours, minutes = minutes, seconds = seconds, onClick = { /*TODO*/ })
    }
}

@Composable
fun DurationDisplay(hours: Long, minutes: Long, seconds: Long, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
    ) {
        TimerText(label = "", time = hours.toInt(), padNeeded = false, size = 48)
        Text(text = ":", fontSize = 20.sp)
        TimerText(label = "", time = minutes.toInt(), padNeeded = true, size = 48)
        Text(text = ":", fontSize = 20.sp)
        TimerText(label = "", time = seconds.toInt(), padNeeded = true, size = 48)
    }
}

@Composable
fun TimerText(label: String, time: Int, padNeeded: Boolean, size: Int) {
    val displayTime by animateIntAsState(targetValue = time)
    val displaySize by animateIntAsState(targetValue = size)
    Text(
        text = if (padNeeded) displayTime.toString().padStart(2, '0') else displayTime.toString(),
        modifier = Modifier,
        fontFamily = FontFamily.SansSerif,
        fontSize = displaySize.sp
    )
}

@Preview(name = "Light theme", widthDp = 360, heightDp = 640)
@Composable
fun TimerDisplayPreviewLight() {
    val viewModel = MainViewModel()
    viewModel.setTimer(Duration.ofMinutes(1))
    viewModel.startTimer()
    MyTheme {
        Surface(color = MaterialTheme.colors.background) {
            TimerFace(viewModel)
        }
    }
}

@Preview(name = "Dark theme", widthDp = 360, heightDp = 640)
@Composable
fun TimerDisplayPreviewDark() {
    val viewModel = MainViewModel()
    viewModel.setTimer(Duration.ofMinutes(1))
    viewModel.startTimer()
    MyTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.background) {
            TimerFace(viewModel)
        }
    }
}
