package com.example.billionseconds.domain

import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.toBirthdayData
import kotlinx.datetime.Instant

object FamilyProfileCalculator {

    data class Result(
        val milestoneInstant: Instant,
        val progressPercent: Float,
        val isMilestoneReached: Boolean,
        val secondsRemaining: Long
    )

    fun compute(profile: FamilyProfile, now: Instant): Result {
        val result = BillionSecondsCalculator.computeAll(profile.toBirthdayData(), now)
        return Result(
            milestoneInstant  = result.milestoneInstant,
            progressPercent   = result.progressPercent,
            isMilestoneReached = result.isMilestoneReached,
            secondsRemaining  = result.secondsRemaining
        )
    }
}
