package com.example.billionseconds.mvi

import com.example.billionseconds.ui.milestones.MilestonesAction

fun milestonesAdapter(dispatch: (AppIntent) -> Unit): (MilestonesAction) -> Unit = { action ->
    when (action) {
        MilestonesAction.ScreenStarted       -> dispatch(AppIntent.MilestonesScreenStarted)
        MilestonesAction.CelebrationDismissed -> dispatch(AppIntent.MilestoneCelebrationDismissed)
        is MilestonesAction.ShareClicked     -> dispatch(AppIntent.MilestoneShareClicked(action.id))
    }
}
