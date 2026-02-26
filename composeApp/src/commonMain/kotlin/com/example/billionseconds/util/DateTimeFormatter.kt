package com.example.billionseconds.util

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateTimeFormatter {

    fun formatInstant(instant: Instant): String {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(timeZone)
        return "${localDateTime.date} ${localDateTime.time}"
    }

    fun formatInstantWithTimezone(instant: Instant): String {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(timeZone)
        return "${localDateTime.date} ${localDateTime.time} (System timezone)"
    }

    fun formatRelativeTime(instant: Instant): String {
        val now = getCurrentInstant()
        val isPast = now > instant
        
        val targetInstant = if (isPast) instant else now
        val referenceInstant = if (isPast) now else instant
        
        val targetMillis = targetInstant.toEpochMilliseconds()
        val referenceMillis = referenceInstant.toEpochMilliseconds()
        val diffMillis = kotlin.math.abs(targetMillis - referenceMillis)
        val diffSeconds = diffMillis / 1000
        
        val days = diffSeconds / (24 * 60 * 60)
        val remainingHours = diffSeconds % (24 * 60 * 60)
        val hours = remainingHours / (60 * 60)
        val remainingMinutes = remainingHours % (60 * 60)
        val minutes = remainingMinutes / 60
        
        return when {
            isPast -> "Reached $days days $hours hours ago!"
            days > 0L -> "$days days, $hours hours, $minutes minutes until billion seconds!"
            hours > 0L -> "$hours hours, $minutes minutes until billion seconds!"
            minutes > 0L -> "$minutes minutes until billion seconds!"
            else -> "Billion seconds will be reached in less than a minute!"
        }
    }

    private fun toSeconds(instant: Instant): Long {
        val millis = instant.toEpochMilliseconds()
        return millis / 1000
    }

    fun formatDetailedResult(billionSecondsInstant: Instant, birthInstant: Instant): String {
        val formattedDate = formatInstantWithTimezone(billionSecondsInstant)
        val relativeTime = formatRelativeTime(billionSecondsInstant)
        
        return "1,000,000,000 seconds from your birth: $formattedDate\n$relativeTime"
    }
}