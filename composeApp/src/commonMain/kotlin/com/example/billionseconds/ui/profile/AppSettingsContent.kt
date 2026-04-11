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
fun AppSettingsContent(
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

        SectionHeader(title = "Отображение")

        ToggleRow(
            title = "Приблизительные метки (~)",
            subtitle = "Показывать ~ перед датами, если время рождения не указано",
            checked = s.approximateLabelsEnabled,
            onToggle = { onIntent(AppIntent.ApproximateLabelsToggled) }
        )

        ToggleRow(
            title = "24-часовой формат времени",
            subtitle = "Использовать 24ч вместо AM/PM",
            checked = s.use24HourFormat,
            onToggle = { onIntent(AppIntent.Use24HourFormatToggled) }
        )

        Spacer(Modifier.height(24.dp))

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
