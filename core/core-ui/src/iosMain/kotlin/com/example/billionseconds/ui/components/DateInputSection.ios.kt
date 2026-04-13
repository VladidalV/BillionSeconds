@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.example.billionseconds.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import platform.Foundation.*
import platform.UIKit.*

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
        val datePicker = remember {
            UIDatePicker().apply {
                datePickerMode = UIDatePickerMode.UIDatePickerModeDate
                preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
                maximumDate = NSDate()
                date = nsDateFrom(year, month, day)
            }
        }

        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val (y, m, d) = extractDate(datePicker.date)
                    onDateChanged(y, m, d)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            },
            text = {
                UIKitView(
                    factory = { datePicker },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        )
    }
}

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
        val timePicker = remember {
            UIDatePicker().apply {
                datePickerMode = UIDatePickerMode.UIDatePickerModeTime
                preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
                date = nsTimeFrom(hour, minute)
            }
        }

        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val (h, m) = extractTime(timePicker.date)
                    onTimeChanged(h, m)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            },
            text = {
                UIKitView(
                    factory = { timePicker },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        )
    }
}

private fun nsDateFrom(year: Int?, month: Int?, day: Int?): NSDate {
    val cal = NSCalendar.currentCalendar
    val currentYear = cal.components(NSCalendarUnitYear, NSDate()).year
    val comps = NSDateComponents().apply {
        this.year   = year?.toLong()  ?: (currentYear - 30L)
        this.month  = month?.toLong() ?: 6L
        this.day    = day?.toLong()   ?: 15L
        this.hour   = 0L
        this.minute = 0L
        this.second = 0L
    }
    return cal.dateFromComponents(comps) ?: NSDate()
}

private fun nsTimeFrom(hour: Int, minute: Int): NSDate {
    val cal = NSCalendar.currentCalendar
    val today = cal.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        NSDate()
    )
    val comps = NSDateComponents().apply {
        this.year   = today.year
        this.month  = today.month
        this.day    = today.day
        this.hour   = hour.toLong()
        this.minute = minute.toLong()
        this.second = 0L
    }
    return cal.dateFromComponents(comps) ?: NSDate()
}

private fun extractDate(date: NSDate): Triple<Int, Int, Int> {
    val cal = NSCalendar.currentCalendar
    val c = cal.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        date
    )
    return Triple(c.year.toInt(), c.month.toInt(), c.day.toInt())
}

private fun extractTime(date: NSDate): Pair<Int, Int> {
    val cal = NSCalendar.currentCalendar
    val c = cal.components(NSCalendarUnitHour or NSCalendarUnitMinute, date)
    return Pair(c.hour.toInt(), c.minute.toInt())
}
