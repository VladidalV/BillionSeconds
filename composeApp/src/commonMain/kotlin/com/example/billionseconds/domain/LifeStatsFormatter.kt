package com.example.billionseconds.domain

object LifeStatsFormatter {

    /** 1 234 567 — с неразрывными пробелами как разделителями тысяч */
    fun formatLarge(value: Long): String {
        if (value < 0L) return "0"
        return value.toString().reversed().chunked(3).joinToString("\u00A0").reversed()
    }

    /** ≈ 1 234 567 */
    fun formatApprox(value: Long): String = "\u2248\u00A0${formatLarge(value)}"

    /** 2.1 млрд если ≥ 1 000 000 000, иначе formatLarge */
    fun formatBillions(value: Long): String {
        if (value < 1_000_000_000L) return formatLarge(value)
        val billions = value.toDouble() / 1_000_000_000.0
        val whole   = billions.toInt()
        val decimal = ((billions - whole) * 10).toInt()
        return "$whole.$decimal млрд"
    }

    /** ≈ 2.1 млрд */
    fun formatApproxBillions(value: Long): String = "\u2248\u00A0${formatBillions(value)}"

    /** 38.4% */
    fun formatPercent(value: Float): String {
        val clamped = value.coerceIn(0f, 100f)
        val whole   = clamped.toInt()
        val decimal = ((clamped - whole) * 10).toInt()
        return "$whole.$decimal%"
    }

    /** "31 год 5 мес. 12 дней" */
    fun formatAge(years: Int, months: Int, days: Int): String {
        val yearsStr  = "$years ${pluralYears(years)}"
        val monthsStr = if (months > 0) " $months ${pluralMonths(months)}" else ""
        val daysStr   = if (days > 0)   " $days ${pluralDays(days)}" else ""
        return "$yearsStr$monthsStr$daysStr"
    }

    // ── Plural helpers (русский язык) ────────────────────────────────────────

    private fun pluralYears(n: Int): String = when {
        n % 100 in 11..19   -> "лет"
        n % 10 == 1          -> "год"
        n % 10 in 2..4       -> "года"
        else                 -> "лет"
    }

    private fun pluralMonths(n: Int): String = when {
        n % 100 in 11..19   -> "мес."
        n % 10 == 1          -> "мес."
        n % 10 in 2..4       -> "мес."
        else                 -> "мес."
    }

    private fun pluralDays(n: Int): String = when {
        n % 100 in 11..19   -> "дней"
        n % 10 == 1          -> "день"
        n % 10 in 2..4       -> "дня"
        else                 -> "дней"
    }
}
