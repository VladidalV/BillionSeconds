package com.example.billionseconds.ui.milestones

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun MilestonesScreen(
    uiState: MilestonesUiState,
    onAction: (MilestonesAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onAction(MilestonesAction.ScreenStarted)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.purpleAccent)
                }
            }

            uiState.error == MilestonesError.NoBirthData -> {
                NoBirthDataPlaceholder()
            }

            uiState.milestones.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Вехи не настроены",
                        color = AppColors.textSubtle,
                        fontSize = 16.sp
                    )
                }
            }

            else -> {
                MilestonesContent(uiState = uiState, onAction = onAction)
            }
        }
    }

    // Celebration dialog — renders above everything
    uiState.celebrationAvailableId?.let { id ->
        val item = uiState.milestones.firstOrNull { it.id == id }
        if (item != null) {
            CelebrationBanner(
                title = item.title,
                onDismiss = { onAction(MilestonesAction.CelebrationDismissed) },
                onShare = { onAction(MilestonesAction.ShareClicked(id)) }
            )
        }
    }
}

// ── Main Content ──────────────────────────────────────────────────────────────

@Composable
private fun MilestonesContent(
    uiState: MilestonesUiState,
    onAction: (MilestonesAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width * 0.75f, 200.dp.toPx()),
                    radius = 220.dp.toPx()
                ),
                radius = 220.dp.toPx(),
                center = Offset(size.width * 0.75f, 200.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height - 200.dp.toPx()),
                    radius = 190.dp.toPx()
                ),
                radius = 190.dp.toPx(),
                center = Offset(size.width * 0.2f, size.height - 200.dp.toPx())
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 24.dp,
                bottom = bottomPadding,
                start = 20.dp,
                end = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Section header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AppColors.purpleAccent.copy(alpha = 0.6f))
                    )
                    Text(
                        text = "КОСМИЧЕСКИЕ ВЕХИ",
                        color = AppColors.textLabel,
                        fontSize = 11.sp,
                        letterSpacing = 2.4.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Approximate disclaimer
            if (uiState.isApproximateMode) {
                item {
                    ApproximateDisclaimer()
                }
            }

            items(uiState.milestones, key = { it.id }) { item ->
                MilestoneCard(
                    item = item,
                    isHighlighted = item.id == uiState.highlightedId,
                    onShareClick = { onAction(MilestonesAction.ShareClicked(item.id)) }
                )
            }
        }

    }
}

// ── Approximate Disclaimer ────────────────────────────────────────────────────

@Composable
private fun ApproximateDisclaimer() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.dangerBackground)
            .border(1.dp, AppColors.textDanger.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "◎", color = AppColors.textDanger, fontSize = 13.sp)
        Text(
            text = "Время рождения не указано. Все даты приблизительны (±12 часов).",
            color = AppColors.textDanger,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

// ── Celebration Banner ────────────────────────────────────────────────────────

@Composable
private fun CelebrationBanner(
    title: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.cardDark,
        titleContentColor = AppColors.textHeading,
        textContentColor = AppColors.textBody,
        title = {
            Text(
                text = "\uD83C\uDF89 Поздравляем!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Вы достигли вехи\n«$title»!",
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
                    .clickable {
                        onShare()
                        onDismiss()
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Поделиться",
                    color = AppColors.buttonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Закрыть",
                    color = AppColors.textLabel,
                    fontSize = 14.sp
                )
            }
        }
    )
}

// ── No Birth Data ─────────────────────────────────────────────────────────────

@Composable
private fun NoBirthDataPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "◈", color = AppColors.purpleAccent, fontSize = 48.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Нет данных",
                color = AppColors.textHeading,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Пройдите онбординг, чтобы\nувидеть достижения.",
                color = AppColors.textBody,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
