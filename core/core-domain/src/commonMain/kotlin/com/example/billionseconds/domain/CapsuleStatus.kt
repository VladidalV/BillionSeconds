package com.example.billionseconds.domain

sealed class CapsuleStatus {
    object Draft     : CapsuleStatus()
    object Available : CapsuleStatus()
    object Opened    : CapsuleStatus()
    object Invalid   : CapsuleStatus()
    data class Locked(val unlockAtMs: Long, val remainingLabel: String) : CapsuleStatus()
}
