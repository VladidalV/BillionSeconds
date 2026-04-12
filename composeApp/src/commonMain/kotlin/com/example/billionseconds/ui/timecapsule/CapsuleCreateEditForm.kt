package com.example.billionseconds.ui.timecapsule

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.CapsuleFormDraft
import com.example.billionseconds.mvi.ConditionType
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun CapsuleCreateEditForm(
    draft: CapsuleFormDraft,
    isEdit: Boolean,
    onIntent: (AppIntent) -> Unit,
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
                    center = Offset(size.width * 0.75f, 200.dp.toPx()),
                    radius = 200.dp.toPx()
                ),
                radius = 200.dp.toPx(),
                center = Offset(size.width * 0.75f, 200.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height - 180.dp.toPx()),
                    radius = 180.dp.toPx()
                ),
                radius = 180.dp.toPx(),
                center = Offset(size.width * 0.25f, size.height - 180.dp.toPx())
            )
        }

        // Scrollable form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, bottom = bottomPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title field
            CapsuleFormField(label = "ЗАГОЛОВОК") {
                CapsuleTextField(
                    value = draft.title,
                    onValueChange = { onIntent(AppIntent.TimeCapsule.FormTitleChanged(it)) },
                    placeholder = "Название капсулы",
                    isError = draft.titleError != null
                )
                draft.titleError?.let { err ->
                    Spacer(Modifier.height(4.dp))
                    Text(text = err, color = AppColors.textError, fontSize = 12.sp, lineHeight = 18.sp)
                }
                Text(
                    text = "${draft.title.length}/80",
                    color = AppColors.textSubtle,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Message field
            CapsuleFormField(label = "СООБЩЕНИЕ") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.cardDark)
                        .border(1.dp, if (draft.messageError != null) AppColors.textError else AppColors.inputBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                ) {
                    OutlinedTextField(
                        value = draft.message,
                        onValueChange = { onIntent(AppIntent.TimeCapsule.FormMessageChanged(it)) },
                        placeholder = {
                            Text(text = "Напишите послание...", color = AppColors.textLabel, fontSize = 15.sp)
                        },
                        minLines = 4,
                        maxLines = 10,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textHeading,
                            unfocusedTextColor = AppColors.textBody,
                            cursorColor = AppColors.purpleAccent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            errorBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                draft.messageError?.let { err ->
                    Spacer(Modifier.height(4.dp))
                    Text(text = err, color = AppColors.textError, fontSize = 12.sp, lineHeight = 18.sp)
                }
                Text(
                    text = "${draft.message.length}/2000",
                    color = AppColors.textSubtle,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Condition type selector
            CapsuleFormField(label = "УСЛОВИЕ ОТКРЫТИЯ") {
                CapsuleConditionSelector(
                    selected = draft.conditionType,
                    onSelect = { onIntent(AppIntent.TimeCapsule.FormConditionTypeChanged(it)) }
                )
            }

            // Date/time input
            AnimatedVisibility(visible = draft.conditionType == ConditionType.DATE) {
                CapsuleFormField(label = "ДАТА И ВРЕМЯ") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.cardDark)
                            .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        DateInputSection(
                            year = draft.selectedYear.toIntOrNull(),
                            month = draft.selectedMonth.toIntOrNull(),
                            day = draft.selectedDay.toIntOrNull(),
                            onDateChanged = { y, m, d ->
                                onIntent(AppIntent.TimeCapsule.FormDateChanged(y.toString(), m.toString(), d.toString()))
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.cardDark)
                            .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                    ) {
                        TimeInputSection(
                            hour = draft.selectedHour.toIntOrNull() ?: 12,
                            minute = draft.selectedMinute.toIntOrNull() ?: 0,
                            onTimeChanged = { h, m ->
                                onIntent(AppIntent.TimeCapsule.FormTimeChanged(h.toString().padStart(2, '0'), m.toString().padStart(2, '0')))
                            }
                        )
                    }
                    draft.conditionError?.let { err ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = err, color = AppColors.textError, fontSize = 12.sp)
                    }
                }
            }

            // Profile selector for BillionSecondsEvent condition
            AnimatedVisibility(visible = draft.conditionType == ConditionType.BILLION_SECONDS_EVENT) {
                CapsuleFormField(label = "ПРОФИЛЬ") {
                    Text(
                        text = "Выбор профиля (${draft.selectedProfileId ?: "не выбран"})",
                        color = AppColors.textSubtle,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.cardDark)
                            .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    )
                    draft.conditionError?.let { err ->
                        Spacer(Modifier.height(4.dp))
                        Text(text = err, color = AppColors.textError, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(AppColors.cardDark)
                        .border(1.dp, AppColors.inputBorder, RoundedCornerShape(50))
                        .clickable { onIntent(AppIntent.TimeCapsule.FormCancelClicked) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Отмена",
                        color = AppColors.textBody,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Save — gradient
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                        .clickable { onIntent(AppIntent.TimeCapsule.FormSaveClicked) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEdit) "Сохранить" else "Создать",
                        color = AppColors.buttonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Save as draft
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onIntent(AppIntent.TimeCapsule.FormSaveDraftClicked) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сохранить черновик",
                    color = AppColors.textLabel,
                    fontSize = 13.sp
                )
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
                text = if (isEdit) "Редактировать капсулу" else "Новая капсула",
                color = AppColors.purpleAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.36).sp
            )
        }
    }
}

// ── Form Field wrapper ────────────────────────────────────────────────────────

@Composable
private fun CapsuleFormField(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            color = AppColors.textLabel,
            fontSize = 12.sp,
            letterSpacing = 2.4.sp,
            fontWeight = FontWeight.Normal
        )
        content()
    }
}

// ── Styled text field ─────────────────────────────────────────────────────────

@Composable
private fun CapsuleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.cardDark)
            .border(1.dp, if (isError) AppColors.textError else AppColors.inputBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = AppColors.textLabel, fontSize = 15.sp)
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textHeading,
                unfocusedTextColor = AppColors.textBody,
                cursorColor = AppColors.purpleAccent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
