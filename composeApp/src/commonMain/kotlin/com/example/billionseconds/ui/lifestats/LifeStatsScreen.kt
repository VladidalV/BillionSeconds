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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeStatsScreen(
    countdown: CountdownUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondsLived = (countdown.progressFraction * 1_000_000_000L).toLong()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика жизни") },
                navigationIcon = {
                    TextButton(onClick = { onIntent(AppIntent.BackClicked) }) {
                        Text("←")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                label = "Секунд прожито",
                value = "%,d".format(secondsLived)
            )
            StatCard(
                label = "Минут прожито",
                value = "%,d".format(secondsLived / 60)
            )
            StatCard(
                label = "Часов прожито",
                value = "%,d".format(secondsLived / 3_600)
            )
            StatCard(
                label = "Дней прожито",
                value = "%,d".format(secondsLived / 86_400)
            )
            StatCard(
                label = "Недель прожито",
                value = "%,d".format(secondsLived / 604_800)
            )
            StatCard(
                label = "Прогресс к миллиарду",
                value = countdown.formattedProgress
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Каждая секунда — это часть твоей уникальной истории.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
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
