package com.example.billionseconds.domain

import com.example.billionseconds.data.model.CapsuleUiItem
import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.domain.model.TimeCapsule
import com.example.billionseconds.domain.model.UnlockCondition

object CapsuleUiMapper {

    fun toUiItem(
        capsule: TimeCapsule,
        status: CapsuleStatus,
        profiles: List<FamilyProfile>
    ): CapsuleUiItem {
        val recipient = capsule.recipientProfileId?.let { id ->
            profiles.firstOrNull { it.id == id }?.name
        }

        val unlockLabel = when (val cond = capsule.unlockCondition) {
            is UnlockCondition.ExactDateTime -> {
                TimeCapsuleFormatter.formatUnlockLabel(cond.epochMillis)
            }
            is UnlockCondition.BillionSecondsEvent -> {
                val name = profiles.firstOrNull { it.id == cond.profileId }?.name
                TimeCapsuleFormatter.formatUnlockLabel(0L, name)
            }
        }

        return CapsuleUiItem(
            id            = capsule.id,
            title         = capsule.title,
            status        = status,
            recipientName = recipient,
            unlockLabel   = unlockLabel,
            createdLabel  = "Создано ${TimeCapsuleFormatter.formatCreatedDate(capsule.createdAt)}"
        )
    }
}
