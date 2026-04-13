package com.example.billionseconds.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object TimeCapsuleFormatter {

    private val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )

    fun formatUnlockDate(epochMs: Long): String {
        val instant = Instant.fromEpochMilliseconds(epochMs)
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dt.dayOfMonth} ${months[dt.monthNumber - 1]} ${dt.year}"
    }

    fun formatCreatedDate(epochMs: Long): String = formatUnlockDate(epochMs)

    fun formatRemainingTime(deltaMs: Long): String {
        val totalSeconds = deltaMs / 1000L
        val days   = totalSeconds / 86400L
        val hours  = (totalSeconds % 86400L) / 3600L
        val years  = days / 365L
        val months = (days % 365L) / 30L
        val remDays = (days % 365L) % 30L

        return when {
            years > 0 && months > 0 -> "${years} г. ${months} мес."
            years > 0               -> "${years} г. ${remDays} д."
            months > 0              -> "${months} мес. ${remDays} д."
            days > 0                -> "${days} д."
            hours > 0               -> "${hours} ч."
            else                    -> "< 1 ч."
        }
    }

    fun formatUnlockLabel(epochMs: Long, profileName: String? = null): String =
        if (profileName != null) {
            "Миллиард секунд $profileName"
        } else {
            "Открывается ${formatUnlockDate(epochMs)}"
        }
}
