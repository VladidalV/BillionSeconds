package com.example.billionseconds.domain.event

import com.example.billionseconds.data.event.EventHistoryRecord
import com.example.billionseconds.data.event.EventHistoryRepository
import kotlinx.datetime.Instant

class EventHistoryManager(private val repository: EventHistoryRepository) {

    fun getRecord(profileId: String): EventHistoryRecord? =
        repository.getRecord(profileId)

    /**
     * Фиксирует первый показ экрана для данного профиля.
     * Идемпотентен: не перезаписывает существующую запись.
     */
    fun markSeen(profileId: String, now: Instant) {
        val existing = repository.getRecord(profileId)
        if (existing != null) return // уже зафиксировано
        repository.saveRecord(
            EventHistoryRecord(
                profileId = profileId,
                firstShownAt = now.epochSeconds,
                celebrationShownAt = null,
                sharePromptShownAt = null
            )
        )
    }

    /**
     * Фиксирует факт показа celebration анимации.
     * Идемпотентен: не перезаписывает уже установленный celebrationShownAt.
     */
    fun markCelebrationShown(profileId: String, now: Instant) {
        val existing = repository.getRecord(profileId) ?: return
        if (existing.celebrationShownAt != null) return
        repository.saveRecord(existing.copy(celebrationShownAt = now.epochSeconds))
    }

    /**
     * Фиксирует факт показа share prompt.
     */
    fun markSharePromptShown(profileId: String, now: Instant) {
        val existing = repository.getRecord(profileId) ?: return
        if (existing.sharePromptShownAt != null) return
        repository.saveRecord(existing.copy(sharePromptShownAt = now.epochSeconds))
    }

    fun wasShown(profileId: String): Boolean =
        repository.getRecord(profileId)?.firstShownAt != null

    fun wasCelebrationShown(profileId: String): Boolean =
        repository.getRecord(profileId)?.celebrationShownAt != null
}
