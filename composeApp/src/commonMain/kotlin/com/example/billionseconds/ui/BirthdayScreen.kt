package com.example.billionseconds.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.BirthdayIntent
import com.example.billionseconds.mvi.BirthdayStore
import com.example.billionseconds.ui.platform.BirthdayDatePicker
import com.example.billionseconds.ui.platform.BirthdayTimePicker
import com.example.billionseconds.ui.platform.rememberDatePickerState
import com.example.billionseconds.ui.platform.rememberTimePickerState

@Composable
fun BirthdayScreen(
    store: BirthdayStore,
    modifier: Modifier = Modifier
) {
    val state by store.state.collectAsState()
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Billion Seconds Calculator",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            DateSection(
                datePickerState = datePickerState,
                state = state,
                onDateSelected = { date ->
                    store.handle(BirthdayIntent.DateSelected(date))
                }
            )
            
            TimeSection(
                timePickerState = timePickerState,
                state = state,
                onTimeSelected = { time ->
                    store.handle(BirthdayIntent.TimeSelected(time))
                }
            )
            
            CalculateButton(
                isLoading = state.isLoading,
                isEnabled = state.isCalculateEnabled,
                onClick = {
                    store.handle(BirthdayIntent.CalculateClicked)
                }
            )
            
            ResultSection(state = state)
        }
    }
}

@Composable
private fun DateSection(
    datePickerState: com.example.billionseconds.ui.platform.DatePickerState,
    state: com.example.billionseconds.mvi.BirthdayState,
    onDateSelected: (kotlinx.datetime.LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Birth Date",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            BirthdayDatePicker(
                state = datePickerState,
                onDateSelected = onDateSelected
            )
            
            state.birthDate?.let { date ->
                Text(
                    text = "Selected: $date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TimeSection(
    timePickerState: com.example.billionseconds.ui.platform.TimePickerState,
    state: com.example.billionseconds.mvi.BirthdayState,
    onTimeSelected: (kotlinx.datetime.LocalTime) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Birth Time",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            BirthdayTimePicker(
                state = timePickerState,
                onTimeSelected = onTimeSelected
            )
            
            state.birthTime?.let { time ->
                Text(
                    text = "Selected: $time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CalculateButton(
    isLoading: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        if (isLoading) {
            Text("Calculating...")
        } else {
            Text("Calculate Billion Seconds")
        }
    }
}

@Composable
private fun ResultSection(state: com.example.billionseconds.mvi.BirthdayState) {
    state.errorMessage?.let { error ->
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
    
    if (state.hasResult) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🎉 Billion Seconds Moment:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.resultText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}