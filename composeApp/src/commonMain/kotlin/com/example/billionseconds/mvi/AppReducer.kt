package com.example.billionseconds.mvi

import com.example.billionseconds.domain.event.model.EventMode
import com.example.billionseconds.ui.event.EventScreenStatus
import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.data.model.AppSettings
import com.example.billionseconds.ui.family.FamilySubScreen
import com.example.billionseconds.ui.timecapsule.CapsuleFormDraft
import com.example.billionseconds.ui.timecapsule.TimeCapsuleSubScreen
import com.example.billionseconds.ui.profile.ProfileSubScreen
import com.example.billionseconds.ui.profile.ProfileConfirmDialog

object AppReducer {

    fun reduce(state: AppState, intent: AppIntent): AppState = when (intent) {

        // Onboarding Screen 1
        is AppIntent.StartClicked ->
            state.copy(error = null)

        // Onboarding Screen 2
        is AppIntent.OnboardingDateChanged ->
            state.copy(year = intent.year, month = intent.month, day = intent.day, error = null)

        is AppIntent.OnboardingTimeChanged ->
            state.copy(hour = intent.hour, minute = intent.minute)

        is AppIntent.UnknownTimeToggled -> {
            val newFlag = !state.unknownTime
            state.copy(
                unknownTime = newFlag,
                hour = if (newFlag) 12 else state.hour,
                minute = if (newFlag) 0 else state.minute
            )
        }

        // Side effects handled in AppStore; reducer only clears error
        is AppIntent.OnboardingCalculateClicked ->
            state.copy(error = null)

        // Side effect in AppStore
        is AppIntent.OnboardingContinueClicked ->
            state

        // Main app
        is AppIntent.DateChanged ->
            state.copy(year = intent.year, month = intent.month, day = intent.day, error = null)

        is AppIntent.TimeChanged ->
            state.copy(hour = intent.hour, minute = intent.minute)

        is AppIntent.CalculateClicked ->
            state.copy(error = null)

        is AppIntent.ClearClicked ->
            state.copy(
                year = null, month = null, day = null,
                hour = 12, minute = 0,
                milestoneInstant = null, secondsRemaining = 0L,
                progressPercent = 0f, isMilestoneReached = false,
                showMainResult = false, error = null,
                unknownTime = false
            )

        // Bottom navigation — navigator handles routing; reducer resets sub-states
        is AppIntent.TabSelected ->
            state.copy(
                family = state.family.copy(
                    formDraft = null,
                    subScreen = FamilySubScreen.List,
                    isDeleteConfirmationVisible = false,
                    pendingDeleteId = null
                ),
                profile = state.profile.copy(
                    subScreen = ProfileSubScreen.Root,
                    confirmDialog = null
                )
            )

        // Countdown screen — lifecycle (side effects handled in Store)
        is AppIntent.CountdownScreenStarted -> state
        is AppIntent.CountdownScreenResumed -> state

        // Countdown screen — action buttons (side effects only)
        is AppIntent.ShareClicked       -> state
        is AppIntent.CreateVideoClicked -> state
        is AppIntent.WriteLetterClicked -> state
        is AppIntent.AddFamilyClicked   -> state

        // Life Stats screen — lifecycle (side effects in Store)
        is AppIntent.LifeStatsScreenStarted -> state
        is AppIntent.LifeStatsScreenResumed -> state

        // Milestones screen — lifecycle (side effects in Store)
        is AppIntent.MilestonesScreenStarted -> state
        is AppIntent.MilestonesScreenResumed -> state

        // Milestones screen — actions
        is AppIntent.MilestoneClicked           -> state
        is AppIntent.MilestoneShareClicked      -> state
        is AppIntent.MilestoneCelebrationDismissed ->
            state.copy(milestones = state.milestones.copy(celebrationAvailableId = null))

        // Family screen — lifecycle (side effects in Store)
        is AppIntent.FamilyScreenStarted -> state
        is AppIntent.FamilyScreenResumed -> state

        // Family screen — list actions (side effects in Store)
        is AppIntent.AddProfileClicked        -> state
        is AppIntent.EditProfileClicked       -> state
        is AppIntent.SetActiveProfileClicked  -> state

        // Delete flow
        is AppIntent.DeleteProfileClicked ->
            state.copy(family = state.family.copy(
                pendingDeleteId             = intent.id,
                isDeleteConfirmationVisible = true
            ))

        is AppIntent.DeleteConfirmed -> state  // side effect in Store
        is AppIntent.DeleteDismissed ->
            state.copy(family = state.family.copy(
                pendingDeleteId             = null,
                isDeleteConfirmationVisible = false
            ))

        // Form reducers
        is AppIntent.FormNameChanged ->
            state.copy(family = state.family.copy(
                formDraft = state.family.formDraft?.copy(
                    name = intent.name,
                    nameError = null,
                    isDirty = true
                )
            ))

        is AppIntent.FormRelationTypeChanged ->
            state.copy(family = state.family.copy(
                formDraft = state.family.formDraft?.copy(
                    relationType = intent.type,
                    isDirty = true
                )
            ))

        is AppIntent.FormCustomRelationChanged ->
            state.copy(family = state.family.copy(
                formDraft = state.family.formDraft?.copy(
                    customRelationName = intent.name,
                    isDirty = true
                )
            ))

        is AppIntent.FormBirthDateChanged ->
            state.copy(family = state.family.copy(
                formDraft = state.family.formDraft?.copy(
                    year = intent.year, month = intent.month, day = intent.day,
                    dateError = null,
                    isDirty = true
                )
            ))

        is AppIntent.FormBirthTimeChanged ->
            state.copy(family = state.family.copy(
                formDraft = state.family.formDraft?.copy(
                    hour = intent.hour, minute = intent.minute,
                    isDirty = true
                )
            ))

        is AppIntent.FormUnknownTimeToggled -> {
            val draft = state.family.formDraft
            if (draft == null) state
            else {
                val newFlag = !draft.unknownBirthTime
                state.copy(family = state.family.copy(
                    formDraft = draft.copy(
                        unknownBirthTime = newFlag,
                        hour   = if (newFlag) 12 else draft.hour,
                        minute = if (newFlag) 0  else draft.minute,
                        isDirty = true
                    )
                ))
            }
        }

        is AppIntent.FormCancelClicked ->
            state.copy(family = state.family.copy(
                formDraft = null,
                subScreen = FamilySubScreen.List
            ))

        is AppIntent.FormSaveClicked -> state  // validation + save in Store

        // Navigation
        is AppIntent.BackClicked -> state // ExitApp effect emitted in AppStore

        // Profile screen — lifecycle
        is AppIntent.ProfileScreenStarted -> state
        is AppIntent.ProfileScreenResumed -> state

        // Profile — sub-screen navigation
        is AppIntent.ProfileSubScreenSelected ->
            state.copy(profile = state.profile.copy(subScreen = intent.sub, confirmDialog = null))

        is AppIntent.ProfileSubScreenDismissed ->
            state.copy(profile = state.profile.copy(subScreen = ProfileSubScreen.Root))

        // Profile — entry points (side effects in Store)
        is AppIntent.ActiveProfileSummaryClicked -> state
        is AppIntent.PremiumClicked              -> state
        is AppIntent.TimeCapsuleClicked          -> state
        is AppIntent.HelpClicked                 -> state
        is AppIntent.LegalLinkClicked            -> state

        // Profile — settings toggles
        is AppIntent.NotificationsToggled ->
            state.copy(profile = state.profile.copy(
                settings = state.profile.settings.copy(
                    notificationsEnabled = !state.profile.settings.notificationsEnabled
                )
            ))

        is AppIntent.MilestoneRemindersToggled ->
            state.copy(profile = state.profile.copy(
                settings = state.profile.settings.copy(
                    milestoneRemindersEnabled = !state.profile.settings.milestoneRemindersEnabled
                )
            ))

        is AppIntent.FamilyRemindersToggled ->
            state.copy(profile = state.profile.copy(
                settings = state.profile.settings.copy(
                    familyRemindersEnabled = !state.profile.settings.familyRemindersEnabled
                )
            ))

        is AppIntent.ReengagementToggled ->
            state.copy(profile = state.profile.copy(
                settings = state.profile.settings.copy(
                    reengagementEnabled = !state.profile.settings.reengagementEnabled
                )
            ))

        is AppIntent.ApproximateLabelsToggled ->
            state.copy(profile = state.profile.copy(
                settings = state.profile.settings.copy(
                    approximateLabelsEnabled = !state.profile.settings.approximateLabelsEnabled
                )
            ))

        is AppIntent.Use24HourFormatToggled ->
            state.copy(profile = state.profile.copy(
                settings = state.profile.settings.copy(
                    use24HourFormat = !state.profile.settings.use24HourFormat
                )
            ))

        // Profile — dangerous actions
        is AppIntent.ResetOnboardingClicked ->
            state.copy(profile = state.profile.copy(
                confirmDialog = ProfileConfirmDialog.ResetOnboarding
            ))

        is AppIntent.ClearAllDataClicked ->
            state.copy(profile = state.profile.copy(
                confirmDialog = ProfileConfirmDialog.ClearAllData
            ))

        is AppIntent.ConfirmDangerousAction ->
            state.copy(profile = state.profile.copy(isActionInProgress = true))

        is AppIntent.DismissConfirmDialog ->
            state.copy(profile = state.profile.copy(
                confirmDialog = null,
                isActionInProgress = false
            ))

        // Debug
        is AppIntent.DebugOpenEventScreen -> state

        // Event Screen — reducer handles only pure state transitions
        is AppIntent.Event.ScreenOpened ->
            state.copy(
                event = state.event.copy(
                    isLoading    = true,
                    screenStatus = EventScreenStatus.Loading,
                    isBackAllowed = false,
                    isCelebrationRunning = false,
                    celebrationCompleted = false,
                    source = intent.source,
                    profileId = intent.profileId,
                    uiModel = null,
                    errorMessage = null
                )
            )

        is AppIntent.Event.ScreenResumed -> state

        is AppIntent.Event.CelebrationDisplayed ->
            state.copy(event = state.event.copy(isCelebrationRunning = true, triggerCelebration = false))

        is AppIntent.Event.CelebrationCompleted ->
            state.copy(event = state.event.copy(
                isCelebrationRunning = false,
                celebrationCompleted = true,
                isBackAllowed = true
            ))

        is AppIntent.Event.CelebrationSkipped ->
            state.copy(event = state.event.copy(
                isCelebrationRunning = false,
                celebrationCompleted = true,
                isBackAllowed = true
            ))

        // User actions — side effects handled in Store
        is AppIntent.Event.ShareClicked          -> state
        is AppIntent.Event.CreateVideoClicked    -> state
        is AppIntent.Event.OpenMilestonesClicked -> state
        is AppIntent.Event.OpenStatsClicked      -> state
        is AppIntent.Event.NextMilestoneClicked  -> state
        is AppIntent.Event.RetryClicked          -> state

        is AppIntent.Event.GoHomeClicked ->
            state.copy(event = state.event.copy(autoOpenTriggered = false))

        is AppIntent.Event.DismissClicked ->
            if (state.event.isBackAllowed || state.event.mode == EventMode.REPEAT)
                state.copy(event = state.event.copy(autoOpenTriggered = false))
            else state  // dismiss заблокирован до завершения celebration

        is AppIntent.Event.BackPressed ->
            if (state.event.isBackAllowed || state.event.mode == EventMode.REPEAT)
                state.copy(event = state.event.copy(autoOpenTriggered = false))
            else state  // back заблокирован во время first-time celebration

        // System intents — side effects only in Store
        is AppIntent.Event.MarkSeenIfNeeded            -> state
        is AppIntent.Event.MarkCelebrationShownIfNeeded -> state

        is AppIntent.Event.ProfileChanged ->
            state.copy(event = state.event.copy(autoOpenTriggered = false))

        // Time Capsule Screen
        is AppIntent.TimeCapsule.ScreenStarted ->
            state.copy(timeCapsule = state.timeCapsule.copy(isLoading = true, error = null))

        is AppIntent.TimeCapsule.AddClicked ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.Create,
                formDraft = CapsuleFormDraft()
            ))

        is AppIntent.TimeCapsule.EditClicked ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.Edit(intent.id)
            ))

        is AppIntent.TimeCapsule.OpenClicked ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.Open(intent.id)
            ))

        is AppIntent.TimeCapsule.DeleteClicked ->
            state.copy(timeCapsule = state.timeCapsule.copy(confirmDeleteId = intent.id))

        is AppIntent.TimeCapsule.CancelDelete ->
            state.copy(timeCapsule = state.timeCapsule.copy(confirmDeleteId = null))

        is AppIntent.TimeCapsule.ConfirmDelete ->
            state.copy(timeCapsule = state.timeCapsule.copy(confirmDeleteId = null))

        is AppIntent.TimeCapsule.BackClicked ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.List,
                formDraft = null,
                openedCapsule = null
            ))

        is AppIntent.TimeCapsule.FormTitleChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    title = intent.value, titleError = null, isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormMessageChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    message = intent.value, messageError = null, isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormConditionTypeChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    conditionType = intent.type, conditionError = null, isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormDateChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    selectedYear = intent.year,
                    selectedMonth = intent.month,
                    selectedDay = intent.day,
                    conditionError = null,
                    isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormTimeChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    selectedHour = intent.hour,
                    selectedMinute = intent.minute,
                    isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormRecipientChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    recipientProfileId = intent.profileId, isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormProfileConditionChanged ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                formDraft = state.timeCapsule.formDraft?.copy(
                    selectedProfileId = intent.profileId, conditionError = null, isDirty = true
                )
            ))

        is AppIntent.TimeCapsule.FormSaveClicked     -> state // validation + save in Store
        is AppIntent.TimeCapsule.FormSaveDraftClicked -> state // save in Store

        is AppIntent.TimeCapsule.FormCancelClicked ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.List,
                formDraft = null
            ))

        is AppIntent.TimeCapsule.DiscardDraftConfirmed ->
            state.copy(timeCapsule = state.timeCapsule.copy(
                subScreen = TimeCapsuleSubScreen.List,
                formDraft = null
            ))
    }
}

