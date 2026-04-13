package com.example.billionseconds.mvi

import com.example.billionseconds.ui.event.EventAction

fun eventAdapter(dispatch: (AppIntent) -> Unit): (EventAction) -> Unit = { action ->
    when (action) {
        EventAction.ScreenResumed         -> dispatch(AppIntent.Event.ScreenResumed)
        EventAction.CelebrationDisplayed  -> dispatch(AppIntent.Event.CelebrationDisplayed)
        EventAction.CelebrationCompleted  -> dispatch(AppIntent.Event.CelebrationCompleted)
        EventAction.CelebrationSkipped    -> dispatch(AppIntent.Event.CelebrationSkipped)
        EventAction.RetryClicked          -> dispatch(AppIntent.Event.RetryClicked)
        EventAction.GoHomeClicked         -> dispatch(AppIntent.Event.GoHomeClicked)
        EventAction.DismissClicked        -> dispatch(AppIntent.Event.DismissClicked)
        EventAction.BackPressed           -> dispatch(AppIntent.Event.BackPressed)
        EventAction.ShareClicked          -> dispatch(AppIntent.Event.ShareClicked)
        EventAction.CreateVideoClicked    -> dispatch(AppIntent.Event.CreateVideoClicked)
        EventAction.OpenMilestonesClicked -> dispatch(AppIntent.Event.OpenMilestonesClicked)
        EventAction.OpenStatsClicked      -> dispatch(AppIntent.Event.OpenStatsClicked)
        EventAction.NextMilestoneClicked  -> dispatch(AppIntent.Event.NextMilestoneClicked)
    }
}
