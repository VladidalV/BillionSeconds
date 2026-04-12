package com.example.billionseconds.data.model

import com.example.billionseconds.domain.CapsuleStatus

data class CapsuleUiItem(
    val id: String,
    val title: String,
    val status: CapsuleStatus,
    val recipientName: String?,
    val unlockLabel: String,
    val createdLabel: String
)

data class CapsuleGroup(
    val header: String,
    val items: List<CapsuleUiItem>
)
