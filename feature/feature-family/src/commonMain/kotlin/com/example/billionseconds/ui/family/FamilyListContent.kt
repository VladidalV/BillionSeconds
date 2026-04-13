package com.example.billionseconds.ui.family

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun FamilyListContent(
    uiState: FamilyUiState,
    onAction: (FamilyAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        // Ambient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width * 0.8f, 180.dp.toPx()),
                    radius = 200.dp.toPx()
                ),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.8f, 180.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height - 200.dp.toPx()),
                    radius = 180.dp.toPx()
                ),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.2f, size.height - 200.dp.toPx())
            )
        }

        if (uiState.profiles.isEmpty()) {
            EmptyFamilyState(
                canAdd = uiState.canAddMore,
                onAdd = { onAction(FamilyAction.AddProfileClicked) }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 96.dp,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Section header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AppColors.purpleAccent.copy(alpha = 0.6f))
                        )
                        Text(
                            text = "ПРОФИЛИ",
                            color = AppColors.textLabel,
                            fontSize = 11.sp,
                            letterSpacing = 2.4.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                items(uiState.profiles, key = { it.id }) { profile ->
                    FamilyProfileCard(
                        item = profile,
                        onSetActive = { onAction(FamilyAction.SetActiveProfileClicked(profile.id)) },
                        onEdit = { onAction(FamilyAction.EditProfileClicked(profile.id)) },
                        onDelete = { onAction(FamilyAction.DeleteProfileClicked(profile.id)) }
                    )
                }

                if (uiState.maxProfilesReached) {
                    item {
                        Text(
                            text = "Достигнут максимум профилей (${uiState.profiles.size})",
                            color = AppColors.textSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Gradient FAB
            if (uiState.canAddMore) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                        .clickable { onAction(FamilyAction.AddProfileClicked) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = AppColors.buttonText,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Light,
                        lineHeight = 26.sp
                    )
                }
            }
        }

        // Frosted top bar
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
                text = "Family",
                color = AppColors.purpleAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.36).sp
            )
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.EmptyFamilyState(canAdd: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "✦", color = AppColors.purpleAccent, fontSize = 48.sp)

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Нет профилей",
            color = AppColors.textHeading,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Добавьте членов семьи, чтобы\nотслеживать их миллиарды секунд.",
            color = AppColors.textBody,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(12.dp))

        if (canAdd) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
                    .clickable(onClick = onAdd)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Добавить профиль",
                    color = AppColors.buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
