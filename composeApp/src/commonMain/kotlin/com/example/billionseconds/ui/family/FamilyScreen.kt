package com.example.billionseconds.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.FamilySubScreen
import com.example.billionseconds.mvi.FamilyUiState
import com.example.billionseconds.ui.theme.AppColors

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
            containerColor = AppColors.cardDark,
            titleContentColor = AppColors.textHeading,
            textContentColor = AppColors.textBody,
            title = {
                Text(
                    text = "Удалить профиль?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val name = uiState.profiles
                    .firstOrNull { it.id == uiState.pendingDeleteId }?.name ?: ""
                Text(
                    text = "Профиль «$name» будет удалён безвозвратно.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AppColors.dangerBackground)
                        .border(1.dp, AppColors.textDanger.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .clickable { onIntent(AppIntent.DeleteConfirmed) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Удалить",
                        color = AppColors.textDanger,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onIntent(AppIntent.DeleteDismissed) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Отмена",
                        color = AppColors.textLabel,
                        fontSize = 14.sp
                    )
                }
            }
        )
    }

    // Sub-screen routing
    when (uiState.subScreen) {
        is FamilySubScreen.List -> {
            if (uiState.isLoading) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(AppColors.backgroundScreen),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.purpleAccent)
                }
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
