package com.example.billionseconds.ui.family

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.FamilyProfileUiItem

@Composable
fun FamilyProfileCard(
    item: FamilyProfileUiItem,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        item.isActive  -> MaterialTheme.colorScheme.primaryContainer
        item.isPrimary -> MaterialTheme.colorScheme.secondaryContainer
        else           -> MaterialTheme.colorScheme.surfaceVariant
    }
    val onContainerColor = when {
        item.isActive  -> MaterialTheme.colorScheme.onPrimaryContainer
        item.isPrimary -> MaterialTheme.colorScheme.onSecondaryContainer
        else           -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: emoji + name + active badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = item.relationEmoji, style = MaterialTheme.typography.titleLarge)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = onContainerColor
                    )
                    Text(
                        text = item.relationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainerColor.copy(alpha = 0.7f)
                    )
                }
                if (item.isActive) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "✓ Активен",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatColumn(
                    label = "Дата рождения",
                    value = item.birthDateText,
                    color = onContainerColor,
                    modifier = Modifier.weight(1f)
                )
                StatColumn(
                    label = "Миллиард секунд",
                    value = item.billionDateText,
                    color = onContainerColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatColumn(
                    label = "Прогресс",
                    value = item.progressText,
                    color = onContainerColor,
                    modifier = Modifier.weight(1f)
                )
                StatColumn(
                    label = "Осталось",
                    value = item.countdownText,
                    color = onContainerColor,
                    modifier = Modifier.weight(1f)
                )
            }

            if (item.hasApproximateTime) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "~ время рождения не указано",
                    style = MaterialTheme.typography.labelSmall,
                    color = onContainerColor.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!item.isActive) {
                    OutlinedButton(
                        onClick = onSetActive,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Выбрать", style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (item.isEditable) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = if (item.isActive) Modifier.weight(1f) else Modifier
                    ) {
                        Text("Изменить", style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (item.isDeletable) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Удалить", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
