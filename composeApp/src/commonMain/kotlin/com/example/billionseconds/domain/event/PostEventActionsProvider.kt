package com.example.billionseconds.domain.event

import com.example.billionseconds.domain.event.model.EventMode
import com.example.billionseconds.mvi.event.PostEventAction

object PostEventActionsProvider {

    /**
     * Возвращает список доступных действий по режиму.
     * MVP: Share, Go Home, Open Milestones
     * Video отключён (stub) в MVP.
     */
    fun getActions(mode: EventMode, isVideoAvailable: Boolean): List<PostEventAction> {
        val base = listOf(
            PostEventAction.SHARE,
            PostEventAction.OPEN_MILESTONES,
            PostEventAction.GO_HOME
        )
        return if (isVideoAvailable) {
            listOf(PostEventAction.SHARE, PostEventAction.CREATE_VIDEO) + base.drop(1)
        } else {
            base
        }
    }
}
