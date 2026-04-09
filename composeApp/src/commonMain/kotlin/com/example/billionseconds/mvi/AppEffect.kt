package com.example.billionseconds.mvi

sealed class AppEffect {
    data object ExitApp                            : AppEffect()
    data class  ShareText(val text: String)        : AppEffect()
    data class  ShowComingSoon(val feature: String): AppEffect()
    data class  ShowError(val message: String)     : AppEffect()
}
