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
import kotlinx.datetime.LocalDate

actual class DatePickerState(
    val selectedDate: MutableState<LocalDate?>
)

@Composable
actual fun rememberDatePickerState(): DatePickerState {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    return remember { DatePickerState(mutableStateOf(selectedDate)) }
}

@Composable
actual fun BirthdayDatePicker(
    state: DatePickerState,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier
) {
    var yearInput by remember { mutableStateOf("2000") }
    var monthInput by remember { mutableStateOf("1") }
    var dayInput by remember { mutableStateOf("1") }

    Column(modifier = modifier) {
        Row {
            TextField(
                value = yearInput,
                onValueChange = { yearInput = it },
                label = { Text("Year") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
            TextField(
                value = monthInput,
                onValueChange = { monthInput = it },
                label = { Text("Month") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
            TextField(
                value = dayInput,
                onValueChange = { dayInput = it },
                label = { Text("Day") },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
        Button(
            onClick = {
                val year = yearInput.toIntOrNull()
                val month = monthInput.toIntOrNull()
                val day = dayInput.toIntOrNull()
                
                if (year != null && month != null && day != null &&
                    month in 1..12 && day in 1..31) {
                    try {
                        val localDate = LocalDate(year, month, day)
                        state.selectedDate.value = localDate
                        onDateSelected(localDate)
                    } catch (e: Exception) {
                        // Invalid date, ignore
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(state.selectedDate.value?.toString() ?: "Select Date")
        }
    }
}