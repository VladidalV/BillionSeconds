package com.example.billionseconds.ui.event

data class EventUiModel(
    val title: String,
    val subtitle: String,
    val eventDateText: String,
    val profileLabel: String,
    val reachedText: String,
    val isApproximateLabelVisible: Boolean,
    val approximateLabel: String,
    val isCelebrationEnabled: Boolean,
    val primaryAction: PostEventActionUi?,
    val secondaryActions: List<PostEventActionUi>,
    val repeatModeNote: String?
)

data class PostEventActionUi(val id: PostEventAction, val label: String, val isEnabled: Boolean)

enum class PostEventAction {
    SHARE, CREATE_VIDEO, OPEN_MILESTONES, OPEN_STATS, GO_HOME, NEXT_MILESTONE
}
