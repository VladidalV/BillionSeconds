package com.example.billionseconds.domain.event

import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.toBirthdayData
import com.example.billionseconds.domain.BillionSecondsCalculator
import com.example.billionseconds.domain.event.model.EventEligibilityStatus
import com.example.billionseconds.domain.event.model.EventMode
import kotlinx.datetime.Instant

data class EventEligibilityResult(
    val status: EventEligibilityStatus,
    val mode: EventMode?,
    val targetDateTime: Instant
)

class EventEligibilityChecker(
    private val historyRepository: EventHistoryRepository
) {

    /**
     * Определяет, может ли Event Screen быть показан для данного профиля.
     *
     * - NotReached: событие ещё не наступило
     * - EligibleFirstTime: событие достигнуто, экран ещё не показывался
     * - EligibleRepeat: событие достигнуто, экран уже показывался
     */
    fun check(profile: FamilyProfile, now: Instant): EventEligibilityResult {
        val birthData = profile.toBirthdayData()
        val result = BillionSecondsCalculator.computeAll(birthData, now)
        val targetDateTime = result.milestoneInstant

        if (!result.isMilestoneReached) {
            return EventEligibilityResult(
                status = EventEligibilityStatus.NotReached,
                mode = null,
                targetDateTime = targetDateTime
            )
        }

        val record = historyRepository.getRecord(profile.id)
        val wasShown = record?.firstShownAt != null

        return if (!wasShown) {
            EventEligibilityResult(
                status = EventEligibilityStatus.EligibleFirstTime,
                mode = EventMode.FIRST_TIME,
                targetDateTime = targetDateTime
            )
        } else {
            EventEligibilityResult(
                status = EventEligibilityStatus.EligibleRepeat,
                mode = EventMode.REPEAT,
                targetDateTime = targetDateTime
            )
        }
    }
}
