package com.example.billionseconds.domain

import com.example.billionseconds.data.model.BirthdayData
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.plus

object MilestonesCalculator {

    data class Result(
        val milestones: List<UserMilestone>,
        val nextMilestoneId: String?,
        val lastReachedId: String?,
        val newlyReachedId: String?
    )

    fun compute(
        birthData: BirthdayData,
        now: Instant,
        isUnknownTime: Boolean,
        lastSeenReachedId: String?
    ): Result {
        val birthInstant = BillionSecondsCalculator.birthInstantFrom(birthData)
        val definitions  = MilestoneConfig.definitions.sortedBy { it.secondsThreshold }

        val milestones = mutableListOf<UserMilestone>()
        var nextAssigned = false

        for (i in definitions.indices) {
            val def = definitions[i]
            val targetInstant = birthInstant.plus(def.secondsThreshold, DateTimeUnit.SECOND)

            if (now.epochSeconds >= targetInstant.epochSeconds) {
                milestones += UserMilestone(
                    definition    = def,
                    targetInstant = targetInstant,
                    status        = MilestoneStatus.Reached(targetInstant),
                    reachedAt     = targetInstant
                )
            } else if (!nextAssigned) {
                nextAssigned = true
                val prevThreshold = if (i == 0) 0L else definitions[i - 1].secondsThreshold
                val elapsed = now.epochSeconds - birthInstant.epochSeconds - prevThreshold
                val range   = def.secondsThreshold - prevThreshold
                val progress = (elapsed.toFloat() / range.toFloat()).coerceIn(0f, 1f)
                val remaining = targetInstant.epochSeconds - now.epochSeconds

                milestones += UserMilestone(
                    definition      = def,
                    targetInstant   = targetInstant,
                    status          = MilestoneStatus.Next,
                    progressFromPrev = progress,
                    secondsRemaining = remaining
                )
            } else {
                milestones += UserMilestone(
                    definition    = def,
                    targetInstant = targetInstant,
                    status        = MilestoneStatus.Upcoming
                )
            }
        }

        val reachedList  = milestones.filter { it.status is MilestoneStatus.Reached }
        val lastReachedId = reachedList.lastOrNull()?.definition?.id
        val nextMilestoneId = milestones.firstOrNull { it.status is MilestoneStatus.Next }?.definition?.id

        val newlyReachedId = if (lastReachedId != null && lastReachedId != lastSeenReachedId) {
            lastReachedId
        } else {
            null
        }

        return Result(
            milestones      = milestones,
            nextMilestoneId = nextMilestoneId,
            lastReachedId   = lastReachedId,
            newlyReachedId  = newlyReachedId
        )
    }
}
