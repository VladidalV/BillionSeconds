package com.example.billionseconds.ui.profile

sealed class ProfileAction {

    data object ScreenStarted : ProfileAction()
    data object SubScreenDismissed : ProfileAction()
    data class  SubScreenSelected(val sub: ProfileSubScreen) : ProfileAction()

    data object ActiveProfileSummaryClicked : ProfileAction()
    data object PremiumClicked : ProfileAction()
    data object TimeCapsuleClicked : ProfileAction()
    data object HelpClicked : ProfileAction()

    data object NotificationsToggled : ProfileAction()
    data object MilestoneRemindersToggled : ProfileAction()
    data object FamilyRemindersToggled : ProfileAction()
    data object ReengagementToggled : ProfileAction()
    data object ApproximateLabelsToggled : ProfileAction()
    data object Use24HourFormatToggled : ProfileAction()

    data class  LegalLinkClicked(val type: LegalLinkType) : ProfileAction()

    data object ResetOnboardingClicked : ProfileAction()
    data object ClearAllDataClicked : ProfileAction()
    data object ConfirmDangerousAction : ProfileAction()
    data object DismissConfirmDialog : ProfileAction()

    data object DebugOpenEventScreen : ProfileAction()
}
