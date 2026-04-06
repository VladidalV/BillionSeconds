package com.example.billionseconds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import billionseconds.composeapp.generated.resources.*
import com.example.billionseconds.mvi.BirthdayIntent
import com.example.billionseconds.mvi.BirthdayState
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection
import org.jetbrains.compose.resources.stringResource

@Composable
fun BirthdayScreen(
    state: BirthdayState,
    onIntent: (BirthdayIntent) -> Unit,
    modifier: Modifier = Modifier
) {
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

        DateInputSection(
            year = state.year,
            month = state.month,
            day = state.day,
            onDateChanged = { y, m, d -> onIntent(BirthdayIntent.DateChanged(y, m, d)) }
        )

        Text(
            text = stringResource(Res.string.optional_time),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start)
        )

        TimeInputSection(
            hour = state.hour,
            minute = state.minute,
            onTimeChanged = { h, m -> onIntent(BirthdayIntent.TimeChanged(h, m)) }
        )

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
