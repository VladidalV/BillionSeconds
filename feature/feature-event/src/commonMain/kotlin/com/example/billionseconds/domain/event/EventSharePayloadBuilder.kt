package com.example.billionseconds.domain.event

import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.RelationType
import kotlinx.datetime.Instant

data class EventSharePayload(
    val text: String,
    val imageUri: String? = null
)

object EventSharePayloadBuilder {

    fun build(profile: FamilyProfile, targetDateTime: Instant): EventSharePayload {
        val dateText = EventFormatter.formatEventDate(targetDateTime)
        val text = when (profile.relationType) {
            RelationType.SELF ->
                "Я достиг миллиарда секунд жизни! \uD83C\uDF89\n$dateText\n#BillionSeconds"
            RelationType.CHILD ->
                "${profile.name} достиг(ла) миллиарда секунд! \uD83C\uDF89\n$dateText\n#BillionSeconds"
            else ->
                "${profile.name}: миллиард секунд! \uD83C\uDF89\n$dateText\n#BillionSeconds"
        }
        return EventSharePayload(text = text, imageUri = null)
    }
}
