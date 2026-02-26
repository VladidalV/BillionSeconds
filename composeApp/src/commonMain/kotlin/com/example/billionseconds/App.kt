package com.example.billionseconds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.billionseconds.mvi.BirthdayStore
import com.example.billionseconds.ui.BirthdayScreen

@Composable
fun App() {
    val store = remember { BirthdayStore() }
    BirthdayScreen(store = store)
}