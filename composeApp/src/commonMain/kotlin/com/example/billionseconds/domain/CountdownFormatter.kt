package com.example.billionseconds.domain

import kotlinx.datetime.*

object CountdownFormatter {

    fun formatMilestoneDate(instant: Instant): String {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.dayOfMonth.pad()}.${local.monthNumber.pad()}.${local.year}"
    }

    fun formatMilestoneTime(instant: Instant): String {
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${local.hour.pad()}:${local.minute.pad()}:${local.second.pad()}"
    }

    fun formatCountdown(secondsRemaining: Long): String {
        if (secondsRemaining <= 0L) return "00:00:00"
        val days    = secondsRemaining / 86_400
        val hours   = (secondsRemaining % 86_400) / 3_600
        val minutes = (secondsRemaining % 3_600) / 60
        val secs    = secondsRemaining % 60
        return if (days > 0) {
            "${days}д ${hours.pad()}:${minutes.pad()}:${secs.pad()}"
        } else {
            "${hours.pad()}:${minutes.pad()}:${secs.pad()}"
        }
    }

    fun formatProgress(fraction: Float): String {
        val percent = (fraction * 100f).coerceIn(0f, 100f)
        return "%.1f%%".format(percent)
    }

    private fun Int.pad(): String  = toString().padStart(2, '0')
    private fun Long.pad(): String = toString().padStart(2, '0')
}
