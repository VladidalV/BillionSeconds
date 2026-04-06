package com.example.billionseconds.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import billionseconds.composeapp.generated.resources.*
import com.example.billionseconds.mvi.BirthdayIntent
import com.example.billionseconds.mvi.BirthdayState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun ResultScreen(
    state: BirthdayState,
    onIntent: (BirthdayIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isMilestoneReached) {
            CelebrationContent()
        } else {
            CountdownContent(state)
        }

        Spacer(Modifier.height(40.dp))

        OutlinedButton(
            onClick = { onIntent(BirthdayIntent.ClearClicked) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.change_date))
        }
    }
}

@Composable
private fun CountdownContent(state: BirthdayState) {
    val milestone = state.milestoneInstant ?: return
    val local = milestone.toLocalDateTime(TimeZone.currentSystemDefault())

    Text(
        text = stringResource(Res.string.your_milestone),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(16.dp))

    Text(
        text = "${local.dayOfMonth}.${local.monthNumber.twoDigits()}.${local.year}  ${local.hour.twoDigits()}:${local.minute.twoDigits()}",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(32.dp))

    Text(
        text = stringResource(Res.string.time_remaining),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = formatSeconds(state.secondsRemaining),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    Spacer(Modifier.height(4.dp))

    Text(
        text = stringResource(Res.string.seconds_left),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CelebrationContent() {
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

private fun formatSeconds(seconds: Long): String {
    val days = seconds / 86_400
    val hours = (seconds % 86_400) / 3_600
    val minutes = (seconds % 3_600) / 60
    val secs = seconds % 60
    return if (days > 0) {
        "${days}д ${hours.twoDigits()}:${minutes.twoDigits()}:${secs.twoDigits()}"
    } else {
        "${hours.twoDigits()}:${minutes.twoDigits()}:${secs.twoDigits()}"
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
private fun Long.twoDigits(): String = toString().padStart(2, '0')
