package com.example.billionseconds.ui.timecapsule

import androidx.compose.runtime.*
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.TimeCapsuleSubScreen
import com.example.billionseconds.mvi.TimeCapsuleUiState

@Composable
fun TimeCapsuleScreen(
    uiState: TimeCapsuleUiState,
    onIntent: (AppIntent) -> Unit
) {
    LaunchedEffect(Unit) {
        onIntent(AppIntent.TimeCapsule.ScreenStarted)
    }

    when (val sub = uiState.subScreen) {
        is TimeCapsuleSubScreen.List   -> CapsuleListContent(uiState = uiState, onIntent = onIntent)
        is TimeCapsuleSubScreen.Create -> CapsuleCreateEditForm(
            draft = uiState.formDraft ?: return@TimeCapsuleScreen,
            isEdit = false,
            onIntent = onIntent
        )
        is TimeCapsuleSubScreen.Edit   -> CapsuleCreateEditForm(
            draft = uiState.formDraft ?: return@TimeCapsuleScreen,
            isEdit = true,
            onIntent = onIntent
        )
        is TimeCapsuleSubScreen.Open   -> CapsuleOpenContent(
            capsule = uiState.openedCapsule ?: return@TimeCapsuleScreen,
            onIntent = onIntent
        )
    }
}
