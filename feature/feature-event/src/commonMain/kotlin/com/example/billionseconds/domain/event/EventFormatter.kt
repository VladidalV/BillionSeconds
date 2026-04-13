package com.example.billionseconds.domain.event

import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.domain.event.model.EventMode
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object EventFormatter {

    fun formatTitle(mode: EventMode, relationType: RelationType, name: String): String =
        when (mode) {
            EventMode.FIRST_TIME -> when (relationType) {
                RelationType.SELF    -> "Ты достиг миллиарда!"
                RelationType.CHILD   -> "$name достиг(ла) миллиарда!"
                RelationType.PARTNER -> "$name достиг(ла) миллиарда!"
                else                 -> "$name: миллиард секунд!"
            }
            EventMode.REPEAT -> when (relationType) {
                RelationType.SELF -> "Твой миллиард секунд"
                else              -> "Миллиард $name"
            }
        }

    fun formatSubtitle(): String = "1 000 000 000 секунд жизни"

    fun formatEventDate(instant: Instant): String {
        val tz = TimeZone.currentSystemDefault()
        val ldt = instant.toLocalDateTime(tz)
        val monthName = monthName(ldt.monthNumber)
        val hour   = ldt.hour.toString().padStart(2, '0')
        val minute = ldt.minute.toString().padStart(2, '0')
        val second = ldt.second.toString().padStart(2, '0')
        return "${ldt.dayOfMonth} $monthName ${ldt.year}, $hour:$minute:$second"
    }

    /**
     * Текст под датой события.
     * FirstTime без history: "Достигнуто"
     * FirstTime с историей (открылись через время): "Достигнуто X дней назад"
     * Repeat: "Открываешь повторно · достигнуто X дней назад"
     */
    fun formatReachedText(
        mode: EventMode,
        firstShownAt: Instant?,
        targetDateTime: Instant,
        now: Instant
    ): String {
        val daysAgo = daysBetween(targetDateTime, now)
        return when (mode) {
            EventMode.FIRST_TIME -> if (daysAgo <= 0) "Достигнуто" else "Достигнуто $daysAgo ${daysLabel(daysAgo)} назад"
            EventMode.REPEAT -> {
                val agoText = if (daysAgo <= 0) "сегодня" else "$daysAgo ${daysLabel(daysAgo)} назад"
                "Событие наступило $agoText"
            }
        }
    }

    fun formatProfileLabel(
        name: String,
        relationType: RelationType,
        customName: String?
    ): String {
        val relationLabel = when (relationType) {
            RelationType.OTHER -> customName?.takeIf { it.isNotBlank() } ?: relationType.displayLabel
            else               -> relationType.displayLabel
        }
        return "$name · $relationLabel"
    }

    fun formatRepeatModeNote(firstShownAt: Instant, now: Instant): String {
        val days = daysBetween(firstShownAt, now)
        return if (days <= 0) "Ты уже открывал этот экран сегодня"
        else "Впервые открыто $days ${daysLabel(days)} назад"
    }

    fun formatApproximateLabel(): String =
        "Время рождения приблизительное — событие могло наступить раньше или позже"

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun daysBetween(from: Instant, to: Instant): Long {
        val seconds = (to - from).inWholeSeconds
        return if (seconds < 0) 0L else seconds / 86_400L
    }

    private fun daysLabel(days: Long): String = when {
        days % 10L == 1L && days % 100L != 11L -> "день"
        days % 10L in 2..4 && days % 100L !in 12..14 -> "дня"
        else -> "дней"
    }

    private fun monthName(month: Int): String = when (month) {
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
        else -> month.toString()
    }
}
