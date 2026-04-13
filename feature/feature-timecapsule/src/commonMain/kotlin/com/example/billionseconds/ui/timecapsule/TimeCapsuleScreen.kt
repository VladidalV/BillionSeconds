package com.example.billionseconds.ui.timecapsule

import androidx.compose.runtime.*

@Composable
fun TimeCapsuleScreen(
    uiState: TimeCapsuleUiState,
    onAction: (TimeCapsuleAction) -> Unit
) {
    LaunchedEffect(Unit) {
        onAction(TimeCapsuleAction.ScreenStarted)
    }

    when (val sub = uiState.subScreen) {
        is TimeCapsuleSubScreen.List   -> CapsuleListContent(uiState = uiState, onAction = onAction)
        is TimeCapsuleSubScreen.Create -> CapsuleCreateEditForm(
            draft = uiState.formDraft ?: return@TimeCapsuleScreen,
            isEdit = false,
            onAction = onAction
        )
        is TimeCapsuleSubScreen.Edit   -> CapsuleCreateEditForm(
            draft = uiState.formDraft ?: return@TimeCapsuleScreen,
            isEdit = true,
            onAction = onAction
        )
        is TimeCapsuleSubScreen.Open   -> CapsuleOpenContent(
            capsule = uiState.openedCapsule ?: return@TimeCapsuleScreen,
            onAction = onAction
        )
    }
}
