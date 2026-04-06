package com.example.billionseconds.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
actual fun DateInputSection(
    year: Int?,
    month: Int?,
    day: Int?,
    onDateChanged: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier
) {
    var dayText by remember { mutableStateOf(day?.toString() ?: "") }
    var monthText by remember { mutableStateOf(month?.toString() ?: "") }
    var yearText by remember { mutableStateOf(year?.toString() ?: "") }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = dayText,
            onValueChange = { v ->
                if (v.length <= 2) {
                    dayText = v
                    val d = v.toIntOrNull() ?: return@OutlinedTextField
                    val m = monthText.toIntOrNull() ?: return@OutlinedTextField
                    val y = yearText.toIntOrNull()?.takeIf { it >= 1000 } ?: return@OutlinedTextField
                    onDateChanged(y, m, d)
                }
            },
            label = { Text("День") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = monthText,
            onValueChange = { v ->
                if (v.length <= 2) {
                    monthText = v
                    val d = dayText.toIntOrNull() ?: return@OutlinedTextField
                    val m = v.toIntOrNull() ?: return@OutlinedTextField
                    val y = yearText.toIntOrNull()?.takeIf { it >= 1000 } ?: return@OutlinedTextField
                    onDateChanged(y, m, d)
                }
            },
            label = { Text("Месяц") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = yearText,
            onValueChange = { v ->
                if (v.length <= 4) {
                    yearText = v
                    val d = dayText.toIntOrNull() ?: return@OutlinedTextField
                    val m = monthText.toIntOrNull() ?: return@OutlinedTextField
                    val y = v.toIntOrNull()?.takeIf { it >= 1000 } ?: return@OutlinedTextField
                    onDateChanged(y, m, d)
                }
            },
            label = { Text("Год") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(2f),
            singleLine = true
        )
    }
}

@Composable
actual fun TimeInputSection(
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier
) {
    var hourText by remember { mutableStateOf(if (hour == 0) "" else hour.toString()) }
    var minuteText by remember { mutableStateOf(if (minute == 0) "" else minute.toString()) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = hourText,
            onValueChange = { v ->
                if (v.length <= 2) {
                    hourText = v
                    onTimeChanged(v.toIntOrNull() ?: 0, minuteText.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Час") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = minuteText,
            onValueChange = { v ->
                if (v.length <= 2) {
                    minuteText = v
                    onTimeChanged(hourText.toIntOrNull() ?: 0, v.toIntOrNull() ?: 0)
                }
            },
            label = { Text("Минута") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }
}
