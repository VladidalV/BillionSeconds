package com.example.billionseconds.ui.lifestats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.LifeStatsError
import com.example.billionseconds.mvi.LifeStatsUiState
import com.example.billionseconds.mvi.StatItem

@Composable
fun LifeStatsScreen(
    uiState: LifeStatsUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onIntent(AppIntent.LifeStatsScreenStarted)
    }

    when {
        uiState.isLoading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.error == LifeStatsError.NoBirthData -> {
            NoBirthDataPlaceholder(modifier)
        }

        else -> {
            LifeStatsContent(uiState = uiState, modifier = modifier)
        }
    }
}

@Composable
private fun LifeStatsContent(uiState: LifeStatsUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero: возраст ─────────────────────────────────────────────────
        AgeHeroCard(ageLabel = uiState.ageLabel, isApproximate = uiState.isUnknownBirthTime)

        // ── Точные статистики ─────────────────────────────────────────────
        if (uiState.exactStats.isNotEmpty()) {
            SectionHeader(title = "Точные данные")
            uiState.exactStats.forEach { item ->
                LifeStatCard(item = item)
            }
        }

        // ── Приблизительные статистики ────────────────────────────────────
        if (uiState.approximateStats.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            SectionHeader(title = "Приблизительно")

            if (uiState.isUnknownBirthTime) {
                ApproximateDisclaimer(
                    text = "Время рождения не указано. Все расчёты выполнены с полудня дня рождения."
                )
            }

            uiState.approximateStats.forEach { item ->
                LifeStatCard(item = item)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AgeHeroCard(ageLabel: String, isApproximate: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Твой возраст",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = if (isApproximate) "\u2248\u00A0$ageLabel" else ageLabel,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
}

@Composable
private fun ApproximateDisclaimer(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun LifeStatCard(item: StatItem, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isApproximate)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                )
            }
            if (item.disclaimer != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.disclaimer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoBirthDataPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "\uD83D\uDCC5", style = MaterialTheme.typography.displayMedium)
            Text(text = "Нет данных о дате рождения", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Пройдите онбординг, чтобы увидеть статистику.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
