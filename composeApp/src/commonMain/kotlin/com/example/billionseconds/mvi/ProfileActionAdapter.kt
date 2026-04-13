package com.example.billionseconds.mvi

import com.example.billionseconds.ui.profile.ProfileAction

fun profileAdapter(dispatch: (AppIntent) -> Unit): (ProfileAction) -> Unit = { action ->
    when (action) {
        ProfileAction.ScreenStarted              -> dispatch(AppIntent.ProfileScreenStarted)
        ProfileAction.SubScreenDismissed         -> dispatch(AppIntent.ProfileSubScreenDismissed)
        is ProfileAction.SubScreenSelected       -> dispatch(AppIntent.ProfileSubScreenSelected(action.sub))
        ProfileAction.ActiveProfileSummaryClicked -> dispatch(AppIntent.ActiveProfileSummaryClicked)
        ProfileAction.PremiumClicked             -> dispatch(AppIntent.PremiumClicked)
        ProfileAction.TimeCapsuleClicked         -> dispatch(AppIntent.TimeCapsuleClicked)
        ProfileAction.HelpClicked                -> dispatch(AppIntent.HelpClicked)
        ProfileAction.NotificationsToggled       -> dispatch(AppIntent.NotificationsToggled)
        ProfileAction.MilestoneRemindersToggled  -> dispatch(AppIntent.MilestoneRemindersToggled)
        ProfileAction.FamilyRemindersToggled     -> dispatch(AppIntent.FamilyRemindersToggled)
        ProfileAction.ReengagementToggled        -> dispatch(AppIntent.ReengagementToggled)
        ProfileAction.ApproximateLabelsToggled   -> dispatch(AppIntent.ApproximateLabelsToggled)
        ProfileAction.Use24HourFormatToggled     -> dispatch(AppIntent.Use24HourFormatToggled)
        is ProfileAction.LegalLinkClicked        -> dispatch(AppIntent.LegalLinkClicked(action.type))
        ProfileAction.ResetOnboardingClicked     -> dispatch(AppIntent.ResetOnboardingClicked)
        ProfileAction.ClearAllDataClicked        -> dispatch(AppIntent.ClearAllDataClicked)
        ProfileAction.ConfirmDangerousAction     -> dispatch(AppIntent.ConfirmDangerousAction)
        ProfileAction.DismissConfirmDialog       -> dispatch(AppIntent.DismissConfirmDialog)
        ProfileAction.DebugOpenEventScreen       -> dispatch(AppIntent.DebugOpenEventScreen)
    }
}
