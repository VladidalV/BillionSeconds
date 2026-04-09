package com.example.billionseconds.ui.main

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.billionseconds.navigation.MainTab

@Composable
fun BottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = { TabIcon(tab, selected = tab == selectedTab) },
                label = { Text(tab.label) },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
private fun TabIcon(tab: MainTab, selected: Boolean) {
    // Placeholder: emoji-иконки до добавления Material Icons зависимости
    val emoji = when (tab) {
        MainTab.Home       -> if (selected) "⏱" else "⏱"
        MainTab.Stats      -> if (selected) "📊" else "📊"
        MainTab.Family     -> if (selected) "👨‍👩‍👧" else "👨‍👩‍👧"
        MainTab.Milestones -> if (selected) "🏆" else "🏆"
        MainTab.Profile    -> if (selected) "👤" else "👤"
    }
    Text(text = emoji)
}
