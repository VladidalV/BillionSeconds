package com.example.billionseconds.domain.event

import com.example.billionseconds.domain.event.model.EventDomainModel
import com.example.billionseconds.domain.event.model.EventMode
import com.example.billionseconds.ui.event.EventUiModel
import com.example.billionseconds.ui.event.PostEventAction
import com.example.billionseconds.ui.event.PostEventActionUi
import kotlinx.datetime.Instant

object EventUiMapper {

    fun toUiModel(domain: EventDomainModel, now: Instant): EventUiModel {
        val actions = PostEventActionsProvider.getActions(domain.mode, isVideoAvailable = false)
        val primaryAction = actions.firstOrNull()?.let {
            PostEventActionUi(id = it, label = actionLabel(it), isEnabled = true)
        }
        val secondaryActions = actions.drop(1).map {
            PostEventActionUi(id = it, label = actionLabel(it), isEnabled = true)
        }

        val firstShownAt = domain.firstShownAt
        val repeatModeNote = if (domain.mode == EventMode.REPEAT && firstShownAt != null) {
            EventFormatter.formatRepeatModeNote(firstShownAt, now)
        } else null

        return EventUiModel(
            title                    = EventFormatter.formatTitle(domain.mode, domain.relationType, domain.profileName),
            subtitle                 = EventFormatter.formatSubtitle(),
            eventDateText            = EventFormatter.formatEventDate(domain.targetDateTime),
            profileLabel             = EventFormatter.formatProfileLabel(
                name         = domain.profileName,
                relationType = domain.relationType,
                customName   = null
            ),
            reachedText              = EventFormatter.formatReachedText(
                mode           = domain.mode,
                firstShownAt   = domain.firstShownAt,
                targetDateTime = domain.targetDateTime,
                now            = now
            ),
            isApproximateLabelVisible = domain.isApproximateMode,
            approximateLabel         = EventFormatter.formatApproximateLabel(),
            isCelebrationEnabled     = domain.mode == EventMode.FIRST_TIME && !domain.celebrationShown,
            primaryAction            = primaryAction,
            secondaryActions         = secondaryActions,
            repeatModeNote           = repeatModeNote
        )
    }

    private fun actionLabel(action: PostEventAction): String = when (action) {
        PostEventAction.SHARE           -> "Поделиться"
        PostEventAction.CREATE_VIDEO    -> "Создать видео"
        PostEventAction.OPEN_MILESTONES -> "Мои вехи"
        PostEventAction.OPEN_STATS      -> "Статистика жизни"
        PostEventAction.GO_HOME         -> "На главную"
        PostEventAction.NEXT_MILESTONE  -> "Следующая веха"
    }
}
