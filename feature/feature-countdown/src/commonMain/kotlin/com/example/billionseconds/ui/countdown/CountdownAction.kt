package com.example.billionseconds.ui.countdown

sealed class CountdownAction {
    data object ScreenStarted : CountdownAction()
    data object ShareClicked : CountdownAction()
    data object CreateVideoClicked : CountdownAction()
    data object WriteLetterClicked : CountdownAction()
    data object AddFamilyClicked : CountdownAction()
    data object ClearClicked : CountdownAction()
}
