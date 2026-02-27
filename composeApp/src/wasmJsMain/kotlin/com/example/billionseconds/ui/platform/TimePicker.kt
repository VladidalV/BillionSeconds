package com.example.billionseconds.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

actual class TimePickerState(
    val selectedTime: MutableState<LocalTime?>
)

@Composable
actual fun rememberTimePickerState(): TimePickerState {
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    return remember { TimePickerState(mutableStateOf(selectedTime)) }
}

@Composable
actual fun BirthdayTimePicker(
    state: TimePickerState,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier
) {
    var hourInput by remember { mutableStateOf("12") }
    var minuteInput by remember { mutableStateOf("0") }
    var secondInput by remember { mutableStateOf("0") }

    Column(modifier = modifier) {
        Row {
            TextField(
                value = hourInput,
                onValueChange = { hourInput = it },
                label = { Text("Час") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
            TextField(
                value = minuteInput,
                onValueChange = { minuteInput = it },
                label = { Text("Минута") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
            TextField(
                value = secondInput,
                onValueChange = { secondInput = it },
                label = { Text("Секунда") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
        Button(
            onClick = {
                val hour = hourInput.toIntOrNull()
                val minute = minuteInput.toIntOrNull()
                val second = secondInput.toIntOrNull()
                
                if (hour != null && hour in 0..23 &&
                    minute != null && minute in 0..59 &&
                    second != null && second in 0..59) {
                    val localTime = LocalTime(hour, minute, second)
                    state.selectedTime.value = localTime
                    onTimeSelected(localTime)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(state.selectedTime.value?.toString() ?: "Выбрать время")
        }
    }
}
