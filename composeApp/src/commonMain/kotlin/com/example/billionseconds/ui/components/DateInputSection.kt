package com.example.billionseconds.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun DateInputSection(
    year: Int?,
    month: Int?,
    day: Int?,
    onDateChanged: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun TimeInputSection(
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
)
