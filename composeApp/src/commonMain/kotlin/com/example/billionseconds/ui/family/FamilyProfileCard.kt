package com.example.billionseconds.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.example.billionseconds.mvi.FamilyProfileUiItem
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun FamilyProfileCard(
    item: FamilyProfileUiItem,
    onSetActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBorderColor = if (item.isActive)
        AppColors.purpleAccent.copy(alpha = 0.25f)
    else
        AppColors.cardBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(AppColors.cardDark)
            .border(1.dp, cardBorderColor, RoundedCornerShape(18.dp))
    ) {
        // Active indicator: left accent bar
        if (item.isActive) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .align(Alignment.TopStart)
                    .offset(y = 20.dp)
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
            )
        }

        Column(modifier = Modifier.padding(20.dp)) {
            // ── Header row ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar circle with emoji
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.isActive)
                                Brush.linearGradient(
                                    listOf(
                                        AppColors.purpleAccent.copy(alpha = 0.25f),
                                        AppColors.blueAccent.copy(alpha = 0.15f)
                                    )
                                )
                            else
                                Brush.linearGradient(
                                    listOf(AppColors.cardMid, AppColors.cardMid)
                                )
                        )
                        .border(
                            1.dp,
                            if (item.isActive) AppColors.purpleAccent.copy(alpha = 0.3f)
                            else AppColors.cardBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.relationEmoji,
                        fontSize = 20.sp
                    )
                }

                // Name + relation
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        color = AppColors.textHeading,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = item.relationLabel,
                        color = AppColors.textLabel,
                        fontSize = 12.sp
                    )
                }

                // Active badge
                if (item.isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.purpleAccent.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                AppColors.purpleAccent.copy(alpha = 0.3f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Активен",
                            color = AppColors.purpleAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Stats divider ─────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.divider)
            )

            Spacer(Modifier.height(14.dp))

            // ── Stats 2×2 grid ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCell(
                    label = "ДАТА РОЖДЕНИЯ",
                    value = item.birthDateText,
                    valueColor = AppColors.textBody,
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    label = "МИЛЛИАРД СЕКУНД",
                    value = item.billionDateText,
                    valueColor = AppColors.textBody,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCell(
                    label = "ПРОГРЕСС",
                    value = item.progressText,
                    valueColor = AppColors.purpleAccent,
                    modifier = Modifier.weight(1f)
                )
                StatCell(
                    label = "ОСТАЛОСЬ",
                    value = item.countdownText,
                    valueColor = AppColors.blueAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Approximate note
            if (item.hasApproximateTime) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "~ время рождения не указано",
                    color = AppColors.textSubtle,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Actions ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!item.isActive) {
                    // "Выбрать" — gradient
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.linearGradient(
                                    listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                                )
                            )
                            .clickable(onClick = onSetActive)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Выбрать",
                            color = AppColors.buttonText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (item.isEditable) {
                    Box(
                        modifier = Modifier
                            .then(if (item.isActive) Modifier.weight(1f) else Modifier)
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.cardMid)
                            .border(1.dp, AppColors.inputBorder, RoundedCornerShape(50))
                            .clickable(onClick = onEdit)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Изменить",
                            color = AppColors.textBody,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (item.isDeletable) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.dangerBackground)
                            .border(1.dp, AppColors.textDanger.copy(alpha = 0.2f), RoundedCornerShape(50))
                            .clickable(onClick = onDelete)
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Удалить",
                            color = AppColors.textDanger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ── Stat Cell ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = AppColors.textLabel,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp
        )
    }
}
