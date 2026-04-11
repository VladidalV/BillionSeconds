package com.example.billionseconds.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ProfileConfirmDialog
import com.example.billionseconds.mvi.ProfileUiState

@Composable
fun DataManagementContent(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Confirm dialog
    uiState.confirmDialog?.let { dialog ->
        val (title, body) = when (dialog) {
            ProfileConfirmDialog.ResetOnboarding ->
                "Сбросить онбординг?" to
                "Все данные будут удалены. Вы начнёте приложение заново. Это действие необратимо."
            ProfileConfirmDialog.ClearAllData ->
                "Удалить все данные?" to
                "Все профили, настройки и локальные данные будут удалены навсегда. Это действие необратимо."
        }
        AlertDialog(
            onDismissRequest = { if (!uiState.isActionInProgress) onIntent(AppIntent.DismissConfirmDialog) },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                if (uiState.isActionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    TextButton(onClick = { onIntent(AppIntent.ConfirmDangerousAction) }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onIntent(AppIntent.DismissConfirmDialog) },
                    enabled = !uiState.isActionInProgress
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))

        SectionHeader(title = "Управление данными")

        Text(
            text = "Действия в этом разделе необратимы. Будьте внимательны.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(8.dp))

        DangerousActionRow(
            title = "Сбросить онбординг",
            subtitle = "Удалить все данные и вернуться к началу",
            onClick = { onIntent(AppIntent.ResetOnboardingClicked) }
        )

        DangerousActionRow(
            title = "Удалить все данные",
            subtitle = "Полная очистка: профили, настройки, данные",
            onClick = { onIntent(AppIntent.ClearAllDataClicked) }
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

@Composable
private fun DangerousActionRow(
    title: String,
    subtitle: String,
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
            Text(text = "⚠️", style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
