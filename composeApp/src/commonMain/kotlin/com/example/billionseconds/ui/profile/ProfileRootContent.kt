package com.example.billionseconds.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ActiveProfileSummary
import com.example.billionseconds.mvi.ProfileSubScreen
import com.example.billionseconds.mvi.ProfileUiState

@Composable
fun ProfileRootContent(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Active Profile Summary
        item {
            ActiveProfileSummaryCard(
                summary = uiState.activeProfileSummary,
                onFamilyClick = { onIntent(AppIntent.ActiveProfileSummaryClicked) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        // Возможности section
        item {
            SectionHeader(title = "Возможности")
            StubNavigationRow(
                emoji = "⭐",
                title = "Premium",
                subtitle = "Скоро",
                onClick = { onIntent(AppIntent.PremiumClicked) }
            )
            StubNavigationRow(
                emoji = "🕰",
                title = "Time Capsule",
                subtitle = "Скоро",
                onClick = { onIntent(AppIntent.TimeCapsuleClicked) }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Настройки section
        item {
            SectionHeader(title = "Настройки")
            NavigationRow(
                emoji = "🔔",
                title = "Уведомления",
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.NotificationSettings)) }
            )
            NavigationRow(
                emoji = "⚙️",
                title = "Параметры приложения",
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.AppSettings)) }
            )
            Spacer(Modifier.height(8.dp))
        }

        // Данные section
        item {
            SectionHeader(title = "Данные")
            NavigationRow(
                emoji = "🗂",
                title = "Управление данными",
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.DataManagement)) }
            )
            Spacer(Modifier.height(8.dp))
        }

        // О приложении section
        item {
            SectionHeader(title = "О приложении")
            NavigationRow(
                emoji = "ℹ️",
                title = "О приложении",
                subtitle = uiState.appVersion.takeIf { it.isNotEmpty() },
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.AboutApp)) }
            )
            StubNavigationRow(
                emoji = "❓",
                title = "Помощь",
                subtitle = "Скоро",
                onClick = { onIntent(AppIntent.HelpClicked) }
            )
        }
    }
}

@Composable
private fun ActiveProfileSummaryCard(
    summary: ActiveProfileSummary?,
    onFamilyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (summary == null) {
                Text(
                    text = "Профиль не найден",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onFamilyClick) {
                    Text("Перейти в Семью")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = summary.relationEmoji, style = MaterialTheme.typography.headlineMedium)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = summary.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = summary.relationLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Миллиард секунд: ${summary.billionDateText}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (summary.hasApproximateTime) {
                    Text(
                        text = "~ время рождения не указано",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onFamilyClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Управление профилями →")
                }
            }
        }
    }
}

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
internal fun NavigationRow(
    emoji: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun StubNavigationRow(
    emoji: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.4f
                    )
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled
        )
    }
}
