package com.example.billionseconds.mvi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BirthdayReducerTest {

    private val initial = BirthdayState()

    @Test
    fun dateChangedUpdatesDateFields() {
        val result = BirthdayReducer.reduce(initial, BirthdayIntent.DateChanged(1990, 6, 15))
        assertEquals(1990, result.year)
        assertEquals(6, result.month)
        assertEquals(15, result.day)
    }

    @Test
    fun dateChangedClearsError() {
        val withError = initial.copy(error = "some error")
        val result = BirthdayReducer.reduce(withError, BirthdayIntent.DateChanged(1990, 1, 1))
        assertNull(result.error)
    }

    @Test
    fun timeChangedUpdatesTimeFields() {
        val result = BirthdayReducer.reduce(initial, BirthdayIntent.TimeChanged(14, 30))
        assertEquals(14, result.hour)
        assertEquals(30, result.minute)
    }

    @Test
    fun clearClickedResetsToInitialState() {
        val modified = BirthdayState(year = 1990, month = 6, day = 15, showResult = true)
        val result = BirthdayReducer.reduce(modified, BirthdayIntent.ClearClicked)
        assertEquals(BirthdayState(), result)
    }

    @Test
    fun calculateClickedClearsError() {
        val withError = initial.copy(error = "some error")
        val result = BirthdayReducer.reduce(withError, BirthdayIntent.CalculateClicked)
        assertNull(result.error)
    }
}
