package com.example.billionseconds.ui.family

import com.example.billionseconds.data.model.RelationType

sealed class FamilyAction {
    data object ScreenStarted : FamilyAction()

    // List actions
    data object AddProfileClicked : FamilyAction()
    data class EditProfileClicked(val id: String) : FamilyAction()
    data class DeleteProfileClicked(val id: String) : FamilyAction()
    data class SetActiveProfileClicked(val id: String) : FamilyAction()

    // Delete confirmation
    data object DeleteConfirmed : FamilyAction()
    data object DeleteDismissed : FamilyAction()

    // Form actions
    data class FormNameChanged(val name: String) : FamilyAction()
    data class FormRelationTypeChanged(val type: RelationType) : FamilyAction()
    data class FormCustomRelationChanged(val name: String) : FamilyAction()
    data class FormBirthDateChanged(val year: Int, val month: Int, val day: Int) : FamilyAction()
    data class FormBirthTimeChanged(val hour: Int, val minute: Int) : FamilyAction()
    data object FormUnknownTimeToggled : FamilyAction()
    data object FormSaveClicked : FamilyAction()
    data object FormCancelClicked : FamilyAction()
}
