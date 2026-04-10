package com.example.billionseconds.ui.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.FamilyUiState

@Composable
fun FamilyListContent(
    uiState: FamilyUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.profiles.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "👨‍👩‍👧",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Нет профилей",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Добавьте профиль, чтобы\nотслеживать миллиарды секунд\nчленов семьи",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { onIntent(AppIntent.AddProfileClicked) }) {
                    Text("Добавить профиль")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 96.dp  // space for FAB
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Профили",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(uiState.profiles, key = { it.id }) { profile ->
                    FamilyProfileCard(
                        item = profile,
                        onSetActive = { onIntent(AppIntent.SetActiveProfileClicked(profile.id)) },
                        onEdit = { onIntent(AppIntent.EditProfileClicked(profile.id)) },
                        onDelete = { onIntent(AppIntent.DeleteProfileClicked(profile.id)) }
                    )
                }
                if (uiState.maxProfilesReached) {
                    item {
                        Text(
                            text = "Достигнут максимум профилей (${uiState.profiles.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // FAB
            FloatingActionButton(
                onClick = {
                    if (uiState.canAddMore) onIntent(AppIntent.AddProfileClicked)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = if (uiState.canAddMore)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (uiState.canAddMore)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
