package com.example.billionseconds.ui.event

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors
import kotlinx.coroutines.delay

private const val CELEBRATION_DURATION_MS = 4000L

@Composable
fun CelebrationOverlay(
    onSkip: () -> Unit,
    onCompleted: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(CELEBRATION_DURATION_MS)
        onCompleted()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "celebration_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.backgroundDark.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "\uD83C\uDF89",
                fontSize = 72.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "1 000 000 000",
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                    ),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp,
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "секунд жизни",
                color = AppColors.textBody,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "A milestone written in the stars.",
                color = AppColors.textSubtle,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(56.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.cardMid)
                    .clickable(onClick = onSkip)
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Пропустить",
                    color = AppColors.textLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
