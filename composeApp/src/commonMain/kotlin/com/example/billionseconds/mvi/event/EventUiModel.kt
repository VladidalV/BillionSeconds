package com.example.billionseconds.mvi.event

data class EventUiModel(
    // Header
    val title: String,
    val subtitle: String,
    val eventDateText: String,
    val profileLabel: String,
    val reachedText: String,
    val isApproximateLabelVisible: Boolean,
    val approximateLabel: String,
    // Celebration
    val isCelebrationEnabled: Boolean,
    // Actions
    val primaryAction: PostEventActionUi?,
    val secondaryActions: List<PostEventActionUi>,
    // Repeat-mode specific
    val repeatModeNote: String?
)

data class PostEventActionUi(
    val id: PostEventAction,
    val label: String,
    val isEnabled: Boolean
)

enum class PostEventAction {
    SHARE,
    CREATE_VIDEO,
    OPEN_MILESTONES,
    OPEN_STATS,
    GO_HOME,
    NEXT_MILESTONE
}
