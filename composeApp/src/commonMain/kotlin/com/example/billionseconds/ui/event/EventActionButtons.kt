package com.example.billionseconds.ui.event

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.event.EventUiModel
import com.example.billionseconds.mvi.event.PostEventAction

@Composable
fun EventActionButtons(
    uiModel: EventUiModel,
    onIntent: (AppIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Primary action — full-width filled button
        uiModel.primaryAction?.let { action ->
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled  = action.isEnabled,
                onClick  = { onIntent(action.id.toIntent()) }
            ) {
                Text(action.label)
            }
        }

        // Secondary actions — outlined buttons
        uiModel.secondaryActions.forEach { action ->
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled  = action.isEnabled,
                onClick  = { onIntent(action.id.toIntent()) }
            ) {
                Text(action.label)
            }
        }
    }
}

private fun PostEventAction.toIntent(): AppIntent = when (this) {
    PostEventAction.SHARE           -> AppIntent.Event.ShareClicked
    PostEventAction.CREATE_VIDEO    -> AppIntent.Event.CreateVideoClicked
    PostEventAction.OPEN_MILESTONES -> AppIntent.Event.OpenMilestonesClicked
    PostEventAction.OPEN_STATS      -> AppIntent.Event.OpenStatsClicked
    PostEventAction.GO_HOME         -> AppIntent.Event.GoHomeClicked
    PostEventAction.NEXT_MILESTONE  -> AppIntent.Event.NextMilestoneClicked
}
