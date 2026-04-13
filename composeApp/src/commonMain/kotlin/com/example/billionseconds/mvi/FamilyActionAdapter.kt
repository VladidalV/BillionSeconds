package com.example.billionseconds.mvi

import com.example.billionseconds.ui.family.FamilyAction

fun familyAdapter(dispatch: (AppIntent) -> Unit): (FamilyAction) -> Unit = { action ->
    when (action) {
        FamilyAction.ScreenStarted                -> dispatch(AppIntent.FamilyScreenStarted)
        FamilyAction.AddProfileClicked            -> dispatch(AppIntent.AddProfileClicked)
        is FamilyAction.EditProfileClicked        -> dispatch(AppIntent.EditProfileClicked(action.id))
        is FamilyAction.DeleteProfileClicked      -> dispatch(AppIntent.DeleteProfileClicked(action.id))
        is FamilyAction.SetActiveProfileClicked   -> dispatch(AppIntent.SetActiveProfileClicked(action.id))
        FamilyAction.DeleteConfirmed              -> dispatch(AppIntent.DeleteConfirmed)
        FamilyAction.DeleteDismissed              -> dispatch(AppIntent.DeleteDismissed)
        is FamilyAction.FormNameChanged           -> dispatch(AppIntent.FormNameChanged(action.name))
        is FamilyAction.FormRelationTypeChanged   -> dispatch(AppIntent.FormRelationTypeChanged(action.type))
        is FamilyAction.FormCustomRelationChanged -> dispatch(AppIntent.FormCustomRelationChanged(action.name))
        is FamilyAction.FormBirthDateChanged      -> dispatch(AppIntent.FormBirthDateChanged(action.year, action.month, action.day))
        is FamilyAction.FormBirthTimeChanged      -> dispatch(AppIntent.FormBirthTimeChanged(action.hour, action.minute))
        FamilyAction.FormUnknownTimeToggled       -> dispatch(AppIntent.FormUnknownTimeToggled)
        FamilyAction.FormSaveClicked              -> dispatch(AppIntent.FormSaveClicked)
        FamilyAction.FormCancelClicked            -> dispatch(AppIntent.FormCancelClicked)
    }
}
