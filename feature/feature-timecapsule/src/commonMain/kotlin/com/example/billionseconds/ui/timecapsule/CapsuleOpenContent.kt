package com.example.billionseconds.ui.timecapsule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.domain.TimeCapsuleFormatter
import com.example.billionseconds.domain.model.TimeCapsule
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun CapsuleOpenContent(
    capsule: TimeCapsule,
    onAction: (TimeCapsuleAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.3f),
                    radius = 260.dp.toPx()
                ),
                radius = 260.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.3f)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 96.dp, bottom = bottomPadding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                Text(text = "✦", color = AppColors.purpleAccent, fontSize = 40.sp)

                Text(
                    text = "Капсула открыта",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        ),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.8).sp,
                        textAlign = TextAlign.Center
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = capsule.title,
                    color = AppColors.textHeading,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-0.4).sp
                )

                Text(
                    text = "Создано ${TimeCapsuleFormatter.formatCreatedDate(capsule.createdAt)}",
                    color = AppColors.textSubtle,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppColors.cardDark)
                        .border(1.dp, AppColors.cardBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        text = capsule.message,
                        color = AppColors.textBody,
                        fontSize = 16.sp,
                        lineHeight = 26.sp
                    )
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                        .clickable { /* TODO: share action */ }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Поделиться",
                        color = AppColors.buttonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAction(TimeCapsuleAction.BackClicked) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Закрыть",
                        color = AppColors.textLabel,
                        fontSize = 15.sp
                    )
                }
            }
        }

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
                text = "Time Capsule",
                color = AppColors.purpleAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.36).sp
            )
        }
    }
}
