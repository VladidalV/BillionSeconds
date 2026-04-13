package com.example.billionseconds.domain.event

import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.toBirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.domain.event.model.EventDomainModel
import com.example.billionseconds.domain.event.model.EventEligibilityStatus
import com.example.billionseconds.domain.event.model.EventMode
import com.example.billionseconds.domain.event.model.EventSource
import kotlinx.datetime.Instant

class EventScreenDataBuilder(
    private val eligibilityChecker: EventEligibilityChecker,
    private val historyManager: EventHistoryManager
) {

    /**
     * Собирает полную доменную модель для Event Screen.
     * Возвращает null если профиль не позволяет построить данные (защитный случай).
     */
    fun build(profile: FamilyProfile, source: EventSource, now: Instant): EventDomainModel {
        val eligibilityResult = eligibilityChecker.check(profile, now)
        val historyRecord = historyManager.getRecord(profile.id)

        val mode = eligibilityResult.mode ?: EventMode.FIRST_TIME
        val wasShown = historyRecord?.firstShownAt != null
        val celebrationShown = historyRecord?.celebrationShownAt != null
        val sharePromptShown = historyRecord?.sharePromptShownAt != null
        val firstShownAt = historyRecord?.firstShownAt?.let { Instant.fromEpochSeconds(it) }

        return EventDomainModel(
            profileId           = profile.id,
            profileName         = profile.name,
            relationType        = profile.relationType,
            unknownBirthTime    = profile.unknownBirthTime,
            targetDateTime      = eligibilityResult.targetDateTime,
            isReached           = eligibilityResult.status != EventEligibilityStatus.NotReached,
            wasShown            = wasShown,
            celebrationShown    = celebrationShown,
            sharePromptShown    = sharePromptShown,
            firstShownAt        = firstShownAt,
            mode                = mode,
            eligibilityStatus   = eligibilityResult.status,
            source              = source,
            isApproximateMode   = profile.unknownBirthTime
        )
    }
}
