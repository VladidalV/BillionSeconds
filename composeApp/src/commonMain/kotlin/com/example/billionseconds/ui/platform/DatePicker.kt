package com.example.billionseconds.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate

expect class DatePickerState

@Composable
expect fun rememberDatePickerState(): DatePickerState

@Composable
expect fun BirthdayDatePicker(
    state: DatePickerState,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
)