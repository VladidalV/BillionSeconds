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
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun OnboardingInputScreen(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        // Decorative ambient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Purple glow — top-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(-96.dp.toPx() + 192.dp.toPx(), 177.dp.toPx() + 192.dp.toPx()),
                    radius = 192.dp.toPx()
                ),
                radius = 192.dp.toPx(),
                center = Offset(-96.dp.toPx() + 192.dp.toPx(), 177.dp.toPx() + 192.dp.toPx())
            )
            // Blue glow — bottom-right
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

        // Scrollable content — padded below the header
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
            FormArea(state = state, onIntent = onIntent)
        }

        // Frosted glass top bar (absolute, drawn on top)
        TopBar()
    }
}

// ── Top App Bar ───────────────────────────────────────────────────────────────

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
        // Star / sparkle icon placeholder
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

// ── Step Indicator ────────────────────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val width = if (isActive) 48.dp else 32.dp
            if (isActive) {
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(width)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(AppColors.stepInactive)
                )
            }
        }
    }
}

// ── Hero Section ──────────────────────────────────────────────────────────────

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

// ── Form Area ─────────────────────────────────────────────────────────────────

@Composable
private fun FormArea(state: AppState, onIntent: (AppIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        // Date of birth
        DateField(state = state, onIntent = onIntent)

        // Time of birth
        TimeField(state = state, onIntent = onIntent)

        // CTA + footer
        CtaSection(state = state, onIntent = onIntent)
    }
}

// ── Date Field ────────────────────────────────────────────────────────────────

@Composable
private fun DateField(state: AppState, onIntent: (AppIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Label
        Text(
            text = "DATE OF BIRTH",
            color = AppColors.textLabel,
            fontSize = 12.sp,
            letterSpacing = 2.4.sp,
            fontWeight = FontWeight.Normal
        )

        // Input wrapper
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.cardDark)
                .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            DateInputSection(
                year = state.year,
                month = state.month,
                day = state.day,
                onDateChanged = { y, m, d -> onIntent(AppIntent.OnboardingDateChanged(y, m, d)) }
            )
        }

        // Validation error
        state.error?.let { err ->
            Text(
                text = err,
                color = AppColors.textError,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

// ── Time Field ────────────────────────────────────────────────────────────────

@Composable
private fun TimeField(state: AppState, onIntent: (AppIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Label row: "TIME OF BIRTH" + toggle text
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
                text = if (state.unknownTime) "ENTER EXACT TIME" else "I DON'T KNOW MY EXACT TIME",
                color = AppColors.purpleAccent.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.clickable { onIntent(AppIntent.UnknownTimeToggled) }
            )
        }

        // Input — hidden when time is unknown
        AnimatedVisibility(visible = !state.unknownTime) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.cardDark)
                    .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                TimeInputSection(
                    hour = state.hour,
                    minute = state.minute,
                    onTimeChanged = { h, m -> onIntent(AppIntent.OnboardingTimeChanged(h, m)) }
                )
            }
        }
    }
}

// ── CTA Section ───────────────────────────────────────────────────────────────

@Composable
private fun CtaSection(state: AppState, onIntent: (AppIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Gradient button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                    )
                )
                .clickable { onIntent(AppIntent.OnboardingCalculateClicked) }
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

        // Footer disclaimer
        Row(
            horizontalArrangement = Arrangement.Center
        ) {
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
