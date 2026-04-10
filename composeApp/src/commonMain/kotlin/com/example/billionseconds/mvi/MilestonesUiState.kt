package com.example.billionseconds.mvi

import com.example.billionseconds.domain.MilestoneStatus

data class MilestonesUiState(
    val isLoading: Boolean                = true,
    val milestones: List<MilestoneUiItem> = emptyList(),
    val highlightedId: String?            = null,
    val isApproximateMode: Boolean        = false,
    val celebrationAvailableId: String?   = null,
    val error: MilestonesError?           = null
)

data class MilestoneUiItem(
    val id: String,
    val title: String,
    val shortTitle: String,
    val targetDateText: String,
    val statusLabel: String,
    val progressText: String,
    val remainingText: String,
    val reachedDateText: String,
    val status: MilestoneStatus,
    val isPrimary: Boolean,
    val isShareable: Boolean,
    val hasApproximateDisclaimer: Boolean
)

sealed class MilestonesError {
    data object NoBirthData : MilestonesError()
}
