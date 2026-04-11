package com.example.billionseconds.ui.event

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val CELEBRATION_DURATION_MS = 4000L

/**
 * Overlay с celebration-анимацией для first-time event.
 * Простая MVP-версия: пульсирующий текст + автозавершение через N секунд.
 * В будущем заменить на confetti / Lottie анимацию.
 */
@Composable
fun CelebrationOverlay(
    onSkip: () -> Unit,
    onCompleted: () -> Unit
) {
    var timeLeft by remember { mutableStateOf(CELEBRATION_DURATION_MS) }

    // Автоматическое завершение
    LaunchedEffect(Unit) {
        delay(CELEBRATION_DURATION_MS)
        onCompleted()
    }

    // Пульсирующая анимация emoji
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue  = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text      = "\uD83C\uDF89",
                fontSize  = 80.sp,
                modifier  = Modifier.scale(scale)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text      = "1 000 000 000",
                style     = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                color     = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text  = "секунд жизни",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(48.dp))
            TextButton(
                onClick = onSkip,
                colors  = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
            ) {
                Text("Пропустить")
            }
        }
    }
}
