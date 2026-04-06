package com.example.billionseconds.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

private val MONTHS = listOf(
    "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
    "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
)

private val DAYS = (1..31).toList()
private val HOURS = (0..23).toList()
private val MINUTES = (0..59).toList()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun DateInputSection(
    year: Int?,
    month: Int?,
    day: Int?,
    onDateChanged: (year: Int, month: Int, day: Int) -> Unit,
    modifier: Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val label = if (year != null && month != null && day != null) {
        "${day.toString().padStart(2, '0')}.${month.toString().padStart(2, '0')}.$year"
    } else {
        "Выбрать дату рождения"
    }

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("📅  $label")
    }

    if (showPicker) {
        var selectedDay by remember { mutableStateOf(day ?: 1) }
        var selectedMonth by remember { mutableStateOf(month ?: 1) }
        var yearText by remember { mutableStateOf(year?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val y = yearText.toIntOrNull()
                    if (y != null && y >= 1000) {
                        onDateChanged(y, selectedMonth, selectedDay)
                        showPicker = false
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            },
            title = { Text("Дата рождения") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // День
                    DropdownSelector(
                        label = "День",
                        selected = selectedDay.toString(),
                        options = DAYS.map { it.toString() },
                        onSelected = { selectedDay = it.toInt() }
                    )

                    // Месяц
                    DropdownSelector(
                        label = "Месяц",
                        selected = MONTHS[selectedMonth - 1],
                        options = MONTHS,
                        onSelected = { selectedMonth = MONTHS.indexOf(it) + 1 }
                    )

                    // Год
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { v -> if (v.length <= 4) yearText = v },
                        label = { Text("Год") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun TimeInputSection(
    hour: Int,
    minute: Int,
    onTimeChanged: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val label = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier.fillMaxWidth()
    ) {
        Text("🕐  $label")
    }

    if (showPicker) {
        var selectedHour by remember { mutableStateOf(hour) }
        var selectedMinute by remember { mutableStateOf(minute) }

        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeChanged(selectedHour, selectedMinute)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            },
            title = { Text("Время рождения") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownSelector(
                            label = "Час",
                            selected = selectedHour.toString().padStart(2, '0'),
                            options = HOURS.map { it.toString().padStart(2, '0') },
                            onSelected = { selectedHour = it.trimStart('0').toIntOrNull() ?: 0 }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DropdownSelector(
                            label = "Минута",
                            selected = selectedMinute.toString().padStart(2, '0'),
                            options = MINUTES.map { it.toString().padStart(2, '0') },
                            onSelected = { selectedMinute = it.trimStart('0').toIntOrNull() ?: 0 }
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 250.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
