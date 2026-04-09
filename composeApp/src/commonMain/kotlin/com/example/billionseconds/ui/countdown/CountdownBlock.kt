package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.billionseconds.domain.model.EventStatus
import com.example.billionseconds.mvi.CountdownUiState

@Composable
fun CountdownBlock(state: CountdownUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.eventStatus == EventStatus.Reached) {
            Text(
                text = "Ты прожил миллиард секунд",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "До миллиарда секунд",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = state.formattedCountdown,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            if (state.isUnknownBirthTime) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "* время рождения не указано, отсчёт с полудня",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
