package com.example.billionseconds.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.navigation.MainTab
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun BottomBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = 351.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(9999.dp))
                .background(AppColors.navBarGradient)
                .border(1.dp, AppColors.navBarBorder, RoundedCornerShape(9999.dp))
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MainTab.entries.forEach { tab ->
                NavItem(
                    tab = tab,
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) }
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val labelColor = if (selected) AppColors.purpleAccent else AppColors.textBody.copy(alpha = 0.5f)

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Icon
        Text(
            text = tab.navIcon,
            fontSize = 18.sp,
            color = if (selected) AppColors.purpleAccent else AppColors.textBody.copy(alpha = 0.5f)
        )

        // Label
        Text(
            text = tab.navLabel,
            color = labelColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}
