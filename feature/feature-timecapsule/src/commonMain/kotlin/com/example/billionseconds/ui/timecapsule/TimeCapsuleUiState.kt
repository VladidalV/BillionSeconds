package com.example.billionseconds.ui.timecapsule

import com.example.billionseconds.data.model.CapsuleGroup
import com.example.billionseconds.domain.model.TimeCapsule

data class TimeCapsuleUiState(
    val isLoading: Boolean = false,
    val groups: List<CapsuleGroup> = emptyList(),
    val subScreen: TimeCapsuleSubScreen = TimeCapsuleSubScreen.List,
    val formDraft: CapsuleFormDraft? = null,
    val openedCapsule: TimeCapsule? = null,
    val confirmDeleteId: String? = null,
    val error: TimeCapsuleError? = null
)

sealed class TimeCapsuleSubScreen {
    object List   : TimeCapsuleSubScreen()
    object Create : TimeCapsuleSubScreen()
    data class Edit(val id: String) : TimeCapsuleSubScreen()
    data class Open(val id: String) : TimeCapsuleSubScreen()
}

data class CapsuleFormDraft(
    val id: String? = null,
    val title: String = "",
    val message: String = "",
    val recipientProfileId: String? = null,
    val conditionType: ConditionType = ConditionType.DATE,
    val selectedYear: String = "",
    val selectedMonth: String = "",
    val selectedDay: String = "",
    val selectedHour: String = "12",
    val selectedMinute: String = "00",
    val selectedProfileId: String? = null,
    val titleError: String? = null,
    val messageError: String? = null,
    val conditionError: String? = null,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false
)

enum class ConditionType { DATE, BILLION_SECONDS_EVENT }

enum class TimeCapsuleError { LoadFailed, SaveFailed, DeleteFailed }
