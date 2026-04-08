package com.example.billionseconds.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import billionseconds.composeapp.generated.resources.*
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.AppState
import com.example.billionseconds.ui.components.MilestoneProgressBar
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnboardingResultScreen(
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
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isMilestoneReached) {
            OnboardingCelebrationContent()
        } else {
            OnboardingMilestoneContent(state)
        }

        Spacer(Modifier.height(32.dp))

        MilestoneProgressBar(
            progress = state.progressPercent,
            label = stringResource(Res.string.onboarding_progress_label)
        )

        if (state.unknownTime) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.onboarding_approximate_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { onIntent(AppIntent.OnboardingContinueClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.onboarding_continue))
        }
    }
}

@Composable
private fun OnboardingMilestoneContent(state: AppState) {
    val milestone = state.milestoneInstant ?: return
    val local = milestone.toLocalDateTime(TimeZone.currentSystemDefault())

    Text(
        text = stringResource(Res.string.your_milestone),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = "${local.dayOfMonth.twoDigits()}.${local.monthNumber.twoDigits()}.${local.year}",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "${local.hour.twoDigits()}:${local.minute.twoDigits()}",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun OnboardingCelebrationContent() {
    Text(
        text = "🎉",
        style = MaterialTheme.typography.displayLarge,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = stringResource(Res.string.congratulations),
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(12.dp))

    Text(
        text = stringResource(Res.string.milestone_reached),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
