package com.example.billionseconds.ui.milestones

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun MilestonesScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "\uD83C\uDFC6", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Достижения",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Здесь появятся ваши достижения\nи памятные вехи на пути\nк миллиарду секунд.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
