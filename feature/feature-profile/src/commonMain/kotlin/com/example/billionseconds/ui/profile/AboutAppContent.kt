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
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun AboutAppContent(
    uiState: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
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

            StyledSectionHeader(title = "О приложении")

            StyledVersionRow(
                label = "Версия",
                value = uiState.appVersion
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            StyledSectionHeader(title = "Юридическая информация")

            StyledNavigationRow(
                emoji = "🔒",
                title = "Политика конфиденциальности",
                onClick = { onAction(ProfileAction.LegalLinkClicked(LegalLinkType.PrivacyPolicy)) }
            )

            StyledNavigationRow(
                emoji = "📋",
                title = "Условия использования",
                onClick = { onAction(ProfileAction.LegalLinkClicked(LegalLinkType.TermsOfUse)) }
            )

            Spacer(Modifier.height(16.dp))
        }

        FrostedTopBar(title = "О приложении")
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
private fun StyledVersionRow(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.cardDark)
            .border(1.dp, AppColors.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = AppColors.textHeading,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = AppColors.textBody,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun StyledNavigationRow(
    emoji: String,
    title: String,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.purpleAccent.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 16.sp)
            }

            Text(
                text = title,
                color = AppColors.textHeading,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "›",
                color = AppColors.textLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light
            )
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
