package com.example.billionseconds.mvi

data class LifeStatsUiState(
    val isLoading: Boolean               = true,
    val isUnknownBirthTime: Boolean      = false,
    val ageLabel: String                 = "",
    val exactStats: List<StatItem>       = emptyList(),
    val approximateStats: List<StatItem> = emptyList(),
    val error: LifeStatsError?           = null
)

data class StatItem(
    val id: String,
    val title: String,
    val value: String,
    val isApproximate: Boolean = false,
    val disclaimer: String?    = null
)

sealed class LifeStatsError {
    data object NoBirthData : LifeStatsError()
}
