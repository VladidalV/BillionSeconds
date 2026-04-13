package com.example.billionseconds.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onAction(ProfileAction.ScreenStarted)
    }

    // Перехватываем системную кнопку Back на Android, когда открыт sub-screen
    PlatformBackHandler(enabled = uiState.subScreen !is ProfileSubScreen.Root) {
        onAction(ProfileAction.SubScreenDismissed)
    }

    when (uiState.subScreen) {
        is ProfileSubScreen.Root ->
            ProfileRootContent(
                uiState = uiState,
                onAction = onAction,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.NotificationSettings ->
            NotificationSettingsContent(
                uiState = uiState,
                onAction = onAction,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.AppSettings ->
            AppSettingsContent(
                uiState = uiState,
                onAction = onAction,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.DataManagement ->
            DataManagementContent(
                uiState = uiState,
                onAction = onAction,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.AboutApp ->
            AboutAppContent(
                uiState = uiState,
                onAction = onAction,
                modifier = modifier.fillMaxSize()
            )
    }
}
