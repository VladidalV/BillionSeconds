package com.example.billionseconds.ui.family

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
import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun FamilyProfileForm(
    draft: ProfileFormDraft,
    isEdit: Boolean,
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
            // Name field
            FormField(label = "ИМЯ") {
                StyledTextField(
                    value = draft.name,
                    onValueChange = { onAction(FamilyAction.FormNameChanged(it)) },
                    placeholder = "Введите имя",
                    isError = draft.nameError != null
                )
                draft.nameError?.let { err ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = err,
                        color = AppColors.textError,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Relation type
            FormField(label = "ТИП РОДСТВА") {
                RelationTypeSelector(
                    selected = draft.relationType,
                    onSelect = { onAction(FamilyAction.FormRelationTypeChanged(it)) }
                )
            }

            // Custom relation name (only for OTHER)
            AnimatedVisibility(visible = draft.relationType == RelationType.OTHER) {
                FormField(label = "УТОЧНИТЬ РОДСТВО") {
                    StyledTextField(
                        value = draft.customRelationName,
                        onValueChange = { onAction(FamilyAction.FormCustomRelationChanged(it)) },
                        placeholder = "Например: Дядя Боря"
                    )
                }
            }

            // Date of birth
            FormField(label = "ДАТА РОЖДЕНИЯ") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.cardDark)
                        .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    DateInputSection(
                        year = draft.year,
                        month = draft.month,
                        day = draft.day,
                        onDateChanged = { y, m, d ->
                            onAction(FamilyAction.FormBirthDateChanged(y, m, d))
                        }
                    )
                }
                draft.dateError?.let { err ->
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = err,
                        color = AppColors.textError,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            // Unknown time toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ВРЕМЯ РОЖДЕНИЯ",
                    color = AppColors.textLabel,
                    fontSize = 12.sp,
                    letterSpacing = 2.4.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = if (draft.unknownBirthTime) "ВВЕСТИ ТОЧНОЕ ВРЕМЯ" else "НЕ ЗНАЮ ТОЧНОЕ ВРЕМЯ",
                    color = AppColors.purpleAccent.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable { onAction(FamilyAction.FormUnknownTimeToggled) }
                )
            }

            // Time input (hidden if unknown)
            AnimatedVisibility(visible = !draft.unknownBirthTime) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.cardDark)
                        .border(1.dp, AppColors.inputBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    TimeInputSection(
                        hour = draft.hour,
                        minute = draft.minute,
                        onTimeChanged = { h, m ->
                            onAction(FamilyAction.FormBirthTimeChanged(h, m))
                        }
                    )
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
                        .clickable { onAction(FamilyAction.FormCancelClicked) }
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

                // Save / Add — gradient
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        )
                        .clickable { onAction(FamilyAction.FormSaveClicked) }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEdit) "Сохранить" else "Добавить",
                        color = AppColors.buttonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
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
                text = if (isEdit) "Редактировать профиль" else "Новый профиль",
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
private fun FormField(
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
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false
) {
    val borderColor = when {
        isError -> AppColors.textError
        else    -> AppColors.inputBorder
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.cardDark)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = AppColors.textLabel,
                    fontSize = 15.sp
                )
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

// ── Relation type selector ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelationTypeSelector(
    selected: RelationType,
    onSelect: (RelationType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = "${selected.emoji} ${selected.displayLabel}",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppColors.textHeading,
                unfocusedTextColor = AppColors.textBody,
                focusedBorderColor = AppColors.purpleAccent.copy(alpha = 0.5f),
                unfocusedBorderColor = AppColors.inputBorder,
                focusedContainerColor = AppColors.cardDark,
                unfocusedContainerColor = AppColors.cardDark,
                focusedTrailingIconColor = AppColors.purpleAccent,
                unfocusedTrailingIconColor = AppColors.textLabel
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(AppColors.cardMid)
        ) {
            RelationType.entries.forEach { type ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${type.emoji} ${type.displayLabel}",
                            color = AppColors.textBody,
                            fontSize = 14.sp
                        )
                    },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = AppColors.textBody,
                        leadingIconColor = AppColors.textBody,
                        trailingIconColor = AppColors.textBody,
                        disabledTextColor = AppColors.textSubtle,
                        disabledLeadingIconColor = AppColors.textSubtle,
                        disabledTrailingIconColor = AppColors.textSubtle
                    )
                )
            }
        }
    }
}
