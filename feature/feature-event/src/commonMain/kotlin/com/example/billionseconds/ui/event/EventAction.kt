package com.example.billionseconds.ui.event

sealed class EventAction {
    data object ScreenResumed          : EventAction()
    data object CelebrationDisplayed   : EventAction()
    data object CelebrationCompleted   : EventAction()
    data object CelebrationSkipped     : EventAction()
    data object RetryClicked           : EventAction()
    data object GoHomeClicked          : EventAction()
    data object DismissClicked         : EventAction()
    data object BackPressed            : EventAction()
    data object ShareClicked           : EventAction()
    data object CreateVideoClicked     : EventAction()
    data object OpenMilestonesClicked  : EventAction()
    data object OpenStatsClicked       : EventAction()
    data object NextMilestoneClicked   : EventAction()
}
