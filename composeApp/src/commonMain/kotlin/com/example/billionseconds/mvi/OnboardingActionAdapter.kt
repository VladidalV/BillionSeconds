package com.example.billionseconds.mvi

import com.example.billionseconds.ui.onboarding.OnboardingAction

fun onboardingAdapter(dispatch: (AppIntent) -> Unit): (OnboardingAction) -> Unit = { action ->
    when (action) {
        OnboardingAction.StartClicked       -> dispatch(AppIntent.StartClicked)
        is OnboardingAction.DateChanged     -> dispatch(AppIntent.OnboardingDateChanged(action.year, action.month, action.day))
        is OnboardingAction.TimeChanged     -> dispatch(AppIntent.OnboardingTimeChanged(action.hour, action.minute))
        OnboardingAction.UnknownTimeToggled -> dispatch(AppIntent.UnknownTimeToggled)
        OnboardingAction.CalculateClicked   -> dispatch(AppIntent.OnboardingCalculateClicked)
        OnboardingAction.ContinueClicked    -> dispatch(AppIntent.OnboardingContinueClicked)
    }
}
