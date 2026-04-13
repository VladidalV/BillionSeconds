package com.example.billionseconds.mvi

import com.example.billionseconds.ui.countdown.CountdownAction

fun countdownAdapter(dispatch: (AppIntent) -> Unit): (CountdownAction) -> Unit = { action ->
    when (action) {
        CountdownAction.ScreenStarted      -> dispatch(AppIntent.CountdownScreenStarted)
        CountdownAction.ShareClicked       -> dispatch(AppIntent.ShareClicked)
        CountdownAction.CreateVideoClicked -> dispatch(AppIntent.CreateVideoClicked)
        CountdownAction.WriteLetterClicked -> dispatch(AppIntent.WriteLetterClicked)
        CountdownAction.AddFamilyClicked   -> dispatch(AppIntent.AddFamilyClicked)
        CountdownAction.ClearClicked       -> dispatch(AppIntent.ClearClicked)
    }
}
