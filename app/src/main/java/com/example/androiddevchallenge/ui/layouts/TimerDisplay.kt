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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.MainViewModel
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.time.Duration

@Composable
fun TimerDisplay(viewModel: MainViewModel, timerState: MainViewModel.TimerState) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isPaused by derivedStateOf {
            timerState == MainViewModel.TimerState.PAUSED
        }
        val totalDuration by viewModel.totalTime.collectAsState()
        val timeRemaining by viewModel.timeRemaining.collectAsState()
        val animTime by viewModel.updateDelay.collectAsState()
        TimerFace(
            totalDuration = totalDuration,
            timeRemaining = timeRemaining,
            timerState = timerState,
            animTime = animTime,
            togglePause = viewModel::togglePause
        )
        Row(horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)) {
            IconButton(
                onClick = { viewModel.restart() },
                Modifier.wrapContentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
            IconButton(
                onClick = { viewModel.togglePause() },
                Modifier.wrapContentSize()
            ) {
                Crossfade(targetState = isPaused) { isPaused ->
                    if (isPaused) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PauseCircleFilled,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }
            IconButton(
                onClick = { viewModel.stopTimer() },
                Modifier.wrapContentSize()
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun TimerFace(
    totalDuration: Duration,
    timeRemaining: Duration,
    timerState: MainViewModel.TimerState,
    animTime: Int,
    togglePause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Log.d("TimerFace", "Total duration = $totalDuration")
    Log.d("TimerFace", "Time remaining = $timeRemaining")
    val hours by derivedStateOf { timeRemaining.toHours() }
    val minutes by derivedStateOf { timeRemaining.minusHours(hours).toMinutes() }
    val seconds by
    derivedStateOf {
        timeRemaining.minusHours(hours).minusMinutes(minutes).seconds
    }
    val millis by derivedStateOf {
        timeRemaining.minusHours(hours)
            .minusMinutes(minutes).minusSeconds(seconds).toMillis()
    }
    val rawProgress by derivedStateOf {
        1 - timeRemaining.toNanos().toFloat() / totalDuration.toNanos().toFloat()
            .coerceAtLeast(1f)
    }
    Log.d("TimerFace", "H = $hours M = $minutes S = $seconds")
    val progress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = tween(
            durationMillis = animTime,
            easing = LinearEasing
        )
    )
    val interactionSource = remember { MutableInteractionSource() }
    val pausedColor = MaterialTheme.colors.error
    val primaryColor = MaterialTheme.colors.primary
    val secondaryColor = MaterialTheme.colors.secondary
    val rawColor by derivedStateOf {
        when (timerState) {
            MainViewModel.TimerState.PAUSED -> pausedColor
            MainViewModel.TimerState.STARTED -> primaryColor
            MainViewModel.TimerState.FINISHED -> secondaryColor
            else -> Color.Red
        }
    }
    val progressColor by animateColorAsState(
        targetValue = rawColor, animationSpec = tween(1000)
    )
    Log.d("TimerDisplay", "State = $timerState Color = $progressColor")
    val rotation by animateFloatAsState(
        targetValue = 6 * totalDuration.seconds * progress,
        animationSpec = when (timerState) {
            MainViewModel.TimerState.NOT_SET -> tween()
            MainViewModel.TimerState.STARTED -> tween(animTime, easing = LinearEasing)
            MainViewModel.TimerState.PAUSED -> tween(animTime, easing = LinearEasing)
            MainViewModel.TimerState.FINISHED -> spring()
        }
    )
    Log.d(
        "TimerDisplay",
        "State = $timerState progress = $progress Raw progress = $rawProgress rotation = $rotation animTime = $animTime total duration = ${totalDuration.seconds}"
    )
    Box(
        modifier = modifier
            .sizeIn(minWidth = 200.dp, minHeight = 200.dp)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                enabled = true
            ) {
                togglePause()
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = progress,
            Modifier
                .drawBehind {
                    val topLeft = Offset(-size.minDimension / 10f, -size.minDimension / 10f)
                    val arcSize = Size(size.width - 2 * topLeft.x, size.height - 2 * topLeft.y)
                    drawArc(
                        Color.Blue,
                        startAngle = rotation * 10,
                        sweepAngle = 30f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawArc(
                        Color.Blue,
                        startAngle = 180 + rotation * 10,
                        sweepAngle = 30f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                .drawBehind {
                    drawCircle(
                        Color.LightGray,
                        radius = size.minDimension / 2.08f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
                .rotate(rotation)
                .sizeIn(minWidth = 200.dp, minHeight = 200.dp),
            strokeWidth = 8.dp,
            color = progressColor
        )
        DurationDisplay(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            millis = millis,
            animTime = animTime,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DurationDisplay(hours: Long, minutes: Long, seconds: Long, millis: Long, animTime: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
    ) {
        val hourSize = 48
        val minuteSize = 56
        val secondsSize = 64
        AnimatedVisibility(visible = hours > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TimerText(
                    label = "",
                    time = hours.toInt(),
                    padNeeded = 0,
                    size = hourSize,
                    animTime
                )
                Text(text = ":", fontSize = 20.sp)
            }
        }
        AnimatedVisibility(visible = hours > 0 || minutes > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TimerText(
                    label = "Minutes", time = minutes.toInt(),
                    padNeeded = if (hours > 0) 2 else 1,
                    size = if (hours > 0) hourSize else minuteSize,
                    animTime
                )
                Text(text = ":", fontSize = 20.sp)
            }
        }
        TimerText(
            label = "Seconds",
            time = seconds.toInt(),
            padNeeded = if (hours > 0 || minutes > 0) 2 else 1,
            size = when {
                hours > 0 -> hourSize
                minutes > 0 -> minuteSize
                else -> secondsSize
            },
            animTime
        )
        AnimatedVisibility(
            visible = hours == 0L && minutes == 0L,
            modifier = Modifier.padding(top = 16.dp, start = 16.dp)
        ) {
            TimerText(
                label = "Milliseconds",
                time = millis.toInt(),
                padNeeded = 3,
                size = 24,
                animTime = animTime
            )
        }
    }
}

@Composable
fun TimerText(label: String, time: Int, padNeeded: Int, size: Int, animTime: Int) {
    val displayTime by animateIntAsState(targetValue = time, animationSpec = tween(animTime))
    val displaySize by animateIntAsState(targetValue = size, animationSpec = tween(animTime))
    Text(
        text = displayTime.toString().padStart(padNeeded, '0'),
        modifier = Modifier.semantics {
            contentDescription = label
        },
        fontFamily = FontFamily.SansSerif,
        fontSize = displaySize.sp
    )
}

@Preview(name = "Light theme", widthDp = 360, heightDp = 640)
@Composable
fun TimerDisplayPreviewLight() {
    val viewModel = MainViewModel()
    viewModel.setTimer(Duration.ofMinutes(1))
    MyTheme {
        Surface(color = MaterialTheme.colors.background) {
            TimerFace(
                totalDuration = viewModel.totalTime.value,
                timeRemaining = viewModel.timeRemaining.value,
                timerState = viewModel.timerState.value,
                animTime = viewModel.updateDelay.value,
                togglePause = { viewModel.togglePause() }
            )
        }
    }
}

@Preview(name = "Dark theme", widthDp = 360, heightDp = 640)
@Composable
fun TimerDisplayPreviewDark() {
    val viewModel = MainViewModel()
    viewModel.setTimer(Duration.ofSeconds(50))
    MyTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.background) {
            TimerFace(
                totalDuration = viewModel.totalTime.value,
                timeRemaining = viewModel.timeRemaining.value,
                timerState = viewModel.timerState.value,
                animTime = viewModel.updateDelay.value,
                togglePause = { viewModel.togglePause() }
            )
        }
    }
}
