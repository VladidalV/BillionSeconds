package com.example.billionseconds.ui.lifestats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.CountdownUiState

@Composable
fun LifeStatsScreen(
    countdown: CountdownUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondsLived = (countdown.progressFraction * 1_000_000_000L).toLong()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Статистика жизни",
            style = MaterialTheme.typography.headlineSmall
        )

        StatCard(label = "Секунд прожито",   value = secondsLived.formatLarge())
        StatCard(label = "Минут прожито",     value = (secondsLived / 60).formatLarge())
        StatCard(label = "Часов прожито",     value = (secondsLived / 3_600).formatLarge())
        StatCard(label = "Дней прожито",      value = (secondsLived / 86_400).formatLarge())
        StatCard(label = "Недель прожито",    value = (secondsLived / 604_800).formatLarge())
        StatCard(label = "Прогресс к миллиарду", value = countdown.formattedProgress)

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Каждая секунда — это часть твоей уникальной истории.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

/** Форматирует число с разделителем тысяч пробелом: 1 234 567 */
private fun Long.formatLarge(): String {
    if (this < 0L) return "0"
    return toString().reversed().chunked(3).joinToString("\u00A0").reversed()
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
