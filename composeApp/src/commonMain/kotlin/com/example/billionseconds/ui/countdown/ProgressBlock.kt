package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.CountdownUiState

@Composable
fun ProgressBlock(state: CountdownUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Прогресс",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = state.formattedProgress,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { state.progressFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
