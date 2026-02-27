package com.example.billionseconds.ui.platform

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import kotlinx.datetime.LocalDate
import java.util.Calendar

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
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedLocalDate = LocalDate(year, month + 1, dayOfMonth)
                state.selectedDate.value = selectedLocalDate
                onDateSelected(selectedLocalDate)
                showDialog = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    Column(modifier = modifier) {
        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(state.selectedDate.value?.toString() ?: "Выбрать дату")
        }
    }
}
