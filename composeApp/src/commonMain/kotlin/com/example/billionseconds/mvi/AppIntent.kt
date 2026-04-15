package com.example.billionseconds.mvi

import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.domain.event.model.EventSource
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.ui.profile.LegalLinkType
import com.example.billionseconds.ui.profile.ProfileSubScreen
import com.example.billionseconds.ui.timecapsule.ConditionType

sealed class AppIntent {

    // Onboarding Screen 1
    data object StartClicked : AppIntent()

    // Onboarding Screen 2
    data class OnboardingDateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class OnboardingTimeChanged(val hour: Int, val minute: Int) : AppIntent()
    data object UnknownTimeToggled : AppIntent()
    data object OnboardingCalculateClicked : AppIntent()

    // Onboarding Screen 3
    data object OnboardingContinueClicked : AppIntent()

    // Main app (legacy BirthdayScreen — may be removed later)
    data class DateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class TimeChanged(val hour: Int, val minute: Int) : AppIntent()
    data object CalculateClicked : AppIntent()
    data object ClearClicked : AppIntent()

    // Bottom navigation
    data class TabSelected(val tab: MainTab) : AppIntent()

    // Countdown screen — lifecycle
    data object CountdownScreenStarted : AppIntent()
    data object CountdownScreenResumed : AppIntent()

    // Countdown screen — action buttons
    data object ShareClicked       : AppIntent()
    data object CreateVideoClicked : AppIntent()
    data object WriteLetterClicked : AppIntent()
    data object AddFamilyClicked   : AppIntent()

    // Life Stats screen — lifecycle
    data object LifeStatsScreenStarted : AppIntent()
    data object LifeStatsScreenResumed : AppIntent()

    // Milestones screen — lifecycle
    data object MilestonesScreenStarted : AppIntent()
    data object MilestonesScreenResumed : AppIntent()

    // Milestones screen — actions
    data class MilestoneClicked(val id: String)      : AppIntent()
    data class MilestoneShareClicked(val id: String) : AppIntent()
    data object MilestoneCelebrationDismissed        : AppIntent()

    // Family screen — lifecycle
    data object FamilyScreenStarted : AppIntent()
    data object FamilyScreenResumed : AppIntent()

    // Family screen — list actions
    data object AddProfileClicked                         : AppIntent()
    data class  EditProfileClicked(val id: String)       : AppIntent()
    data class  DeleteProfileClicked(val id: String)     : AppIntent()
    data class  SetActiveProfileClicked(val id: String)  : AppIntent()

    // Family screen — delete confirmation
    data object DeleteConfirmed : AppIntent()
    data object DeleteDismissed : AppIntent()

    // Family screen — form (shared for create and edit)
    data class  FormNameChanged(val name: String)                              : AppIntent()
    data class  FormRelationTypeChanged(val type: RelationType)                : AppIntent()
    data class  FormCustomRelationChanged(val name: String)                    : AppIntent()
    data class  FormBirthDateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class  FormBirthTimeChanged(val hour: Int, val minute: Int)           : AppIntent()
    data object FormUnknownTimeToggled : AppIntent()
    data object FormSaveClicked        : AppIntent()
    data object FormCancelClicked      : AppIntent()

    // Navigation
    data object BackClicked : AppIntent()

    // Profile screen — lifecycle
    data object ProfileScreenStarted : AppIntent()
    data object ProfileScreenResumed : AppIntent()

    // Profile — sub-screen navigation
    data class  ProfileSubScreenSelected(val sub: ProfileSubScreen) : AppIntent()
    data object ProfileSubScreenDismissed : AppIntent()

    // Profile — entry points
    data object ActiveProfileSummaryClicked : AppIntent()
    data object PremiumClicked              : AppIntent()
    data object TimeCapsuleClicked          : AppIntent()
    data object HelpClicked                 : AppIntent()

    // Profile — settings toggles
    data object NotificationsToggled         : AppIntent()
    data object MilestoneRemindersToggled    : AppIntent()
    data object FamilyRemindersToggled       : AppIntent()
    data object ReengagementToggled          : AppIntent()
    data object ApproximateLabelsToggled     : AppIntent()
    data object Use24HourFormatToggled       : AppIntent()

    // Profile — legal
    data class LegalLinkClicked(val type: LegalLinkType) : AppIntent()

    // Profile — dangerous actions
    data object ResetOnboardingClicked  : AppIntent()
    data object ClearAllDataClicked     : AppIntent()
    data object ConfirmDangerousAction  : AppIntent()
    data object DismissConfirmDialog    : AppIntent()

    // Sync
    data object SyncCompleted : AppIntent()

    // Debug / Testing
    data object DebugOpenEventScreen : AppIntent()

    // Time Capsule Screen
    sealed class TimeCapsule : AppIntent() {
        // Lifecycle
        data object ScreenStarted : TimeCapsule()
        // List actions
        data object AddClicked : TimeCapsule()
        data class  EditClicked(val id: String) : TimeCapsule()
        data class  OpenClicked(val id: String) : TimeCapsule()
        data class  DeleteClicked(val id: String) : TimeCapsule()
        data class  ConfirmDelete(val id: String) : TimeCapsule()
        data object CancelDelete : TimeCapsule()
        data object BackClicked : TimeCapsule()
        // Form intents
        data class  FormTitleChanged(val value: String) : TimeCapsule()
        data class  FormMessageChanged(val value: String) : TimeCapsule()
        data class  FormConditionTypeChanged(val type: ConditionType) : TimeCapsule()
        data class  FormDateChanged(val year: String, val month: String, val day: String) : TimeCapsule()
        data class  FormTimeChanged(val hour: String, val minute: String) : TimeCapsule()
        data class  FormRecipientChanged(val profileId: String?) : TimeCapsule()
        data class  FormProfileConditionChanged(val profileId: String) : TimeCapsule()
        data object FormSaveClicked : TimeCapsule()
        data object FormSaveDraftClicked : TimeCapsule()
        data object FormCancelClicked : TimeCapsule()
        data object DiscardDraftConfirmed : TimeCapsule()
    }

    // Event Screen
    sealed class Event : AppIntent() {
        // Lifecycle
        data class ScreenOpened(val profileId: String, val source: EventSource) : Event()
        data object ScreenResumed : Event()
        // Celebration lifecycle
        data object CelebrationDisplayed : Event()   // анимация начала играть
        data object CelebrationCompleted : Event()   // анимация завершилась
        data object CelebrationSkipped : Event()     // пользователь нажал "Пропустить"
        // User actions
        data object ShareClicked : Event()
        data object CreateVideoClicked : Event()
        data object OpenMilestonesClicked : Event()
        data object OpenStatsClicked : Event()
        data object GoHomeClicked : Event()
        data object NextMilestoneClicked : Event()
        data object DismissClicked : Event()
        data object BackPressed : Event()
        data object RetryClicked : Event()
        // System / internal
        data object MarkSeenIfNeeded : Event()           // вызывается Store после ScreenOpened
        data object MarkCelebrationShownIfNeeded : Event() // вызывается Store после CelebrationCompleted
        data class ProfileChanged(val newProfileId: String) : Event()
    }
}
