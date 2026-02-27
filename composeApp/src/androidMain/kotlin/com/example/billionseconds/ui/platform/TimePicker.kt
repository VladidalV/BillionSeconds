package com.example.billionseconds.ui.platform

import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedLocalTime = LocalTime(hourOfDay, minute)
                state.selectedTime.value = selectedLocalTime
                onTimeSelected(selectedLocalTime)
                showDialog = false
            },
            12,
            0,
            true
        )
        timePickerDialog.show()
    }

    Column(modifier = modifier) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(state.selectedTime.value?.toString() ?: "Выбрать время")
        }
    }
}
