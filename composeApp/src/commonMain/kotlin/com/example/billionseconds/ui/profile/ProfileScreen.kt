package com.example.billionseconds.ui.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ProfileSubScreen
import com.example.billionseconds.mvi.ProfileUiState

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onIntent(AppIntent.ProfileScreenStarted)
    }

    when (uiState.subScreen) {
        is ProfileSubScreen.Root ->
            ProfileRootContent(
                uiState = uiState,
                onIntent = onIntent,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.NotificationSettings ->
            NotificationSettingsContent(
                uiState = uiState,
                onIntent = onIntent,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.AppSettings ->
            AppSettingsContent(
                uiState = uiState,
                onIntent = onIntent,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.DataManagement ->
            DataManagementContent(
                uiState = uiState,
                onIntent = onIntent,
                modifier = modifier.fillMaxSize()
            )

        is ProfileSubScreen.AboutApp ->
            AboutAppContent(
                uiState = uiState,
                onIntent = onIntent,
                modifier = modifier.fillMaxSize()
            )
    }
}
