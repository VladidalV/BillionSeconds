package com.example.billionseconds.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.navigation.AppScreen
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.ui.countdown.CountdownScreen
import com.example.billionseconds.ui.family.FamilyScreen
import com.example.billionseconds.ui.lifestats.LifeStatsScreen
import com.example.billionseconds.ui.milestones.MilestonesScreen
import com.example.billionseconds.ui.profile.ProfileScreen

@Composable
fun MainScaffold(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab = (state.screen as? AppScreen.Main)?.tab ?: MainTab.Home

    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab -> onIntent(AppIntent.TabSelected(tab)) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                MainTab.Home       -> CountdownScreen(state = state, onIntent = onIntent)
                MainTab.Stats      -> LifeStatsScreen(uiState = state.lifeStats, onIntent = onIntent)
                MainTab.Family     -> FamilyScreen()
                MainTab.Milestones -> MilestonesScreen()
                MainTab.Profile    -> ProfileScreen()
            }
        }
    }
}
