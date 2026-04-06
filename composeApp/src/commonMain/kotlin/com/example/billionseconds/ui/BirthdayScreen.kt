package com.example.billionseconds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import billionseconds.composeapp.generated.resources.*
import com.example.billionseconds.mvi.BirthdayIntent
import com.example.billionseconds.mvi.BirthdayState
import org.jetbrains.compose.resources.stringResource

@Composable
fun BirthdayScreen(
    state: BirthdayState,
    onIntent: (BirthdayIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var dayText by remember { mutableStateOf(state.day?.toString() ?: "") }
    var monthText by remember { mutableStateOf(state.month?.toString() ?: "") }
    var yearText by remember { mutableStateOf(state.year?.toString() ?: "") }
    var hourText by remember { mutableStateOf(if (state.hour == 0) "" else state.hour.toString()) }
    var minuteText by remember { mutableStateOf(if (state.minute == 0) "" else state.minute.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.enter_birthday),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        // Дата рождения
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = dayText,
                onValueChange = { v ->
                    if (v.length <= 2) {
                        dayText = v
                        updateDate(v, monthText, yearText, onIntent)
                    }
                },
                label = { Text(stringResource(Res.string.day)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = monthText,
                onValueChange = { v ->
                    if (v.length <= 2) {
                        monthText = v
                        updateDate(dayText, v, yearText, onIntent)
                    }
                },
                label = { Text(stringResource(Res.string.month)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = yearText,
                onValueChange = { v ->
                    if (v.length <= 4) {
                        yearText = v
                        updateDate(dayText, monthText, v, onIntent)
                    }
                },
                label = { Text(stringResource(Res.string.year)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(2f),
                singleLine = true
            )
        }

        // Время (необязательно)
        Text(
            text = stringResource(Res.string.optional_time),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { v ->
                    if (v.length <= 2) {
                        hourText = v
                        updateTime(v, minuteText, onIntent)
                    }
                },
                label = { Text(stringResource(Res.string.hour)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { v ->
                    if (v.length <= 2) {
                        minuteText = v
                        updateTime(hourText, v, onIntent)
                    }
                },
                label = { Text(stringResource(Res.string.minute)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onIntent(BirthdayIntent.CalculateClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.calculate))
        }
    }
}

private fun updateDate(
    dayText: String,
    monthText: String,
    yearText: String,
    onIntent: (BirthdayIntent) -> Unit
) {
    val day = dayText.toIntOrNull() ?: return
    val month = monthText.toIntOrNull() ?: return
    val year = yearText.toIntOrNull() ?: return
    if (year < 1000) return
    onIntent(BirthdayIntent.DateChanged(year, month, day))
}

private fun updateTime(
    hourText: String,
    minuteText: String,
    onIntent: (BirthdayIntent) -> Unit
) {
    val hour = hourText.toIntOrNull() ?: 0
    val minute = minuteText.toIntOrNull() ?: 0
    onIntent(BirthdayIntent.TimeChanged(hour, minute))
}
