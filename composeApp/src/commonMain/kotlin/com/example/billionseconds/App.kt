package com.example.billionseconds

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.billionseconds.mvi.BirthdayStore
import com.example.billionseconds.ui.BirthdayScreen

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val store = remember { BirthdayStore(scope) }
    BirthdayScreen(store = store)
}