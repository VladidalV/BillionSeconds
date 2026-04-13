package com.example.billionseconds.domain

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object MilestonesFormatter {

    /** "100 млн" / "1 млрд" — shortTitle из definition */
    fun formatThreshold(seconds: Long): String = when {
        seconds >= 1_000_000_000L -> {
            val b = seconds / 1_000_000_000L
            "$b млрд"
        }
        seconds >= 1_000_000L -> {
            val m = seconds / 1_000_000L
            "$m млн"
        }
        else -> formatLarge(seconds)
    }

    /** "12 марта 2025" */
    fun formatTargetDate(instant: Instant, isApproximate: Boolean = false): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day   = dt.dayOfMonth
        val month = monthNameGenitive(dt.monthNumber)
        val year  = dt.year
        val prefix = if (isApproximate) "~\u00A0" else ""
        return "${prefix}$day $month $year"
    }

    /**
     * "2 года 4 мес." / "15 дней" / "3 часа" / "45 мин."
     * Показываем только значимые единицы.
     */
    fun formatRemaining(seconds: Long): String {
        if (seconds <= 0L) return "уже достигнуто"
        val days  = seconds / 86_400L
        val hours = (seconds % 86_400L) / 3_600L
        val mins  = (seconds % 3_600L) / 60L

        return when {
            days >= 365L -> {
                val years = (days / 365.25).toInt()
                val remDays = days - (years * 365.25).toLong()
                val months = (remDays / 30.44).toInt()
                if (months > 0) "$years ${pluralYears(years)} $months ${pluralMonths(months)}"
                else "$years ${pluralYears(years)}"
            }
            days >= 30L -> {
                val months = (days / 30.44).toInt().coerceAtLeast(1)
                "$months ${pluralMonths(months)}"
            }
            days >= 1L -> "$days ${pluralDays(days.toInt())}"
            hours >= 1L -> "$hours ${pluralHours(hours.toInt())}"
            else -> "$mins мин."
        }
    }

    /** "83.4%" */
    fun formatProgress(fraction: Float): String {
        val clamped = (fraction * 100f).coerceIn(0f, 100f)
        val whole   = clamped.toInt()
        val decimal = ((clamped - whole) * 10).toInt()
        return "$whole.$decimal%"
    }

    /** "Достигнуто 12.03.2025" */
    fun formatReachedDate(instant: Instant): String {
        val dt  = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val mon = dt.monthNumber.toString().padStart(2, '0')
        return "Достигнуто $day.$mon.${dt.year}"
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun formatLarge(value: Long): String {
        if (value < 0L) return "0"
        return value.toString().reversed().chunked(3).joinToString("\u00A0").reversed()
    }

    private fun monthNameGenitive(month: Int): String = when (month) {
        1  -> "января"
        2  -> "февраля"
        3  -> "марта"
        4  -> "апреля"
        5  -> "мая"
        6  -> "июня"
        7  -> "июля"
        8  -> "августа"
        9  -> "сентября"
        10 -> "октября"
        11 -> "ноября"
        12 -> "декабря"
        else -> "?"
    }

    private fun pluralYears(n: Int): String = when {
        n % 100 in 11..19 -> "лет"
        n % 10 == 1        -> "год"
        n % 10 in 2..4     -> "года"
        else               -> "лет"
    }

    private fun pluralMonths(n: Int): String = "мес."

    private fun pluralDays(n: Int): String = when {
        n % 100 in 11..19 -> "дней"
        n % 10 == 1        -> "день"
        n % 10 in 2..4     -> "дня"
        else               -> "дней"
    }

    private fun pluralHours(n: Int): String = when {
        n % 100 in 11..19 -> "часов"
        n % 10 == 1        -> "час"
        n % 10 in 2..4     -> "часа"
        else               -> "часов"
    }
}
