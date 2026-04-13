package com.example.billionseconds.mvi

import com.example.billionseconds.ui.timecapsule.TimeCapsuleAction

fun timeCapsuleAdapter(dispatch: (AppIntent) -> Unit): (TimeCapsuleAction) -> Unit = { action ->
    when (action) {
        TimeCapsuleAction.ScreenStarted                -> dispatch(AppIntent.TimeCapsule.ScreenStarted)
        TimeCapsuleAction.AddClicked                   -> dispatch(AppIntent.TimeCapsule.AddClicked)
        is TimeCapsuleAction.EditClicked               -> dispatch(AppIntent.TimeCapsule.EditClicked(action.id))
        is TimeCapsuleAction.OpenClicked               -> dispatch(AppIntent.TimeCapsule.OpenClicked(action.id))
        is TimeCapsuleAction.DeleteClicked             -> dispatch(AppIntent.TimeCapsule.DeleteClicked(action.id))
        is TimeCapsuleAction.ConfirmDelete             -> dispatch(AppIntent.TimeCapsule.ConfirmDelete(action.id))
        TimeCapsuleAction.CancelDelete                 -> dispatch(AppIntent.TimeCapsule.CancelDelete)
        TimeCapsuleAction.BackClicked                  -> dispatch(AppIntent.TimeCapsule.BackClicked)
        is TimeCapsuleAction.FormTitleChanged          -> dispatch(AppIntent.TimeCapsule.FormTitleChanged(action.value))
        is TimeCapsuleAction.FormMessageChanged        -> dispatch(AppIntent.TimeCapsule.FormMessageChanged(action.value))
        is TimeCapsuleAction.FormConditionTypeChanged  -> dispatch(AppIntent.TimeCapsule.FormConditionTypeChanged(action.type))
        is TimeCapsuleAction.FormDateChanged           -> dispatch(AppIntent.TimeCapsule.FormDateChanged(action.year, action.month, action.day))
        is TimeCapsuleAction.FormTimeChanged           -> dispatch(AppIntent.TimeCapsule.FormTimeChanged(action.hour, action.minute))
        is TimeCapsuleAction.FormRecipientChanged      -> dispatch(AppIntent.TimeCapsule.FormRecipientChanged(action.profileId))
        is TimeCapsuleAction.FormProfileConditionChanged -> dispatch(AppIntent.TimeCapsule.FormProfileConditionChanged(action.profileId))
        TimeCapsuleAction.FormSaveClicked              -> dispatch(AppIntent.TimeCapsule.FormSaveClicked)
        TimeCapsuleAction.FormSaveDraftClicked         -> dispatch(AppIntent.TimeCapsule.FormSaveDraftClicked)
        TimeCapsuleAction.FormCancelClicked            -> dispatch(AppIntent.TimeCapsule.FormCancelClicked)
        TimeCapsuleAction.DiscardDraftConfirmed        -> dispatch(AppIntent.TimeCapsule.DiscardDraftConfirmed)
    }
}
