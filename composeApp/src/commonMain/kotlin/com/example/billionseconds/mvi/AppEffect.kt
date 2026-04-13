package com.example.billionseconds.mvi

import com.example.billionseconds.domain.event.EventSharePayload
import com.example.billionseconds.domain.event.model.EventSource

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

    // Event Screen
    data class  NavigateToEventScreen(val profileId: String, val source: EventSource) : AppEffect()
    data object NavigateToShareFromEvent     : AppEffect()
    data object NavigateToMilestonesFromEvent: AppEffect()
    data object NavigateToStatsFromEvent     : AppEffect()
    data object NavigateToHomeFromEvent      : AppEffect()
    data object CloseEventScreen             : AppEffect()
    data class  ShareEventPayload(val payload: EventSharePayload) : AppEffect()
    data class  ShowEventError(val message: String) : AppEffect()
}
