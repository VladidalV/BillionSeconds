package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.domain.model.EventStatus
import com.example.billionseconds.ui.theme.AppColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CountdownBlock(state: CountdownUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Circular Dial ─────────────────────────────────────────────────────
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val strokeWidth = 8.dp.toPx()
                val trackRadius = 128.dp.toPx()

                // Outer orbital decoration ring
                drawCircle(
                    color = AppColors.ringDecoration,
                    radius = 144.dp.toPx(),
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                // Background track
                drawCircle(
                    color = AppColors.stepInactive,
                    radius = trackRadius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Progress arc
                val clamped = state.progressFraction.coerceIn(0f, 1f)
                if (clamped > 0f) {
                    val sweepAngle = 360f * clamped
                    drawArc(
                        color = AppColors.purpleAccent,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - trackRadius, center.y - trackRadius),
                        size = Size(trackRadius * 2, trackRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Glowing endpoint dot
                    val endRad = (-90f + sweepAngle) * (PI / 180.0).toFloat()
                    val dotCenter = Offset(
                        center.x + trackRadius * cos(endRad),
                        center.y + trackRadius * sin(endRad)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppColors.purpleAccent,
                                AppColors.purpleAccent.copy(alpha = 0f)
                            ),
                            center = dotCenter,
                            radius = strokeWidth * 1.5f
                        ),
                        radius = strokeWidth * 1.5f,
                        center = dotCenter
                    )
                }
            }

            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "TEMPORAL TARGET",
                    color = AppColors.purpleAccent.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                if (state.eventStatus == EventStatus.Reached) {
                    Text(text = "\uD83C\uDF89", fontSize = 40.sp, textAlign = TextAlign.Center)
                } else {
                    Text(
                        text = state.formattedMilestoneDate,
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            ),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1.0).sp,
                            textAlign = TextAlign.Center
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "CURRENT VELOCITY",
                    color = AppColors.textBody.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = state.formattedProgress,
                    color = AppColors.blueAccent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // ── Countdown Card ────────────────────────────────────────────────────
        if (state.eventStatus != EventStatus.Reached) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.cardDark)
                    .border(1.dp, AppColors.inputBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "ДО МИЛЛИАРДА СЕКУНД",
                        color = AppColors.textLabel,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = state.formattedCountdown,
                        color = AppColors.textHeading,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp,
                        textAlign = TextAlign.Center
                    )
                    if (state.isUnknownBirthTime) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "* время рождения не указано, отсчёт с полудня",
                            color = AppColors.textSubtle,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
