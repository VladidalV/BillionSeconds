package com.example.billionseconds.ui.timecapsule

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.TimeCapsuleUiState
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

private const val MAX_CAPSULES = 20

@Composable
fun CapsuleListContent(
    uiState: TimeCapsuleUiState,
    onIntent: (AppIntent) -> Unit
) {
    val totalCapsules = uiState.groups.sumOf { it.items.size }
    val canAddMore = totalCapsules < MAX_CAPSULES

    Box(
        modifier = Modifier
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

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.purpleAccent)
            }
        } else if (uiState.groups.isEmpty()) {
            EmptyCapsuleState(canAdd = canAddMore, onAdd = { onIntent(AppIntent.TimeCapsule.AddClicked) })
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 96.dp,
                    bottom = bottomPadding
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.groups.forEach { group ->
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(AppColors.purpleAccent.copy(alpha = 0.6f))
                            )
                            Text(
                                text = group.header,
                                color = AppColors.textLabel,
                                fontSize = 11.sp,
                                letterSpacing = 2.4.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    items(group.items, key = { it.id }) { item ->
                        CapsuleCard(
                            item = item,
                            onOpen   = { onIntent(AppIntent.TimeCapsule.OpenClicked(item.id)) },
                            onEdit   = { onIntent(AppIntent.TimeCapsule.EditClicked(item.id)) },
                            onDelete = { onIntent(AppIntent.TimeCapsule.DeleteClicked(item.id)) }
                        )
                    }
                }

                if (!canAddMore) {
                    item {
                        Text(
                            text = "Достигнут максимум капсул ($MAX_CAPSULES)",
                            color = AppColors.textSubtle,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            }

            // Gradient FAB
            if (canAddMore) {
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
                        .clickable { onIntent(AppIntent.TimeCapsule.AddClicked) },
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
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
            Text(
                text = "✕",
                color = AppColors.textLabel,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onIntent(AppIntent.TimeCapsule.BackClicked) }
            )
        }

        // Delete confirm dialog
        uiState.confirmDeleteId?.let { id ->
            AlertDialog(
                onDismissRequest = { onIntent(AppIntent.TimeCapsule.CancelDelete) },
                containerColor = AppColors.cardDark,
                titleContentColor = AppColors.textHeading,
                textContentColor = AppColors.textBody,
                title = { Text("Удалить капсулу?", fontWeight = FontWeight.Bold) },
                text  = { Text("Это действие нельзя отменить.", fontSize = 14.sp) },
                confirmButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.dangerBackground)
                            .clickable { onIntent(AppIntent.TimeCapsule.ConfirmDelete(id)) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Удалить", color = AppColors.textDanger, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onIntent(AppIntent.TimeCapsule.CancelDelete) }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Отмена", color = AppColors.textLabel, fontSize = 14.sp)
                    }
                }
            )
        }
    }
}

@Composable
private fun BoxScope.EmptyCapsuleState(canAdd: Boolean, onAdd: () -> Unit) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "◈", color = AppColors.purpleAccent, fontSize = 48.sp)

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Нет капсул",
            color = AppColors.textHeading,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Создайте капсулу времени с посланием,\nкоторое откроется в особый момент.",
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
                    text = "Создать капсулу",
                    color = AppColors.buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
