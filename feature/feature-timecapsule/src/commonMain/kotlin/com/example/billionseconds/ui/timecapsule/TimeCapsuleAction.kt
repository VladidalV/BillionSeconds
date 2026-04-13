package com.example.billionseconds.ui.timecapsule

sealed class TimeCapsuleAction {
    data object ScreenStarted                                                  : TimeCapsuleAction()
    data object AddClicked                                                     : TimeCapsuleAction()
    data class  EditClicked(val id: String)                                    : TimeCapsuleAction()
    data class  OpenClicked(val id: String)                                    : TimeCapsuleAction()
    data class  DeleteClicked(val id: String)                                  : TimeCapsuleAction()
    data class  ConfirmDelete(val id: String)                                  : TimeCapsuleAction()
    data object CancelDelete                                                   : TimeCapsuleAction()
    data object BackClicked                                                    : TimeCapsuleAction()
    data class  FormTitleChanged(val value: String)                            : TimeCapsuleAction()
    data class  FormMessageChanged(val value: String)                          : TimeCapsuleAction()
    data class  FormConditionTypeChanged(val type: ConditionType)              : TimeCapsuleAction()
    data class  FormDateChanged(val year: String, val month: String, val day: String) : TimeCapsuleAction()
    data class  FormTimeChanged(val hour: String, val minute: String)          : TimeCapsuleAction()
    data class  FormRecipientChanged(val profileId: String?)                   : TimeCapsuleAction()
    data class  FormProfileConditionChanged(val profileId: String)             : TimeCapsuleAction()
    data object FormSaveClicked                                                : TimeCapsuleAction()
    data object FormSaveDraftClicked                                           : TimeCapsuleAction()
    data object FormCancelClicked                                              : TimeCapsuleAction()
    data object DiscardDraftConfirmed                                          : TimeCapsuleAction()
}
