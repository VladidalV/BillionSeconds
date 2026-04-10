package com.example.billionseconds.ui.milestones

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.domain.MilestoneStatus
import com.example.billionseconds.mvi.MilestoneUiItem

@Composable
fun MilestoneCard(
    item: MilestoneUiItem,
    isHighlighted: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isHighlighted -> MaterialTheme.colorScheme.primaryContainer
        item.status is MilestoneStatus.Reached -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isHighlighted -> MaterialTheme.colorScheme.onPrimaryContainer
        item.status is MilestoneStatus.Reached -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Header row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = if (item.isPrimary) MaterialTheme.typography.titleMedium
                        else MaterialTheme.typography.bodyLarge,
                        color = contentColor
                    )
                    Text(
                        text = item.targetDateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(label = item.statusLabel, status = item.status)
            }

            // ── Progress (только для Next) ────────────────────────────────
            if (item.status is MilestoneStatus.Next && item.progressText.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { (item.progressText.trimEnd('%').toFloatOrNull() ?: 0f) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.remainingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = item.progressText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Reached date + share ──────────────────────────────────────
            if (item.status is MilestoneStatus.Reached) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.reachedDateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    if (item.isShareable) {
                        TextButton(
                            onClick = onShareClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Поделиться",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // ── Approximate disclaimer ────────────────────────────────────
            if (item.hasApproximateDisclaimer) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Дата приблизительная — время рождения не указано",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, status: MilestoneStatus) {
    val badgeColor = when (status) {
        is MilestoneStatus.Reached  -> MaterialTheme.colorScheme.tertiary
        is MilestoneStatus.Next     -> MaterialTheme.colorScheme.primary
        is MilestoneStatus.Upcoming -> MaterialTheme.colorScheme.outline
    }
    Surface(
        color = badgeColor.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = badgeColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
