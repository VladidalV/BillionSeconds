package com.example.billionseconds.ui.family

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ProfileFormDraft
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection

@Composable
fun FamilyProfileForm(
    draft: ProfileFormDraft,
    isEdit: Boolean,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEdit) "Редактировать профиль" else "Новый профиль",
            style = MaterialTheme.typography.headlineSmall
        )

        // Name field
        OutlinedTextField(
            value = draft.name,
            onValueChange = { onIntent(AppIntent.FormNameChanged(it)) },
            label = { Text("Имя") },
            isError = draft.nameError != null,
            supportingText = draft.nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // RelationType selector
        RelationTypeSelector(
            selected = draft.relationType,
            onSelect = { onIntent(AppIntent.FormRelationTypeChanged(it)) }
        )

        // Custom relation name (only for OTHER)
        AnimatedVisibility(visible = draft.relationType == RelationType.OTHER) {
            OutlinedTextField(
                value = draft.customRelationName,
                onValueChange = { onIntent(AppIntent.FormCustomRelationChanged(it)) },
                label = { Text("Уточнить родство (например, Дядя Боря)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Date section
        Text(
            text = "Дата рождения",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DateInputSection(
            year = draft.year,
            month = draft.month,
            day = draft.day,
            onDateChanged = { y, m, d ->
                onIntent(AppIntent.FormBirthDateChanged(y, m, d))
            }
        )
        draft.dateError?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Unknown time toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Время рождения неизвестно",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = draft.unknownBirthTime,
                onCheckedChange = { onIntent(AppIntent.FormUnknownTimeToggled) }
            )
        }

        // Time section (hidden if unknownBirthTime)
        AnimatedVisibility(visible = !draft.unknownBirthTime) {
            Column {
                Text(
                    text = "Время рождения (необязательно)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                TimeInputSection(
                    hour = draft.hour,
                    minute = draft.minute,
                    onTimeChanged = { h, m ->
                        onIntent(AppIntent.FormBirthTimeChanged(h, m))
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
            OutlinedButton(
                onClick = { onIntent(AppIntent.FormCancelClicked) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Отмена")
            }
            Button(
                onClick = { onIntent(AppIntent.FormSaveClicked) },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isEdit) "Сохранить" else "Добавить")
            }
        }
    }
}

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
            label = { Text("Тип родства") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            RelationType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text("${type.emoji} ${type.displayLabel}") },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    }
                )
            }
        }
    }
}
