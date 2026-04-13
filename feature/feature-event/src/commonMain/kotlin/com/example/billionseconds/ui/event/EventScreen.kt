package com.example.billionseconds.ui.event

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun EventScreen(
    uiState: EventUiState,
    onAction: (EventAction) -> Unit
) {
    var celebrationVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.triggerCelebration) {
        if (uiState.triggerCelebration) {
            celebrationVisible = true
            onAction(EventAction.CelebrationDisplayed)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        when (uiState.screenStatus) {
            EventScreenStatus.Loading -> EventLoadingContent()

            EventScreenStatus.NotEligible,
            EventScreenStatus.Error -> EventErrorContent(
                message = uiState.errorMessage ?: "Произошла ошибка",
                onRetry = { onAction(EventAction.RetryClicked) },
                onGoHome = { onAction(EventAction.GoHomeClicked) }
            )

            EventScreenStatus.ProfileMissing -> EventErrorContent(
                message = "Профиль не найден",
                onRetry = null,
                onGoHome = { onAction(EventAction.GoHomeClicked) }
            )

            EventScreenStatus.FirstTime,
            EventScreenStatus.Repeat -> {
                val uiModel = uiState.uiModel
                if (uiModel != null) {
                    EventMainContent(uiState = uiState, onAction = onAction)
                }

                if (celebrationVisible && uiState.screenStatus == EventScreenStatus.FirstTime) {
                    CelebrationOverlay(
                        onSkip = {
                            celebrationVisible = false
                            onAction(EventAction.CelebrationSkipped)
                        },
                        onCompleted = {
                            celebrationVisible = false
                            onAction(EventAction.CelebrationCompleted)
                        }
                    )
                }
            }
        }
    }
}

// ── Main Content ──────────────────────────────────────────────────────────────

@Composable
private fun EventMainContent(
    uiState: EventUiState,
    onAction: (EventAction) -> Unit
) {
    val uiModel = uiState.uiModel ?: return
    val actionsVisible = !uiState.isCelebrationRunning || uiState.celebrationCompleted

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(96.dp.toPx(), 200.dp.toPx()),
                    radius = 220.dp.toPx()
                ),
                radius = 220.dp.toPx(),
                center = Offset(96.dp.toPx(), 200.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width - 80.dp.toPx(), size.height - 200.dp.toPx()),
                    radius = 200.dp.toPx()
                ),
                radius = 200.dp.toPx(),
                center = Offset(size.width - 80.dp.toPx(), size.height - 200.dp.toPx())
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, bottom = 48.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Repeat mode note
            uiModel.repeatModeNote?.let { note ->
                Text(
                    text = note,
                    color = AppColors.textSubtle,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))
            }

            // Profile label
            Text(
                text = uiModel.profileLabel.uppercase(),
                color = AppColors.textLabel,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text = uiModel.title,
                color = AppColors.textHeading,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1.2).sp,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(10.dp))

            // Subtitle
            Text(
                text = uiModel.subtitle,
                color = AppColors.purpleAccent,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(40.dp))

            // Date card
            EventDateCard(uiModel = uiModel)

            Spacer(Modifier.height(40.dp))

            // Action buttons
            AnimatedVisibility(
                visible = actionsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EventActionButtons(uiModel = uiModel, onAction = onAction)
            }

            Spacer(Modifier.height(20.dp))

            // Dismiss button
            if (uiState.isBackAllowed) {
                Text(
                    text = "Закрыть",
                    color = AppColors.textLabel,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onAction(EventAction.DismissClicked) }
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                )
            }
        }

        // Frosted top bar
        EventTopBar()
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.EventTopBar() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.headerBackground)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "✦",
            color = AppColors.purpleAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Billion Seconds",
            color = AppColors.purpleAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.36).sp
        )
    }
}

// ── Date Card ─────────────────────────────────────────────────────────────────

@Composable
private fun EventDateCard(uiModel: EventUiModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.cardDark)
    ) {
        // Subtle gradient overlay inside card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.purpleAccent.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "TEMPORAL TARGET",
                color = AppColors.purpleAccent.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(4.dp))

            // Event date with gradient text
            Text(
                text = uiModel.eventDateText,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                    ),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.8).sp,
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = uiModel.reachedText,
                color = AppColors.textBody,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            if (uiModel.isApproximateLabelVisible) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppColors.dangerBackground)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = uiModel.approximateLabel,
                        color = AppColors.textDanger,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ── Loading ───────────────────────────────────────────────────────────────────

@Composable
private fun EventLoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.purpleAccent)
    }
}

// ── Error ─────────────────────────────────────────────────────────────────────

@Composable
private fun EventErrorContent(
    message: String,
    onRetry: (() -> Unit)?,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = AppColors.textBody,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(32.dp))

        onRetry?.let { retry ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
                    .clickable(onClick = retry)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Попробовать снова",
                    color = AppColors.buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(AppColors.cardDark)
                .clickable(onClick = onGoHome)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "На главную",
                color = AppColors.textBody,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
