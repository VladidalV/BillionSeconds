package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.shared.ComingSoonSheet
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun CountdownScreen(
    uiState: CountdownUiState,
    onAction: (CountdownAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onAction(CountdownAction.ScreenStarted)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundDark)
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.purpleAccent)
                }
            }

            uiState.error == CountdownError.NoProfileData ||
            uiState.error == CountdownError.CorruptedData -> {
                ErrorContent(onAction = onAction)
            }

            else -> {
                CountdownContent(uiState = uiState, onAction = onAction)
            }
        }
    }
}

@Composable
private fun CountdownContent(
    uiState: CountdownUiState,
    onAction: (CountdownAction) -> Unit
) {
    var comingSoonFeature by remember { mutableStateOf<String?>(null) }

    comingSoonFeature?.let { feature ->
        ComingSoonSheet(feature = feature, onDismiss = { comingSoonFeature = null })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.28f),
                    radius = 280.dp.toPx()
                ),
                radius = 280.dp.toPx(),
                center = Offset(size.width * 0.5f, size.height * 0.28f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.72f),
                    radius = 200.dp.toPx()
                ),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.15f, size.height * 0.72f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 24.dp, bottom = bottomPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            EventBlock(state = uiState)
            CountdownBlock(state = uiState)
            ProgressBlock(state = uiState)
            ActionsBlock(onAction = onAction)
        }

    }
}

@Composable
private fun ErrorContent(onAction: (CountdownAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "◈", color = AppColors.textSubtle, fontSize = 40.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Что-то пошло не так",
            color = AppColors.textHeading,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.linearGradient(
                        listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                    )
                )
                .clickable { onAction(CountdownAction.ClearClicked) }
                .padding(horizontal = 32.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Начать заново",
                color = AppColors.buttonText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
