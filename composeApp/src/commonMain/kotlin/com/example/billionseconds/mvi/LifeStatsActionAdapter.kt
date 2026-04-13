package com.example.billionseconds.mvi

import com.example.billionseconds.ui.lifestats.LifeStatsAction

fun lifeStatsAdapter(dispatch: (AppIntent) -> Unit): (LifeStatsAction) -> Unit = { action ->
    when (action) {
        LifeStatsAction.ScreenStarted -> dispatch(AppIntent.LifeStatsScreenStarted)
    }
}
