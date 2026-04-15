package com.example.billionseconds.network

import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.TimeCapsuleRepository
import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.network.api.*
import com.example.billionseconds.network.client.createHttpClient
import com.example.billionseconds.network.sync.SyncManager
import com.example.billionseconds.network.token.TokenManager
import com.example.billionseconds.network.token.createTokenStorage

/** Single factory for wiring the network layer. */
fun createSyncManager(
    familyRepository: FamilyProfileRepository,
    settingsRepository: AppSettingsRepository,
    eventHistoryRepository: EventHistoryRepository,
    timeCapsuleRepository: TimeCapsuleRepository,
    birthdayRepository: BirthdayRepository
): SyncManager {
    val tokenStorage = createTokenStorage()
    val tokenManager = TokenManager(tokenStorage)
    val httpClient = createHttpClient(tokenManager)
    return SyncManager(
        tokenManager          = tokenManager,
        authApi               = AuthApi(httpClient),
        syncApi               = SyncApi(httpClient),
        familyRepository      = familyRepository,
        settingsRepository    = settingsRepository,
        eventHistoryRepository = eventHistoryRepository,
        timeCapsuleRepository = timeCapsuleRepository,
        birthdayRepository    = birthdayRepository
    )
}
