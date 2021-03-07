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
package com.example.androiddevchallenge.ui.layouts

import androidx.annotation.IntRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme
import java.time.Duration

enum class DurationOptions {
    HOUR,
    MINUTE,
    SECOND
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TimerSetting(
    modifier: Modifier = Modifier,
    initialDuration: Duration = Duration.ZERO,
    onDurationSet: (Duration) -> Unit
) {
    var duration by remember { mutableStateOf(initialDuration) }
    var selected by remember { mutableStateOf(DurationOptions.SECOND) }
    val hours by remember { derivedStateOf { duration.toHours() } }
    val minutes by remember { derivedStateOf { duration.minusHours(hours).toMinutes() } }
    val seconds by remember {
        derivedStateOf {
            duration.minusHours(hours).minusMinutes(minutes).seconds
        }
    }
    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DurationDisplay(hours, selected, minutes, seconds, onSelect = { selected = it })
        InputPad { number ->
            duration = handleInput(selected, duration, number, hours, minutes, seconds)
        }
        AnimatedVisibility(visible = duration > Duration.ZERO) {
            TimerOptions { onDurationSet(duration) }
        }
    }
}

@Composable
fun TimerOptions(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.PlayCircleFilled, contentDescription = "Start Timer",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colors.primary
        )
    }
}

private fun handleInput(
    selected: DurationOptions,
    duration: Duration,
    number: Int,
    hours: Long,
    minutes: Long,
    seconds: Long
): Duration {
    return when (selected) {
        DurationOptions.HOUR -> {
            if (number == -1) {
                duration.minusHours(hours).plusHours(hours / 10)
            } else {
                duration.minusHours(hours)
                    .plusHours((hours * 10 + number).coerceAtMost(99))
            }
        }
        DurationOptions.MINUTE -> {
            if (number == -1) {
                duration.minusMinutes(minutes).plusMinutes(minutes / 10)
            } else {
                duration.minusMinutes(minutes)
                    .plusMinutes((minutes * 10 + number).coerceAtMost(59))
            }
        }
        DurationOptions.SECOND -> {
            if (number == -1) {
                duration.minusSeconds(seconds).plusSeconds(seconds / 10)
            } else {
                duration.minusSeconds(seconds)
                    .plusSeconds((seconds * 10 + number).coerceAtMost(59))
            }
        }
    }
}

@Composable
fun DurationDisplay(
    hours: Long,
    selected: DurationOptions,
    minutes: Long,
    seconds: Long,
    onSelect: (DurationOptions) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TimerSettingText(
            label = "Hour",
            time = hours.toString().padStart(2, '0'),
            selected == DurationOptions.HOUR
        ) { onSelect(DurationOptions.HOUR) }
        Text(text = ":", fontSize = 20.sp)
        TimerSettingText(
            label = "Minute",
            time = minutes.toString().padStart(2, '0'),
            selected == DurationOptions.MINUTE
        ) { onSelect(DurationOptions.MINUTE) }
        Text(text = ":", fontSize = 20.sp)
        TimerSettingText(
            label = "Second",
            time = seconds.toString().padStart(2, '0'),
            selected == DurationOptions.SECOND
        ) { onSelect(DurationOptions.SECOND) }
    }
}

@Composable
fun InputKey(@IntRange(from = 0, to = 9) number: Int, onClick: (Int) -> Unit) {
    Text(
        text = number.toString(),
        Modifier
            .fillMaxSize()
            .clickable { onClick(number) },
        textAlign = TextAlign.Center,
        fontSize = 32.sp
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InputPad(onClick: (Int) -> Unit) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxWidth(0.8f),
        contentPadding = PaddingValues(4.dp),
        cells = GridCells.Fixed(3)
    ) {
        items(9) { index -> InputKey(number = index + 1, onClick = { onClick(index + 1) }) }
        item { }
        item { InputKey(number = 0, onClick = { onClick(0) }) }
        item {
            IconButton(
                onClick = { onClick(-1) },
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = Icons.Default.Backspace.name,
                )
            }
        }
    }
}

@Composable
fun TimerSettingText(label: String, time: String, isSelected: Boolean, onSelect: () -> Unit) {
    Text(
        text = time,
        Modifier
            .semantics {
                contentDescription = label
            }
            .clickable { onSelect() },
        fontFamily = FontFamily.SansSerif,
        fontSize = 48.sp,
        color = if (isSelected) MaterialTheme.colors.primary
        else MaterialTheme.colors.onBackground
    )
}

@Preview(name = "Light theme", widthDp = 360, heightDp = 640)
@Composable
fun TimerSettingPreview() {
    MyTheme {
        Surface(color = MaterialTheme.colors.background) {
            TimerSetting {}
        }
    }
}

@Preview(name = "Dark theme", widthDp = 360, heightDp = 640)
@Composable
fun TimerSettingPreviewDark() {
    MyTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colors.background) {
            TimerSetting {}
        }
    }
}
