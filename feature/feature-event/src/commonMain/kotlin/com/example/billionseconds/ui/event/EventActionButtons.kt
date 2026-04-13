package com.example.billionseconds.ui.event

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors

@Composable
fun EventActionButtons(
    uiModel: EventUiModel,
    onAction: (EventAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary action — gradient button
        uiModel.primaryAction?.let { action ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (action.isEnabled)
                            Brush.linearGradient(
                                listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                            )
                        else
                            Brush.linearGradient(
                                listOf(
                                    AppColors.buttonGradientStart.copy(alpha = 0.4f),
                                    AppColors.buttonGradientEnd.copy(alpha = 0.4f)
                                )
                            )
                    )
                    .then(
                        if (action.isEnabled)
                            Modifier.clickable { onAction(action.id.toAction()) }
                        else Modifier
                    )
                    .padding(vertical = 18.dp, horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action.label,
                    color = AppColors.buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
            }
        }

        // Secondary actions — outlined style
        uiModel.secondaryActions.forEach { action ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(AppColors.cardDark)
                    .border(
                        width = 1.dp,
                        color = if (action.isEnabled) AppColors.inputBorder else AppColors.inputBorder.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50)
                    )
                    .then(
                        if (action.isEnabled)
                            Modifier.clickable { onAction(action.id.toAction()) }
                        else Modifier
                    )
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = action.label,
                    color = if (action.isEnabled) AppColors.textBody else AppColors.textLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun PostEventAction.toAction(): EventAction = when (this) {
    PostEventAction.SHARE           -> EventAction.ShareClicked
    PostEventAction.CREATE_VIDEO    -> EventAction.CreateVideoClicked
    PostEventAction.OPEN_MILESTONES -> EventAction.OpenMilestonesClicked
    PostEventAction.OPEN_STATS      -> EventAction.OpenStatsClicked
    PostEventAction.GO_HOME         -> EventAction.GoHomeClicked
    PostEventAction.NEXT_MILESTONE  -> EventAction.NextMilestoneClicked
}
