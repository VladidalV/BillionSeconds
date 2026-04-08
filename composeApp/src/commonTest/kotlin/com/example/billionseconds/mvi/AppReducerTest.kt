package com.example.billionseconds.mvi

import com.example.billionseconds.navigation.AppScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AppReducerTest {

    private val initial = AppState()

    @Test
    fun startClickedNavigatesToOnboardingInput() {
        val result = AppReducer.reduce(initial, AppIntent.StartClicked)
        assertEquals(AppScreen.OnboardingInput, result.screen)
    }

    @Test
    fun startClickedClearsError() {
        val withError = initial.copy(error = "err")
        val result = AppReducer.reduce(withError, AppIntent.StartClicked)
        assertNull(result.error)
    }

    @Test
    fun onboardingDateChangedUpdatesFields() {
        val result = AppReducer.reduce(initial, AppIntent.OnboardingDateChanged(1990, 6, 15))
        assertEquals(1990, result.year)
        assertEquals(6, result.month)
        assertEquals(15, result.day)
    }

    @Test
    fun onboardingDateChangedClearsError() {
        val withError = initial.copy(error = "err")
        val result = AppReducer.reduce(withError, AppIntent.OnboardingDateChanged(1990, 1, 1))
        assertNull(result.error)
    }

    @Test
    fun onboardingTimeChangedUpdatesFields() {
        val result = AppReducer.reduce(initial, AppIntent.OnboardingTimeChanged(14, 30))
        assertEquals(14, result.hour)
        assertEquals(30, result.minute)
    }

    @Test
    fun unknownTimeToggledSetsDefaultTime() {
        val result = AppReducer.reduce(initial, AppIntent.UnknownTimeToggled)
        assertTrue(result.unknownTime)
        assertEquals(12, result.hour)
        assertEquals(0, result.minute)
    }

    @Test
    fun unknownTimeToggledTwiceRestoresFlag() {
        val toggled = AppReducer.reduce(initial, AppIntent.UnknownTimeToggled)
        val restored = AppReducer.reduce(toggled, AppIntent.UnknownTimeToggled)
        assertFalse(restored.unknownTime)
    }

    @Test
    fun onboardingCalculateClickedClearsError() {
        val withError = initial.copy(error = "err")
        val result = AppReducer.reduce(withError, AppIntent.OnboardingCalculateClicked)
        assertNull(result.error)
    }

    @Test
    fun onboardingContinueClickedDoesNotChangeState() {
        val result = AppReducer.reduce(initial, AppIntent.OnboardingContinueClicked)
        assertEquals(initial, result)
    }

    @Test
    fun clearClickedResetsOnboardingFields() {
        val modified = initial.copy(
            year = 1990, month = 6, day = 15,
            unknownTime = true, showMainResult = true
        )
        val result = AppReducer.reduce(modified, AppIntent.ClearClicked)
        assertNull(result.year)
        assertNull(result.month)
        assertNull(result.day)
        assertFalse(result.unknownTime)
        assertFalse(result.showMainResult)
        assertNull(result.error)
    }

    @Test
    fun dateChangedUpdatesFieldsInMain() {
        val result = AppReducer.reduce(initial, AppIntent.DateChanged(1995, 3, 10))
        assertEquals(1995, result.year)
        assertEquals(3, result.month)
        assertEquals(10, result.day)
    }
}
