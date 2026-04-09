package com.example.billionseconds.ui.countdown

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.mvi.CountdownError
import com.example.billionseconds.ui.shared.ComingSoonSheet

@Composable
fun CountdownScreen(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val countdown = state.countdown

    LaunchedEffect(Unit) {
        onIntent(AppIntent.CountdownScreenStarted)
    }

    when {
        countdown.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        countdown.error == CountdownError.NoProfileData ||
        countdown.error == CountdownError.CorruptedData -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Что-то пошло не так", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { onIntent(AppIntent.ClearClicked) }) {
                        Text("Начать заново")
                    }
                }
            }
        }

        else -> {
            CountdownContent(
                state = state,
                onIntent = onIntent,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CountdownContent(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val countdown = state.countdown
    var comingSoonFeature by remember { mutableStateOf<String?>(null) }

    comingSoonFeature?.let { feature ->
        com.example.billionseconds.ui.shared.ComingSoonSheet(
            feature = feature,
            onDismiss = { comingSoonFeature = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        EventBlock(state = countdown)

        HorizontalDivider()

        CountdownBlock(state = countdown)

        ProgressBlock(state = countdown)

        HorizontalDivider()

        ActionsBlock(onIntent = onIntent)

        Spacer(Modifier.height(16.dp))
    }
}
