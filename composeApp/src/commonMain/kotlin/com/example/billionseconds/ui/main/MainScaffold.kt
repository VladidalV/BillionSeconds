package com.example.billionseconds.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.ui.countdown.CountdownScreen
import com.example.billionseconds.ui.family.FamilyScreen
import com.example.billionseconds.ui.lifestats.LifeStatsScreen
import com.example.billionseconds.ui.milestones.MilestonesScreen
import com.example.billionseconds.ui.profile.ProfileScreen

@Composable
fun MainScaffold(
    state: AppState,
    selectedTab: MainTab,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {

    Box(modifier = modifier.fillMaxSize()) {
        // Screen content
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (selectedTab) {
                MainTab.Home       -> CountdownScreen(state = state, onIntent = onIntent)
                MainTab.Stats      -> LifeStatsScreen(uiState = state.lifeStats, onIntent = onIntent)
                MainTab.Family     -> FamilyScreen(uiState = state.family, onIntent = onIntent)
                MainTab.Milestones -> MilestonesScreen(uiState = state.milestones, onIntent = onIntent)
                MainTab.Profile    -> ProfileScreen(uiState = state.profile, onIntent = onIntent)
            }
        }

        // Floating pill nav bar
        BottomBar(
            selectedTab = selectedTab,
            onTabSelected = { tab -> onIntent(AppIntent.TabSelected(tab)) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        )
    }
}
