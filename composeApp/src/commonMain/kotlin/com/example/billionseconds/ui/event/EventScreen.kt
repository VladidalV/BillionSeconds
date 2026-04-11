package com.example.billionseconds.ui.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.mvi.AppEffect
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.event.EventScreenStatus
import com.example.billionseconds.mvi.event.EventUiState
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun EventScreen(
    state: EventUiState,
    effects: SharedFlow<AppEffect>,
    onIntent: (AppIntent) -> Unit
) {
    var celebrationVisible by remember { mutableStateOf(false) }

    // Подписка на эффекты celebration
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is AppEffect.TriggerCelebrationAnimation -> {
                    celebrationVisible = true
                    onIntent(AppIntent.Event.CelebrationDisplayed)
                }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state.screenStatus) {
            EventScreenStatus.Loading -> EventLoadingContent()

            EventScreenStatus.NotEligible,
            EventScreenStatus.Error -> EventErrorContent(
                message = state.errorMessage ?: "Произошла ошибка",
                onRetry = { onIntent(AppIntent.Event.RetryClicked) },
                onGoHome = { onIntent(AppIntent.Event.GoHomeClicked) }
            )

            EventScreenStatus.ProfileMissing -> EventErrorContent(
                message = "Профиль не найден",
                onRetry = null,
                onGoHome = { onIntent(AppIntent.Event.GoHomeClicked) }
            )

            EventScreenStatus.FirstTime,
            EventScreenStatus.Repeat -> {
                val uiModel = state.uiModel
                if (uiModel != null) {
                    EventMainContent(
                        state    = state,
                        onIntent = onIntent
                    )
                }

                // Celebration overlay поверх контента
                if (celebrationVisible && state.screenStatus == EventScreenStatus.FirstTime) {
                    CelebrationOverlay(
                        onSkip = {
                            celebrationVisible = false
                            onIntent(AppIntent.Event.CelebrationSkipped)
                        },
                        onCompleted = {
                            celebrationVisible = false
                            onIntent(AppIntent.Event.CelebrationCompleted)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventMainContent(
    state: EventUiState,
    onIntent: (AppIntent) -> Unit
) {
    val uiModel = state.uiModel ?: return
    val actionsVisible = !state.isCelebrationRunning || state.celebrationCompleted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // Repeat mode note
        uiModel.repeatModeNote?.let { note ->
            Text(
                text  = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }

        // Profile label
        Text(
            text  = uiModel.profileLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        // Title
        Text(
            text       = uiModel.title,
            style      = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))

        // Subtitle
        Text(
            text  = uiModel.subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        // Event date
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text  = uiModel.eventDateText,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = uiModel.reachedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (uiModel.isApproximateLabelVisible) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = uiModel.approximateLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Action buttons — скрыты во время celebration
        AnimatedVisibility(
            visible = actionsVisible,
            enter   = fadeIn(),
            exit    = fadeOut()
        ) {
            EventActionButtons(
                uiModel  = uiModel,
                onIntent = onIntent
            )
        }

        Spacer(Modifier.height(24.dp))

        // Dismiss / Back button — только в repeat mode или после celebration
        if (state.isBackAllowed) {
            TextButton(onClick = { onIntent(AppIntent.Event.DismissClicked) }) {
                Text("Закрыть")
            }
        }
    }
}

@Composable
private fun EventLoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EventErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text      = message,
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))
        onRetry?.let {
            Button(onClick = it) { Text("Попробовать снова") }
            Spacer(Modifier.height(8.dp))
        }
        OutlinedButton(onClick = onGoHome) { Text("На главную") }
    }
}
