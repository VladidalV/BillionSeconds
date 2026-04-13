package com.example.billionseconds.ui.milestones

sealed class MilestonesAction {
    data object ScreenStarted : MilestonesAction()
    data class ShareClicked(val id: String) : MilestonesAction()
    data object CelebrationDismissed : MilestonesAction()
}
