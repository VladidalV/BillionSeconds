package com.example.billionseconds.ui.family

import com.example.billionseconds.data.model.RelationType

data class FamilyUiState(
    val isLoading: Boolean                   = true,
    val profiles: List<FamilyProfileUiItem> = emptyList(),
    val activeProfileId: String?             = null,
    val subScreen: FamilySubScreen           = FamilySubScreen.List,
    val formDraft: ProfileFormDraft?         = null,
    val pendingDeleteId: String?             = null,
    val isDeleteConfirmationVisible: Boolean = false,
    val canAddMore: Boolean                  = true,
    val maxProfilesReached: Boolean          = false,
    val error: FamilyError?                  = null
)

data class FamilyProfileUiItem(
    val id: String,
    val name: String,
    val relationLabel: String,
    val relationEmoji: String,
    val birthDateText: String,
    val billionDateText: String,
    val progressText: String,
    val countdownText: String,
    val isActive: Boolean,
    val isPrimary: Boolean,
    val isDeletable: Boolean,
    val isEditable: Boolean,
    val hasApproximateTime: Boolean
)

sealed class FamilySubScreen {
    data object List                            : FamilySubScreen()
    data object CreateForm                      : FamilySubScreen()
    data class  EditForm(val profileId: String) : FamilySubScreen()
}

data class ProfileFormDraft(
    val profileId: String?          = null,
    val name: String                = "",
    val relationType: RelationType  = RelationType.SELF,
    val customRelationName: String  = "",
    val year: Int?                  = null,
    val month: Int?                 = null,
    val day: Int?                   = null,
    val hour: Int                   = 12,
    val minute: Int                 = 0,
    val unknownBirthTime: Boolean   = false,
    val nameError: String?          = null,
    val dateError: String?          = null,
    val isDirty: Boolean            = false
)

sealed class FamilyError {
    data object LoadFailed   : FamilyError()
    data object SaveFailed   : FamilyError()
    data object DeleteFailed : FamilyError()
}
