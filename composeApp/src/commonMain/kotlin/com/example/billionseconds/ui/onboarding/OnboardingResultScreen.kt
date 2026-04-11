package com.example.billionseconds.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.ui.theme.AppColors
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OnboardingResultScreen(
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
                    center = Offset(-39.dp.toPx() + 78.dp.toPx(), -88.dp.toPx() + 177.dp.toPx()),
                    radius = 177.dp.toPx()
                ),
                radius = 177.dp.toPx(),
                center = Offset(-39.dp.toPx() + 78.dp.toPx(), -88.dp.toPx() + 177.dp.toPx())
            )
            // Blue glow — bottom-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width + 39.dp.toPx() - 78.dp.toPx(), size.height + 88.dp.toPx() - 177.dp.toPx()),
                    radius = 177.dp.toPx()
                ),
                radius = 177.dp.toPx(),
                center = Offset(size.width + 39.dp.toPx() - 78.dp.toPx(), size.height + 88.dp.toPx() - 177.dp.toPx())
            )
        }

        // Scrollable main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, bottom = 48.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp)
        ) {
            // Step 3 of 3
            StepIndicator(currentStep = 2, totalSteps = 3)

            HeaderSection(isMilestoneReached = state.isMilestoneReached)

            CircularRevealVisual(
                progress = state.progressPercent,
                state = state
            )

            CtaSection(onIntent = onIntent)
        }

        // Frosted glass top bar
        ResultTopBar()
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.ResultTopBar() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.headerBackground)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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

// ── Header Section ────────────────────────────────────────────────────────────

@Composable
private fun HeaderSection(isMilestoneReached: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = if (isMilestoneReached) "Your billionth second has arrived!" else "Your billionth second arrives...",
            color = AppColors.purpleAccent,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.8).sp,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )
        Text(
            text = if (isMilestoneReached)
                "A milestone written in the stars. You have reached this celestial alignment."
            else
                "A milestone written in the stars. Every moment has led to this celestial alignment.",
            color = AppColors.textBody,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

// ── Circular Dial Visual ──────────────────────────────────────────────────────

@Composable
private fun CircularRevealVisual(
    progress: Float,
    state: AppState
) {
    val milestoneDate = state.milestoneInstant
        ?.toLocalDateTime(TimeZone.currentSystemDefault())

    Box(
        modifier = Modifier.size(340.dp),
        contentAlignment = Alignment.Center
    ) {
        // Rings drawn on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val strokeWidth = 8.dp.toPx()
            val trackRadius = 148.dp.toPx()

            // Outer orbital decoration ring
            drawCircle(
                color = AppColors.ringDecoration,
                radius = 166.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Background track ring
            drawCircle(
                color = AppColors.stepInactive,
                radius = trackRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            val clampedProgress = progress.coerceIn(0f, 1f)
            if (clampedProgress > 0f) {
                val sweepAngle = 360f * clampedProgress
                val arcTopLeft = Offset(
                    center.x - trackRadius,
                    center.y - trackRadius
                )
                drawArc(
                    color = AppColors.purpleAccent,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = Size(trackRadius * 2, trackRadius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Glowing endpoint dot
                val endRad = (-90f + sweepAngle) * (PI / 180.0).toFloat()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AppColors.purpleAccent, AppColors.purpleAccent.copy(alpha = 0f)),
                        center = Offset(
                            center.x + trackRadius * cos(endRad),
                            center.y + trackRadius * sin(endRad)
                        ),
                        radius = strokeWidth * 1.5f
                    ),
                    radius = strokeWidth * 1.5f,
                    center = Offset(
                        center.x + trackRadius * cos(endRad),
                        center.y + trackRadius * sin(endRad)
                    )
                )
            }
        }

        // Center text content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "TEMPORAL TARGET",
                color = AppColors.purpleAccent.copy(alpha = 0.6f),
                fontSize = 10.sp,
                letterSpacing = 4.8.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            if (milestoneDate != null) {
                // Month + Day with gradient
                val monthAbbr = milestoneDate.month.name.take(3)
                val day = milestoneDate.dayOfMonth
                Text(
                    text = "$monthAbbr $day",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        ),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-2.4).sp,
                        textAlign = TextAlign.Center
                    )
                )
                // Year
                Text(
                    text = milestoneDate.year.toString(),
                    color = AppColors.textHeading,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 3.6.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                // Milestone already passed or no data
                Text(
                    text = "🎉",
                    fontSize = 44.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "CURRENT LIFE VELOCITY",
                color = AppColors.textBody.copy(alpha = 0.6f),
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = formatPercent(progress),
                color = AppColors.blueAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatPercent(value: Float): String {
    val clamped = (value * 100f).coerceIn(0f, 100f)
    val intPart = clamped.toInt()
    val decPart = ((clamped - intPart) * 10).toInt()
    return "$intPart.$decPart%"
}

// ── CTA Section ───────────────────────────────────────────────────────────────

@Composable
private fun CtaSection(onIntent: (AppIntent) -> Unit) {
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
                .clickable { onIntent(AppIntent.OnboardingContinueClicked) }
                .padding(vertical = 16.dp, horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter the Journey",
                    color = AppColors.buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    text = "→",
                    color = AppColors.buttonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Footer caption
        Text(
            text = "CALIBRATING YOUR TEMPORAL ANCHOR",
            color = AppColors.textBody.copy(alpha = 0.4f),
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}
