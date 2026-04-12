package com.example.billionseconds.ui.milestones

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.domain.MilestoneStatus
import com.example.billionseconds.mvi.MilestoneUiItem
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun MilestoneCard(
    item: MilestoneUiItem,
    isHighlighted: Boolean,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNext = item.status is MilestoneStatus.Next
    val isReached = item.status is MilestoneStatus.Reached
    val isActive = isHighlighted || isNext

    val borderColor = when {
        isActive   -> AppColors.purpleAccent.copy(alpha = 0.3f)
        isReached  -> AppColors.blueAccent.copy(alpha = 0.2f)
        else       -> AppColors.cardBorder
    }
    val cardBackground = when {
        isReached -> AppColors.cardMid
        else      -> AppColors.cardDark
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBackground)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
    ) {
        // Left accent bar for Next / Highlighted
        if (isActive) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (isActive) 18.dp else 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Icon + title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val icon = when {
                            item.isPrimary -> "◈"
                            isReached      -> "◎"
                            isNext         -> "◉"
                            else           -> "○"
                        }
                        val iconColor = when {
                            isActive  -> AppColors.purpleAccent
                            isReached -> AppColors.blueAccent
                            else      -> AppColors.textSubtle
                        }
                        Text(
                            text = icon,
                            color = iconColor,
                            fontSize = if (item.isPrimary) 18.sp else 14.sp
                        )
                        Text(
                            text = item.title,
                            color = when {
                                isActive  -> AppColors.textHeading
                                isReached -> AppColors.textBody
                                else      -> AppColors.textBody.copy(alpha = 0.6f)
                            },
                            fontSize = if (item.isPrimary || isActive) 16.sp else 14.sp,
                            fontWeight = if (item.isPrimary || isActive) FontWeight.SemiBold else FontWeight.Normal,
                            letterSpacing = (-0.3).sp
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = item.targetDateText,
                        color = when {
                            isActive  -> AppColors.purpleAccent.copy(alpha = 0.8f)
                            isReached -> AppColors.blueAccent.copy(alpha = 0.7f)
                            else      -> AppColors.textLabel
                        },
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Status badge
                StatusBadge(label = item.statusLabel, status = item.status)
            }

            // ── Progress bar (Next only) ───────────────────────────────────────
            if (isNext && item.progressText.isNotEmpty()) {
                val progress = item.progressText.trimEnd('%').toFloatOrNull()?.div(100f) ?: 0f

                Spacer(Modifier.height(14.dp))

                // Track + fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppColors.stepInactive)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                                )
                            )
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.remainingText,
                        color = AppColors.textSubtle,
                        fontSize = 11.sp
                    )
                    Text(
                        text = item.progressText,
                        color = AppColors.purpleAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Reached: date + share ─────────────────────────────────────────
            if (isReached && item.reachedDateText.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AppColors.divider)
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.reachedDateText,
                        color = AppColors.textSubtle,
                        fontSize = 12.sp
                    )
                    if (item.isShareable) {
                        Text(
                            text = "Поделиться →",
                            color = AppColors.purpleAccent.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onShareClick)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // ── Approximate disclaimer ────────────────────────────────────────
            if (item.hasApproximateDisclaimer) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "~ дата приблизительная — время рождения не указано",
                    color = AppColors.textSubtle,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
private fun StatusBadge(label: String, status: MilestoneStatus) {
    val (badgeColor, bgColor) = when (status) {
        is MilestoneStatus.Reached  -> AppColors.blueAccent  to AppColors.blueAccent.copy(alpha = 0.12f)
        is MilestoneStatus.Next     -> AppColors.purpleAccent to AppColors.purpleAccent.copy(alpha = 0.12f)
        is MilestoneStatus.Upcoming -> AppColors.textLabel    to AppColors.cardBorder
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = badgeColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}
