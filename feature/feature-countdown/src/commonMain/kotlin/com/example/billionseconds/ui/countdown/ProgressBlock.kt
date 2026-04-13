package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun ProgressBlock(state: CountdownUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "ПРОГРЕСС",
                color = AppColors.textLabel,
                fontSize = 11.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = state.formattedProgress,
                color = AppColors.purpleAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Track + gradient fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(AppColors.stepInactive)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(state.progressFraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
            )
        }
    }
}
