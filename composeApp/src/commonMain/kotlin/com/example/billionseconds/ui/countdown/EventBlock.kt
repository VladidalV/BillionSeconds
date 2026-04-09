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
fun EventBlock(state: CountdownUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state.eventStatus) {
            EventStatus.Reached -> {
                Text(
                    text = "\uD83C\uDF89",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ты достиг миллиарда секунд!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            EventStatus.Today -> {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Сегодня твой день! \uD83C\uDF1F",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(12.dp))
                MilestoneDateTimeInfo(state)
            }

            EventStatus.Upcoming -> {
                Text(
                    text = "Твой миллиард секунд",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                MilestoneDateTimeInfo(state)
            }
        }
    }
}

@Composable
private fun MilestoneDateTimeInfo(state: CountdownUiState) {
    Text(
        text = state.formattedMilestoneDate,
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
    if (!state.isUnknownBirthTime) {
        Text(
            text = state.formattedMilestoneTime,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    } else {
        Text(
            text = "Точное время неизвестно",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
