package com.example.billionseconds.ui.family

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.FamilySubScreen
import com.example.billionseconds.mvi.FamilyUiState

@Composable
fun FamilyScreen(
    uiState: FamilyUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onIntent(AppIntent.FamilyScreenStarted)
    }

    // Delete confirmation dialog
    if (uiState.isDeleteConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onIntent(AppIntent.DeleteDismissed) },
            title = { Text("Удалить профиль?") },
            text = {
                val name = uiState.profiles
                    .firstOrNull { it.id == uiState.pendingDeleteId }?.name ?: ""
                Text("Профиль «$name» будет удалён безвозвратно.")
            },
            confirmButton = {
                TextButton(
                    onClick = { onIntent(AppIntent.DeleteConfirmed) }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(AppIntent.DeleteDismissed) }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Sub-screen routing
    when (val sub = uiState.subScreen) {
        is FamilySubScreen.List -> {
            if (uiState.isLoading) {
                // Loading placeholder — BuildFamilyUiState runs synchronously so this is brief
                CircularProgressIndicator(modifier = modifier.fillMaxSize())
            } else {
                FamilyListContent(
                    uiState = uiState,
                    onIntent = onIntent,
                    modifier = modifier
                )
            }
        }
        is FamilySubScreen.CreateForm -> {
            val draft = uiState.formDraft
            if (draft != null) {
                FamilyProfileForm(
                    draft = draft,
                    isEdit = false,
                    onIntent = onIntent,
                    modifier = modifier
                )
            }
        }
        is FamilySubScreen.EditForm -> {
            val draft = uiState.formDraft
            if (draft != null) {
                FamilyProfileForm(
                    draft = draft,
                    isEdit = true,
                    onIntent = onIntent,
                    modifier = modifier
                )
            }
        }
    }
}
