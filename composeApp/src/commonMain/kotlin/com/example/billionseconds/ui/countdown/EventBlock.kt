package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.domain.model.EventStatus
import com.example.billionseconds.mvi.CountdownUiState
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun EventBlock(state: CountdownUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (state.eventStatus) {
            EventStatus.Reached -> {
                ReachedHeader()
            }

            EventStatus.Today -> {
                TodayBanner()
                Spacer(Modifier.height(4.dp))
                MilestoneDateTime(state)
            }

            EventStatus.Upcoming -> {
                Text(
                    text = "A TEMPORAL ARTIFACT",
                    color = AppColors.purpleAccent.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Твой миллиард секунд",
                    color = AppColors.textHeading,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Today Banner ──────────────────────────────────────────────────────────────

@Composable
private fun TodayBanner() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                )
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✦ Сегодня твой день!",
            color = AppColors.buttonText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )
    }
}

// ── Reached Header ────────────────────────────────────────────────────────────

@Composable
private fun ReachedHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "\uD83C\uDF89", fontSize = 48.sp)
        Text(
            text = "Ты достиг",
            color = AppColors.textBody,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "миллиарда секунд!",
            style = TextStyle(
                brush = Brush.linearGradient(
                    listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                ),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp,
                textAlign = TextAlign.Center
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ── Milestone Date + Time ─────────────────────────────────────────────────────

@Composable
private fun MilestoneDateTime(state: CountdownUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = state.formattedMilestoneDate,
            style = TextStyle(
                brush = Brush.linearGradient(
                    listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                ),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.0).sp,
                textAlign = TextAlign.Center
            ),
            textAlign = TextAlign.Center
        )
        if (!state.isUnknownBirthTime) {
            Text(
                text = state.formattedMilestoneTime,
                color = AppColors.textBody,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Точное время неизвестно",
                color = AppColors.textSubtle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
