package com.example.billionseconds.mvi

sealed class AppEffect {
    data object ExitApp                                              : AppEffect()
    data class  ShareText(val text: String)                         : AppEffect()
    data class  ShowComingSoon(val feature: String)                  : AppEffect()
    data class  ShowError(val message: String)                       : AppEffect()
    data class  ShowMilestoneCelebration(val milestoneId: String)         : AppEffect()
    data class  ShareMilestone(val milestoneId: String, val text: String) : AppEffect()
    data class  ActiveProfileChanged(val profileId: String)               : AppEffect()
    data class  ShowFamilyError(val message: String)                      : AppEffect()
    data object NavigateToFamily                                          : AppEffect()
    data class  LaunchExternalUrl(val url: String)                        : AppEffect()
    data class  ShowProfileError(val message: String)                     : AppEffect()
    data object OnboardingReset                                           : AppEffect()
}
