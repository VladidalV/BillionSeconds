package com.example.billionseconds.network

import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.TimeCapsuleRepository
import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.network.api.*
import com.example.billionseconds.network.auth.AuthManager
import com.example.billionseconds.network.client.createHttpClient
import com.example.billionseconds.network.sync.SyncManager
import com.example.billionseconds.network.token.TokenManager
import com.example.billionseconds.network.token.createTokenStorage

data class NetworkComponents(
    val syncManager: SyncManager,
    val authManager: AuthManager,
)

/** Single factory for wiring the network layer. */
fun createNetworkComponents(
    familyRepository: FamilyProfileRepository,
    settingsRepository: AppSettingsRepository,
    eventHistoryRepository: EventHistoryRepository,
    timeCapsuleRepository: TimeCapsuleRepository,
    birthdayRepository: BirthdayRepository,
): NetworkComponents {
    val tokenStorage  = createTokenStorage()
    val tokenManager  = TokenManager(tokenStorage)
    // onSessionExpired wired after authManager is created (callback captured by reference)
    var onSessionExpiredCallback: () -> Unit = {}
    val httpClient    = createHttpClient(tokenManager) { onSessionExpiredCallback() }
    val authApi       = AuthApi(httpClient)
    val authManager   = AuthManager(tokenManager, authApi)
    onSessionExpiredCallback = authManager::onSessionExpired
    val syncManager   = SyncManager(
        tokenManager           = tokenManager,
        authApi                = authApi,
        syncApi                = SyncApi(httpClient),
        familyRepository       = familyRepository,
        settingsRepository     = settingsRepository,
        eventHistoryRepository = eventHistoryRepository,
        timeCapsuleRepository  = timeCapsuleRepository,
        birthdayRepository     = birthdayRepository,
        onGuestStateReady      = { userId -> authManager.setGuestState(userId) },
    )
    return NetworkComponents(syncManager, authManager)
}
