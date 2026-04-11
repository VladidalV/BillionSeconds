package com.example.billionseconds.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ProfileUiState

@Composable
fun NotificationSettingsContent(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = uiState.settings
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))

        SectionHeader(title = "Уведомления")

        ToggleRow(
            title = "Уведомления включены",
            subtitle = if (!s.notificationsEnabled)
                "Включите, чтобы получать напоминания" else null,
            checked = s.notificationsEnabled,
            onToggle = { onIntent(AppIntent.NotificationsToggled) }
        )

        if (!s.notificationsEnabled) {
            Text(
                text = "Разрешите уведомления в настройках системы, чтобы они работали",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        SectionHeader(title = "Типы уведомлений")

        ToggleRow(
            title = "Напоминания о вехах",
            subtitle = "Получать уведомление, когда приближается миллиард секунд",
            checked = s.milestoneRemindersEnabled,
            enabled = s.notificationsEnabled,
            onToggle = { onIntent(AppIntent.MilestoneRemindersToggled) }
        )

        ToggleRow(
            title = "Напоминания о семье",
            subtitle = "Уведомления о вехах членов семьи",
            checked = s.familyRemindersEnabled,
            enabled = s.notificationsEnabled,
            onToggle = { onIntent(AppIntent.FamilyRemindersToggled) }
        )

        ToggleRow(
            title = "Периодические напоминания",
            subtitle = "Мотивационные уведомления о прогрессе",
            checked = s.reengagementEnabled,
            enabled = s.notificationsEnabled,
            onToggle = { onIntent(AppIntent.ReengagementToggled) }
        )

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onIntent(AppIntent.ProfileSubScreenDismissed) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Назад")
        }

        Spacer(Modifier.height(16.dp))
    }
}
