package com.example.billionseconds.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun OnboardingInputScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
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
                    center = Offset(-96.dp.toPx() + 192.dp.toPx(), 177.dp.toPx() + 192.dp.toPx()),
                    radius = 192.dp.toPx()
                ),
                radius = 192.dp.toPx(),
                center = Offset(-96.dp.toPx() + 192.dp.toPx(), 177.dp.toPx() + 192.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width + 96.dp.toPx() - 192.dp.toPx(), size.height - 269.dp.toPx()),
                    radius = 192.dp.toPx()
                ),
                radius = 192.dp.toPx(),
                center = Offset(size.width + 96.dp.toPx() - 192.dp.toPx(), size.height - 269.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, bottom = 48.dp)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(currentStep = 1, totalSteps = 3)
            HeroSection()
            FormArea(uiState = uiState, onAction = onAction)
        }

        TopBar()
    }
}

@Composable
private fun BoxScope.TopBar() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.headerBackground)
            .padding(horizontal = 32.dp),
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
            text = "Billion Seconds",
            color = AppColors.purpleAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.36).sp
        )
    }
}

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "The Origin Moment",
            color = AppColors.textHeading,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.72).sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
        Text(
            text = "Precise temporal data allows us to calculate your cosmic milestones with absolute accuracy.",
            color = AppColors.textBody,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun FormArea(uiState: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        DateField(uiState = uiState, onAction = onAction)
        TimeField(uiState = uiState, onAction = onAction)
        CtaSection(uiState = uiState, onAction = onAction)
    }
}

@Composable
private fun DateField(uiState: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "DATE OF BIRTH",
            color = AppColors.textLabel,
            fontSize = 12.sp,
            letterSpacing = 2.4.sp,
            fontWeight = FontWeight.Normal
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.cardDark)
                .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            DateInputSection(
                year = uiState.year,
                month = uiState.month,
                day = uiState.day,
                onDateChanged = { y, m, d -> onAction(OnboardingAction.DateChanged(y, m, d)) }
            )
        }

        uiState.error?.let { err ->
            Text(
                text = err,
                color = AppColors.textError,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun TimeField(uiState: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TIME OF BIRTH",
                color = AppColors.textLabel,
                fontSize = 12.sp,
                letterSpacing = 2.4.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = if (uiState.unknownTime) "ENTER EXACT TIME" else "I DON'T KNOW MY EXACT TIME",
                color = AppColors.purpleAccent.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.clickable { onAction(OnboardingAction.UnknownTimeToggled) }
            )
        }

        AnimatedVisibility(visible = !uiState.unknownTime) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.cardDark)
                    .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                TimeInputSection(
                    hour = uiState.hour,
                    minute = uiState.minute,
                    onTimeChanged = { h, m -> onAction(OnboardingAction.TimeChanged(h, m)) }
                )
            }
        }
    }
}

@Composable
private fun CtaSection(uiState: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                    )
                )
                .clickable { onAction(OnboardingAction.CalculateClicked) }
                .padding(vertical = 20.dp, horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calculate Milestones",
                    color = AppColors.buttonText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "→",
                    color = AppColors.buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.Center) {
            Text(
                text = "By continuing, you agree to our ",
                color = AppColors.textLabel,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
            Text(
                text = "Temporal Privacy Policy",
                color = AppColors.textBody,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                textDecoration = TextDecoration.Underline
            )
            Text(
                text = ".",
                color = AppColors.textLabel,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }
    }
}
