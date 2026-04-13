package com.example.billionseconds.ui.timecapsule

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
import com.example.billionseconds.data.model.CapsuleUiItem
import com.example.billionseconds.domain.CapsuleStatus
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun CapsuleCard(
    item: CapsuleUiItem,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isAvailable = item.status is CapsuleStatus.Available
    val isOpened    = item.status is CapsuleStatus.Opened
    val isDraft     = item.status is CapsuleStatus.Draft
    val isInvalid   = item.status is CapsuleStatus.Invalid

    val borderColor = when (item.status) {
        is CapsuleStatus.Available -> AppColors.purpleAccent.copy(alpha = 0.6f)
        is CapsuleStatus.Opened    -> AppColors.blueAccent.copy(alpha = 0.15f)
        is CapsuleStatus.Draft     -> AppColors.inputBorder
        is CapsuleStatus.Invalid   -> AppColors.textDanger.copy(alpha = 0.2f)
        is CapsuleStatus.Locked    -> AppColors.cardBorder
    }

    val icon = when (item.status) {
        is CapsuleStatus.Available -> "✦"
        is CapsuleStatus.Opened    -> "◎"
        is CapsuleStatus.Draft     -> "○"
        is CapsuleStatus.Invalid   -> "◈"
        is CapsuleStatus.Locked    -> "◈"
    }

    val iconColor = when (item.status) {
        is CapsuleStatus.Available -> AppColors.purpleAccent
        is CapsuleStatus.Opened    -> AppColors.blueAccent
        is CapsuleStatus.Draft     -> AppColors.textSubtle
        is CapsuleStatus.Invalid   -> AppColors.textDanger
        is CapsuleStatus.Locked    -> AppColors.textSubtle
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppColors.cardDark)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .then(if (isAvailable || isOpened) Modifier.clickable(onClick = onOpen) else Modifier)
    ) {
        // Left accent bar for available
        if (isAvailable) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, color = iconColor, fontSize = 14.sp)
                Text(
                    text = item.title,
                    color = if (isOpened || isDraft || isInvalid) AppColors.textSubtle else AppColors.textHeading,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(status = item.status)
            }

            Text(
                text = item.unlockLabel,
                color = AppColors.textLabel,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            if (item.recipientName != null) {
                Text(
                    text = "Для: ${item.recipientName}",
                    color = AppColors.textSubtle,
                    fontSize = 11.sp
                )
            }

            Text(
                text = item.createdLabel,
                color = AppColors.textSubtle,
                fontSize = 11.sp
            )

            val lockedStatus = item.status as? CapsuleStatus.Locked
            if (lockedStatus != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Осталось: ${lockedStatus.remainingLabel}",
                    color = AppColors.blueAccent.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isInvalid) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Профиль удалён — условие недоступно",
                    color = AppColors.textDanger,
                    fontSize = 11.sp
                )
            }

            if (isDraft || item.status is CapsuleStatus.Locked) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.cardMid)
                            .clickable(onClick = onEdit)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Изменить",
                            color = AppColors.textBody,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.dangerBackground)
                            .clickable(onClick = onDelete)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Удалить",
                            color = AppColors.textDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isAvailable) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                        .clickable(onClick = onOpen)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Открыть",
                        color = AppColors.buttonText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: CapsuleStatus) {
    val (label, textColor, bgColor) = when (status) {
        is CapsuleStatus.Available -> Triple("ДОСТУПНО",  AppColors.buttonText,  AppColors.purpleAccent.copy(alpha = 0.3f))
        is CapsuleStatus.Locked    -> Triple("ЗАПЕРТО",   AppColors.textSubtle,  AppColors.stepInactive)
        is CapsuleStatus.Opened    -> Triple("ОТКРЫТО",   AppColors.blueAccent,  AppColors.blueAccent.copy(alpha = 0.1f))
        is CapsuleStatus.Draft     -> Triple("ЧЕРНОВИК",  AppColors.textLabel,   AppColors.cardMid)
        is CapsuleStatus.Invalid   -> Triple("ОШИБКА",    AppColors.textDanger,  AppColors.dangerBackground)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
