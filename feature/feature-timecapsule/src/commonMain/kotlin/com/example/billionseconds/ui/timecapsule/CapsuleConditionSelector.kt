package com.example.billionseconds.ui.timecapsule

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
fun CapsuleConditionSelector(
    selected: ConditionType,
    onSelect: (ConditionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.cardDark)
            .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ConditionType.entries.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Brush.linearGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        ) else Brush.linearGradient(
                            listOf(AppColors.cardDark, AppColors.cardDark)
                        )
                    )
                    .clickable { onSelect(type) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (type) {
                        ConditionType.DATE -> "По дате"
                        ConditionType.BILLION_SECONDS_EVENT -> "Миллиард секунд"
                    },
                    color = if (isSelected) AppColors.buttonText else AppColors.textSubtle,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
