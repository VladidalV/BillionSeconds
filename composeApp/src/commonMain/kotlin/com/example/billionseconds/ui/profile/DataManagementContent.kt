package com.example.billionseconds.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ProfileConfirmDialog
import com.example.billionseconds.mvi.ProfileUiState
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun DataManagementContent(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width * 0.75f, 160.dp.toPx()),
                    radius = 200.dp.toPx()
                ),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.75f, 160.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height - 180.dp.toPx()),
                    radius = 180.dp.toPx()
                ),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.2f, size.height - 180.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding)
        ) {
            Spacer(Modifier.height(96.dp))

            StyledSectionHeader(title = "Управление данными")

            StyledInfoText(
                text = "Действия в этом разделе необратимы. Будьте внимательны."
            )

            Spacer(Modifier.height(8.dp))

            StyledDebugActionRow(
                title = "🧪 Открыть Event Screen",
                subtitle = "Сброс истории + открытие экрана (требует дату рождения до 1993 г.)",
                onClick = { onIntent(AppIntent.DebugOpenEventScreen) }
            )

            Spacer(Modifier.height(8.dp))

            StyledDangerousActionRow(
                title = "Сбросить онбординг",
                subtitle = "Удалить все данные и вернуться к началу",
                onClick = { onIntent(AppIntent.ResetOnboardingClicked) }
            )

            StyledDangerousActionRow(
                title = "Удалить все данные",
                subtitle = "Полная очистка: профили, настройки, данные",
                onClick = { onIntent(AppIntent.ClearAllDataClicked) }
            )

            Spacer(Modifier.height(16.dp))
        }

        FrostedTopBar(title = "Данные")

        uiState.confirmDialog?.let { dialog ->
            val (title, body) = when (dialog) {
                ProfileConfirmDialog.ResetOnboarding ->
                    "Сбросить онбординг?" to
                    "Все данные будут удалены. Вы начнёте приложение заново. Это действие необратимо."
                ProfileConfirmDialog.ClearAllData ->
                    "Удалить все данные?" to
                    "Все профили, настройки и локальные данные будут удалены навсегда. Это действие необратимо."
            }
            AlertDialog(
                onDismissRequest = { if (!uiState.isActionInProgress) onIntent(AppIntent.DismissConfirmDialog) },
                containerColor = AppColors.cardDark,
                titleContentColor = AppColors.textHeading,
                textContentColor = AppColors.textBody,
                title = {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = body,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                },
                confirmButton = {
                    if (uiState.isActionInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = AppColors.purpleAccent)
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(AppColors.dangerBackground)
                                .border(1.dp, AppColors.textDanger.copy(alpha = 0.2f), RoundedCornerShape(50))
                                .clickable { onIntent(AppIntent.ConfirmDangerousAction) }
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Удалить",
                                color = AppColors.textDanger,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                dismissButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onIntent(AppIntent.DismissConfirmDialog) }
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
    }
}

@Composable
private fun StyledSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = AppColors.textLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun StyledInfoText(text: String) {
    Text(
        text = text,
        color = AppColors.textSubtle,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun StyledDebugActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.cardDark)
            .border(1.dp, AppColors.cardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AppColors.blueAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
                Text(
                    text = subtitle,
                    color = AppColors.textBody,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun StyledDangerousActionRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.dangerBackground)
            .border(1.dp, AppColors.textDanger.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "⚠️", color = AppColors.textDanger, fontSize = 16.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = AppColors.textDanger,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
                Text(
                    text = subtitle,
                    color = AppColors.textBody,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun BoxScope.FrostedTopBar(title: String) {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.headerBackground)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✦",
                color = AppColors.purpleAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = AppColors.purpleAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.36).sp
            )
        }
        Text(
            text = "⊙",
            color = AppColors.textLabel,
            fontSize = 18.sp
        )
    }
}