package com.example.billionseconds.domain

import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.toBirthdayData
import com.example.billionseconds.domain.model.TimeCapsule
import com.example.billionseconds.domain.model.UnlockCondition

object CapsuleUnlockResolver {

    fun resolve(
        capsule: TimeCapsule,
        nowMs: Long,
        profiles: List<FamilyProfile>
    ): CapsuleStatus {
        if (capsule.isDraft) return CapsuleStatus.Draft
        if (capsule.openedAt != null) return CapsuleStatus.Opened

        return when (val condition = capsule.unlockCondition) {
            is UnlockCondition.ExactDateTime -> {
                if (condition.epochMillis <= nowMs) {
                    CapsuleStatus.Available
                } else {
                    val remaining = TimeCapsuleFormatter.formatRemainingTime(condition.epochMillis - nowMs)
                    CapsuleStatus.Locked(condition.epochMillis, remaining)
                }
            }
            is UnlockCondition.BillionSecondsEvent -> {
                val profile = profiles.firstOrNull { it.id == condition.profileId }
                    ?: return CapsuleStatus.Invalid

                val birthData = profile.toBirthdayData()
                val birthMs = BillionSecondsCalculator.birthEpochMs(birthData)
                val billionMs = birthMs + BillionSecondsCalculator.BILLION * 1000L

                if (billionMs <= nowMs) {
                    CapsuleStatus.Available
                } else {
                    val remaining = TimeCapsuleFormatter.formatRemainingTime(billionMs - nowMs)
                    CapsuleStatus.Locked(billionMs, remaining)
                }
            }
        }
    }
}
