package com.example.billionseconds.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun NotificationSettingsContent(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = uiState.settings
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

            StyledSectionHeader(title = "Уведомления")

            StyledToggleRow(
                title = "Уведомления включены",
                subtitle = if (!s.notificationsEnabled)
                    "Включите, чтобы получать напоминания" else null,
                checked = s.notificationsEnabled,
                onToggle = { onAction(ProfileAction.NotificationsToggled) }
            )

            if (!s.notificationsEnabled) {
                StyledInfoText(
                    text = "Разрешите уведомления в настройках системы, чтобы они работали"
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            StyledSectionHeader(title = "Типы уведомлений")

            StyledToggleRow(
                title = "Напоминания о вехах",
                subtitle = "Получать уведомление, когда приближается миллиард секунд",
                checked = s.milestoneRemindersEnabled,
                enabled = s.notificationsEnabled,
                onToggle = { onAction(ProfileAction.MilestoneRemindersToggled) }
            )

            StyledToggleRow(
                title = "Напоминания о семье",
                subtitle = "Уведомления о вехах членов семьи",
                checked = s.familyRemindersEnabled,
                enabled = s.notificationsEnabled,
                onToggle = { onAction(ProfileAction.FamilyRemindersToggled) }
            )

            StyledToggleRow(
                title = "Периодические напоминания",
                subtitle = "Мотивационные уведомления о прогрессе",
                checked = s.reengagementEnabled,
                enabled = s.notificationsEnabled,
                onToggle = { onAction(ProfileAction.ReengagementToggled) }
            )

            Spacer(Modifier.height(16.dp))
        }

        FrostedTopBar(title = "Уведомления")
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
private fun StyledToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) AppColors.textHeading else AppColors.textHeading.copy(alpha = 0.4f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (enabled) AppColors.textBody else AppColors.textBody.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor    = AppColors.purpleAccent.copy(alpha = 0.35f),
                checkedThumbColor    = AppColors.purpleAccent,
                checkedBorderColor   = Color.Transparent,
                uncheckedTrackColor  = AppColors.stepInactive,
                uncheckedThumbColor  = AppColors.purpleAccent.copy(alpha = 0.7f),
                uncheckedBorderColor = Color.Transparent
            )
        )
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
