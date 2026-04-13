package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun ActionsBlock(onAction: (CountdownAction) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                .clickable { onAction(CountdownAction.ShareClicked) }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Поделиться",
                color = AppColors.buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondaryButton(
                text = "Видео",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CountdownAction.CreateVideoClicked) }
            )
            SecondaryButton(
                text = "Письмо",
                modifier = Modifier.weight(1f),
                onClick = { onAction(CountdownAction.WriteLetterClicked) }
            )
        }

        SecondaryButton(
            text = "Добавить семью",
            modifier = Modifier.fillMaxWidth(),
            onClick = { onAction(CountdownAction.AddFamilyClicked) }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAction(CountdownAction.ClearClicked) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Изменить дату рождения",
                color = AppColors.textLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(AppColors.cardDark)
            .border(1.dp, AppColors.inputBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = AppColors.textBody,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
