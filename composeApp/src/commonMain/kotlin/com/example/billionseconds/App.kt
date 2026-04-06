package com.example.billionseconds

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.createBirthdayStorage
import com.example.billionseconds.mvi.BirthdayStore
import com.example.billionseconds.ui.BirthdayScreen
import com.example.billionseconds.ui.ResultScreen

@Composable
fun App() {
    val store = remember {
        BirthdayStore(BirthdayRepository(createBirthdayStorage()))
    }
    DisposableEffect(store) {
        onDispose { store.dispose() }
    }

    val state by store.state.collectAsState()

    MaterialTheme {
        if (state.showResult) {
            ResultScreen(
                state = state,
                onIntent = store::dispatch
            )
        } else {
            BirthdayScreen(
                state = state,
                onIntent = store::dispatch
            )
        }
    }
}
