package com.example.billionseconds.ui.milestones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.MilestonesError
import com.example.billionseconds.mvi.MilestonesUiState

@Composable
fun MilestonesScreen(
    uiState: MilestonesUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onIntent(AppIntent.MilestonesScreenStarted)
    }

    when {
        uiState.isLoading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.error == MilestonesError.NoBirthData -> {
            NoBirthDataPlaceholder(modifier)
        }

        uiState.milestones.isEmpty() -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Вехи не настроены",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        else -> {
            MilestonesContent(
                uiState  = uiState,
                onIntent = onIntent,
                modifier = modifier
            )
        }
    }

    // Celebration banner
    uiState.celebrationAvailableId?.let { id ->
        val item = uiState.milestones.firstOrNull { it.id == id }
        if (item != null) {
            CelebrationBanner(
                title     = item.title,
                onDismiss = { onIntent(AppIntent.MilestoneCelebrationDismissed) },
                onShare   = { onIntent(AppIntent.MilestoneShareClicked(id)) }
            )
        }
    }
}

@Composable
private fun MilestonesContent(
    uiState: MilestonesUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // Approximate disclaimer
        if (uiState.isApproximateMode) {
            item {
                ApproximateDisclaimer()
            }
        }

        items(uiState.milestones, key = { it.id }) { item ->
            MilestoneCard(
                item          = item,
                isHighlighted = item.id == uiState.highlightedId,
                onShareClick  = { onIntent(AppIntent.MilestoneShareClicked(item.id)) }
            )
        }
    }
}

@Composable
private fun ApproximateDisclaimer(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "Время рождения не указано. Все даты приблизительны (±12 часов).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CelebrationBanner(
    title: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "\uD83C\uDF89 Поздравляем!")
        },
        text = {
            Text(text = "Вы достигли вехи\n«$title»!")
        },
        confirmButton = {
            TextButton(onClick = {
                onShare()
                onDismiss()
            }) {
                Text("Поделиться")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun NoBirthDataPlaceholder(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "\uD83C\uDFC6", style = MaterialTheme.typography.displayMedium)
            Text(
                text = "Нет данных о дате рождения",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Пройдите онбординг, чтобы увидеть достижения.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
