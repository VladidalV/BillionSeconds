package com.example.billionseconds.mvi

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BirthdayReducerTest {

    @Test
    fun `DateSelected should update birthDate and enable calculation if time is set`() {
        val initialState = BirthdayState(
            birthTime = LocalTime(12, 0)
        )
        val date = LocalDate(2020, 1, 1)
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.DateSelected(date))
        
        assertEquals(date, result.birthDate)
        assertEquals(LocalTime(12, 0), result.birthTime)
        assertTrue(result.isCalculateEnabled)
        assertNull(result.errorMessage)
    }

    @Test
    fun `DateSelected should update birthDate but not enable calculation if time is not set`() {
        val initialState = BirthdayState()
        val date = LocalDate(2020, 1, 1)
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.DateSelected(date))
        
        assertEquals(date, result.birthDate)
        assertNull(result.birthTime)
        assertFalse(result.isCalculateEnabled)
        assertNull(result.errorMessage)
    }

    @Test
    fun `TimeSelected should update birthTime and enable calculation if date is set`() {
        val initialState = BirthdayState(
            birthDate = LocalDate(2020, 1, 1)
        )
        val time = LocalTime(12, 0)
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.TimeSelected(time))
        
        assertEquals(LocalDate(2020, 1, 1), result.birthDate)
        assertEquals(time, result.birthTime)
        assertTrue(result.isCalculateEnabled)
        assertNull(result.errorMessage)
    }

    @Test
    fun `TimeSelected should update birthTime but not enable calculation if date is not set`() {
        val initialState = BirthdayState()
        val time = LocalTime(12, 0)
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.TimeSelected(time))
        
        assertNull(result.birthDate)
        assertEquals(time, result.birthTime)
        assertFalse(result.isCalculateEnabled)
        assertNull(result.errorMessage)
    }

    @Test
    fun `CalculateClicked should set isLoading when both date and time are set`() {
        val initialState = BirthdayState(
            birthDate = LocalDate(2020, 1, 1),
            birthTime = LocalTime(12, 0)
        )
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.CalculateClicked)
        
        assertTrue(result.isLoading)
        assertNull(result.errorMessage)
    }

    @Test
    fun `CalculateClicked should set errorMessage when date is not set`() {
        val initialState = BirthdayState(
            birthTime = LocalTime(12, 0)
        )
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.CalculateClicked)
        
        assertEquals("Please select both date and time", result.errorMessage)
        assertFalse(result.isLoading)
    }

    @Test
    fun `CalculateClicked should set errorMessage when time is not set`() {
        val initialState = BirthdayState(
            birthDate = LocalDate(2020, 1, 1)
        )
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.CalculateClicked)
        
        assertEquals("Please select both date and time", result.errorMessage)
        assertFalse(result.isLoading)
    }

    @Test
    fun `ClearResult should reset all state fields`() {
        val initialState = BirthdayState(
            birthDate = LocalDate(2020, 1, 1),
            birthTime = LocalTime(12, 0),
            billionSecondsInstant = kotlinx.datetime.Clock.System.now(),
            resultText = "Some result",
            isCalculateEnabled = true,
            isLoading = true,
            errorMessage = "Some error"
        )
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.ClearResult)
        
        assertNull(result.birthDate)
        assertNull(result.birthTime)
        assertNull(result.billionSecondsInstant)
        assertEquals("", result.resultText)
        assertFalse(result.isCalculateEnabled)
        assertFalse(result.isLoading)
        assertNull(result.errorMessage)
    }

    @Test
    fun `ClearResult should clear previous errorMessage`() {
        val initialState = BirthdayState(
            birthDate = LocalDate(2020, 1, 1),
            birthTime = LocalTime(12, 0),
            errorMessage = "Previous error"
        )
        
        val result = BirthdayReducer.reduce(initialState, BirthdayIntent.ClearResult)
        
        assertNull(result.errorMessage)
    }
}