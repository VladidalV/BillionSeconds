package com.example.billionseconds.ui.profile

import com.example.billionseconds.data.model.AppSettings

data class ProfileUiState(
    val isLoading: Boolean                      = true,
    val activeProfileSummary: ActiveProfileSummary? = null,
    val settings: AppSettings                  = AppSettings(),
    val appVersion: String                     = "",
    val subScreen: ProfileSubScreen            = ProfileSubScreen.Root,
    val confirmDialog: ProfileConfirmDialog?   = null,
    val isActionInProgress: Boolean            = false,
    val error: ProfileError?                   = null
)

data class ActiveProfileSummary(
    val name: String,
    val relationLabel: String,
    val relationEmoji: String,
    val billionDateText: String,
    val hasApproximateTime: Boolean,
    val isPrimary: Boolean
)

sealed class ProfileSubScreen {
    data object Root                 : ProfileSubScreen()
    data object NotificationSettings : ProfileSubScreen()
    data object AppSettings          : ProfileSubScreen()
    data object DataManagement       : ProfileSubScreen()
    data object AboutApp             : ProfileSubScreen()
}

sealed class ProfileConfirmDialog {
    data object ResetOnboarding : ProfileConfirmDialog()
    data object ClearAllData    : ProfileConfirmDialog()
}

sealed class ProfileError {
    data object LoadFailed         : ProfileError()
    data object SettingsSaveFailed : ProfileError()
    data object ActionFailed       : ProfileError()
}

enum class LegalLinkType { PrivacyPolicy, TermsOfUse }
