package com.example.billionseconds.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalTime

expect class TimePickerState

@Composable
expect fun rememberTimePickerState(): TimePickerState

@Composable
expect fun BirthdayTimePicker(
    state: TimePickerState,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
)