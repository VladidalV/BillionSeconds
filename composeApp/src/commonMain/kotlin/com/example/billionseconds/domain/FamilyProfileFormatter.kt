package com.example.billionseconds.domain

import com.example.billionseconds.data.model.RelationType
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object FamilyProfileFormatter {

    fun formatRelation(type: RelationType, customName: String?): String =
        if (type == RelationType.OTHER && !customName.isNullOrBlank())
            "Другое: $customName"
        else
            type.displayLabel

    fun formatBirthDate(year: Int, month: Int, day: Int): String =
        "$day ${monthNameGenitive(month)} $year"

    fun formatBillionDate(instant: Instant, isApproximate: Boolean): String {
        val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val prefix = if (isApproximate) "~\u00A0" else ""
        return "${prefix}${dt.dayOfMonth} ${monthNameGenitive(dt.monthNumber)} ${dt.year}"
    }

    fun formatCountdown(secondsRemaining: Long, isReached: Boolean): String {
        if (isReached || secondsRemaining <= 0L) return "уже достигнуто"
        return "через ${MilestonesFormatter.formatRemaining(secondsRemaining)}"
    }

    // fraction — значение от 0f до 1f (как возвращает BillionSecondsCalculator.progressPercent)
    fun formatProgress(fraction: Float): String {
        val pct     = (fraction * 100f).coerceIn(0f, 100f)
        val whole   = pct.toInt()
        val decimal = ((pct - whole) * 10).toInt()
        return "$whole.$decimal%"
    }

    // ── Private ───────────────────────────────────────────────────────────────

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
}
