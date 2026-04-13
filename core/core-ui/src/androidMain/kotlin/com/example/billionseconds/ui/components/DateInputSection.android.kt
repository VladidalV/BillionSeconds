package com.example.billionseconds.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateInputSection(
    year: Int?,
    month: Int?,
    day: Int?,
    onDateChanged: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val label = if (year != null && month != null && day != null) {
        "%02d.%02d.%d".format(day, month, year)
    } else {
        "Выбрать дату рождения"
    }

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("📅  $label")
    }

    if (showPicker) {
        val initialMillis = if (year != null && month != null && day != null) {
            dateToMillis(year, month, day)
        } else {
            // Default: 30 лет назад
            dateToMillis(
                Calendar.getInstance().get(Calendar.YEAR) - 30,
                Calendar.getInstance().get(Calendar.MONTH) + 1,
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            )
        }

        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val (y, m, d) = millisToDate(millis)
                        onDateChanged(y, m, d)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TimeInputSection(
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val label = "%02d:%02d".format(hour, minute)

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("🕐  $label")
    }

    if (showPicker) {
        val state = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChanged(state.hour, state.minute)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            },
            text = {
                TimePicker(
                    state = state,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        )
    }
}

private fun dateToMillis(year: Int, month: Int, day: Int): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.set(year, month - 1, day, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

private fun millisToDate(millis: Long): Triple<Int, Int, Int> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return Triple(
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH)
    )
}
