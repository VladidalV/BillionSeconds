package com.example.billionseconds.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateTimeFormatter {

    fun formatInstant(instant: kotlinx.datetime.Instant): String {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(timeZone)
        return "${localDateTime.date} ${localDateTime.time}"
    }

    fun formatInstantWithTimezone(instant: kotlinx.datetime.Instant): String {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = instant.toLocalDateTime(timeZone)
        return "${localDateTime.date} ${localDateTime.time} (Системный часовой пояс)"
    }

    fun formatRelativeTime(instant: kotlinx.datetime.Instant): String {
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
            isPast -> "Достигнуто $days дней $hours часов назад!"
            days > 0L -> "$days дней, $hours часов, $minutes минут до миллиарда секунд!"
            hours > 0L -> "$hours часов, $minutes минут до миллиарда секунд!"
            minutes > 0L -> "$minutes минут до миллиарда секунд!"
            else -> "Миллиард секунд будет достигнут менее чем через минуту!"
        }
    }

    private fun toSeconds(instant: kotlinx.datetime.Instant): Long {
        val millis = instant.toEpochMilliseconds()
        return millis / 1000
    }

    fun formatDetailedResult(
        billionSecondsInstant: kotlinx.datetime.Instant,
        birthInstant: kotlinx.datetime.Instant
    ): String {
        val formattedDate = formatInstantWithTimezone(billionSecondsInstant)
        val relativeTime = formatRelativeTime(billionSecondsInstant)
        return "1 000 000 000 секунд с момента вашего рождения: $formattedDate\n$relativeTime"
    }
}