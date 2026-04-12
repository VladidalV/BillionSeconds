package com.example.billionseconds.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonSheet(
    feature: String,
    onDismiss: () -> Unit
) {
    val title = when (feature) {
        "create_video"  -> "Создать видео"
        "write_letter"  -> "Написать письмо себе"
        "add_family"    -> "Добавить семью"
        "premium"       -> "Подписка"
        "time_capsule"  -> "Time Capsule"
        "help"          -> "Помощь"
        else            -> "Скоро"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.backgroundScreen,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppColors.cardDark)
                    .border(1.dp, AppColors.cardBorder, RoundedCornerShape(24.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✦",
                    color = AppColors.purpleAccent,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = title,
                    color = AppColors.textHeading,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Эта функция уже в разработке.\nСкоро она появится в приложении.",
                    color = AppColors.textBody,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                        .clickable { onDismiss() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Понятно",
                        color = AppColors.buttonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}