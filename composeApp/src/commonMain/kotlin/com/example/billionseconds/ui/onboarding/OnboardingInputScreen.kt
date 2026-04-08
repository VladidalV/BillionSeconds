package com.example.billionseconds.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import billionseconds.composeapp.generated.resources.*
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.ui.components.DateInputSection
import com.example.billionseconds.ui.components.TimeInputSection
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingInputScreen(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.enter_birthday),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        DateInputSection(
            year = state.year,
            month = state.month,
            day = state.day,
            onDateChanged = { y, m, d ->
                onIntent(AppIntent.OnboardingDateChanged(y, m, d))
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.unknown_time),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = state.unknownTime,
                onCheckedChange = { onIntent(AppIntent.UnknownTimeToggled) }
            )
        }

        AnimatedVisibility(visible = !state.unknownTime) {
            Column {
                Text(
                    text = stringResource(Res.string.optional_time),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
                TimeInputSection(
                    hour = state.hour,
                    minute = state.minute,
                    onTimeChanged = { h, m ->
                        onIntent(AppIntent.OnboardingTimeChanged(h, m))
                    }
                )
            }
        }

        state.error?.let { err ->
            Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { onIntent(AppIntent.OnboardingCalculateClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.calculate))
        }
    }
}
