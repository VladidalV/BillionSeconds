package com.example.billionseconds.network.sync

import com.example.billionseconds.data.AppSettingsRepository
import com.example.billionseconds.data.BirthdayRepository
import com.example.billionseconds.data.FamilyProfileRepository
import com.example.billionseconds.data.TimeCapsuleRepository
import com.example.billionseconds.data.event.EventHistoryRecord
import com.example.billionseconds.data.event.EventHistoryRepository
import com.example.billionseconds.data.model.AppSettings
import com.example.billionseconds.data.model.FamilyProfile
import com.example.billionseconds.data.model.RelationType
import com.example.billionseconds.domain.model.TimeCapsule
import com.example.billionseconds.domain.model.UnlockCondition
import com.example.billionseconds.network.api.*
import com.example.billionseconds.network.model.*
import com.example.billionseconds.network.token.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class SyncStatus { IDLE, SYNCING, SUCCESS, ERROR }

class SyncManager(
    private val tokenManager: TokenManager,
    private val authApi: AuthApi,
    private val syncApi: SyncApi,
    private val familyRepository: FamilyProfileRepository,
    private val settingsRepository: AppSettingsRepository,
    private val eventHistoryRepository: EventHistoryRepository,
    private val timeCapsuleRepository: TimeCapsuleRepository,
    private val birthdayRepository: BirthdayRepository
) {
    private val _status = MutableStateFlow(SyncStatus.IDLE)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    /** Main entry point — call on app start from a background coroutine. */
    suspend fun syncOnStart() = withContext(Dispatchers.Default) {
        _status.value = SyncStatus.SYNCING
        try {
            ensureAuthenticated()
            val isFirstSync = !birthdayRepository.isOnboardingCompleted() || familyRepository.loadProfiles().isEmpty()
            if (tokenManager.isAuthenticated() && isFirstSync.not()) {
                // Existing user: pull server state
                pullAndApply()
            } else if (tokenManager.isAuthenticated()) {
                // Newly authenticated user: push local data, then pull
                migrateLocalData()
            }
            _status.value = SyncStatus.SUCCESS
        } catch (_: Exception) {
            _status.value = SyncStatus.ERROR
        }
    }

    private suspend fun ensureAuthenticated() {
        if (tokenManager.isAuthenticated()) return
        val response = authApi.loginAnonymous(AnonymousAuthRequest(tokenManager.getDeviceId()))
        tokenManager.saveTokens(response)
    }

    private suspend fun migrateLocalData() {
        val localProfiles = familyRepository.loadProfiles()
        val settings = settingsRepository.getSettings()
        val activeLocalId = familyRepository.getActiveProfileId()

        val migrateRequest = MigrateRequest(
            profiles = localProfiles.map { it.toMigrateItem() },
            activeProfileLocalId = activeLocalId,
            settings = MigrateSettingsItem(
                onboardingCompleted        = birthdayRepository.isOnboardingCompleted(),
                notificationsEnabled       = settings.notificationsEnabled,
                milestoneRemindersEnabled  = settings.milestoneRemindersEnabled,
                familyRemindersEnabled     = settings.familyRemindersEnabled,
                reengagementEnabled        = settings.reengagementEnabled,
                approximateLabelsEnabled   = settings.approximateLabelsEnabled,
                use24HourFormat            = settings.use24HourFormat
            ),
            eventHistory = localProfiles.mapNotNull { profile ->
                eventHistoryRepository.getRecord(profile.id)?.toMigrateItem(profile.id)
            },
            capsules = timeCapsuleRepository.getAll().map { it.toMigrateItem() }
        )

        val response = syncApi.migrateData(migrateRequest)

        // Build a local-id → server-id map from the migration response
        val idMap = response.idMapping.associate { it.localId to it.serverId }
        applyServerState(
            profiles         = response.profiles,
            activeProfileId  = response.settings?.activeProfileId,
            settings         = response.settings,
            eventHistory     = response.eventHistory,
            capsules         = response.capsules,
            idMap            = idMap
        )
    }

    private suspend fun pullAndApply() {
        val sync = syncApi.getSync()
        applyServerState(
            profiles        = sync.profiles,
            activeProfileId = sync.settings?.activeProfileId,
            settings        = sync.settings,
            eventHistory    = sync.eventHistory,
            capsules        = sync.capsules
        )
    }

    private fun applyServerState(
        profiles: List<ProfileResponse>,
        activeProfileId: String?,
        settings: SettingsResponse?,
        eventHistory: List<EventHistoryResponse>,
        capsules: List<CapsuleResponse>,
        idMap: Map<String, String> = emptyMap()
    ) {
        // Profiles
        val serverProfiles = profiles.map { it.toFamilyProfile() }
        if (serverProfiles.isNotEmpty()) {
            familyRepository.saveProfiles(serverProfiles)
            // Resolve active profile id: may need remapping from local id
            val resolvedActiveId = activeProfileId?.let { idMap[it] ?: it }
            if (resolvedActiveId != null) {
                familyRepository.setActiveProfileId(resolvedActiveId)
            }
        }

        // Settings
        if (settings != null) {
            settingsRepository.saveSettings(settings.toAppSettings())
            if (settings.onboardingCompleted) {
                birthdayRepository.setOnboardingCompleted(true)
            }
        }

        // Event history
        eventHistory.forEach { eh ->
            val profileId = idMap[eh.profileId] ?: eh.profileId
            val existing = eventHistoryRepository.getRecord(profileId)
            val firstShownEpoch = eh.firstShownAt?.isoToEpochSeconds()
            if (firstShownEpoch != null) {
                eventHistoryRepository.saveRecord(
                    EventHistoryRecord(
                        profileId           = profileId,
                        firstShownAt        = firstShownEpoch,
                        celebrationShownAt  = eh.celebrationShownAt?.isoToEpochSeconds()
                            ?: existing?.celebrationShownAt,
                        sharePromptShownAt  = eh.sharePromptShownAt?.isoToEpochSeconds()
                            ?: existing?.sharePromptShownAt
                    )
                )
            }
        }

        // Capsules
        if (capsules.isNotEmpty()) {
            timeCapsuleRepository.clearAll()
            capsules.forEach { cap ->
                timeCapsuleRepository.save(cap.toTimeCapsule(idMap))
            }
        }
    }
}

// ---- Mapping extensions ----

private fun FamilyProfile.toMigrateItem() = MigrateProfileItem(
    localId             = id,
    name                = name,
    relationType        = relationType.name,
    customRelationName  = customRelationName,
    birthYear           = birthYear,
    birthMonth          = birthMonth,
    birthDay            = birthDay,
    birthHour           = birthHour,
    birthMinute         = birthMinute,
    unknownBirthTime    = unknownBirthTime,
    sortOrder           = sortOrder
)

private fun ProfileResponse.toFamilyProfile() = FamilyProfile(
    id                  = id,
    name                = name,
    relationType        = runCatching { RelationType.valueOf(relationType) }.getOrDefault(RelationType.OTHER),
    customRelationName  = customRelationName,
    birthYear           = birthYear,
    birthMonth          = birthMonth,
    birthDay            = birthDay,
    birthHour           = birthHour,
    birthMinute         = birthMinute,
    unknownBirthTime    = unknownBirthTime,
    isPrimary           = isPrimary,
    sortOrder           = sortOrder,
    createdAtEpochSeconds = createdAt.isoToEpochSeconds()
)

private fun EventHistoryRecord.toMigrateItem(profileId: String) = MigrateEventHistoryItem(
    profileLocalId        = profileId,
    firstShownAt          = firstShownAt.epochSecondsToIso(),
    celebrationShownAt    = celebrationShownAt?.epochSecondsToIso(),
    sharePromptShownAt    = sharePromptShownAt?.epochSecondsToIso()
)

private fun TimeCapsule.toMigrateItem() = MigrateCapsuleItem(
    localId                    = id,
    title                      = title,
    message                    = message,
    recipientProfileLocalId    = recipientProfileId,
    isDraft                    = isDraft,
    unlockConditionType        = when (unlockCondition) {
        is UnlockCondition.ExactDateTime      -> "exact_date_time"
        is UnlockCondition.BillionSecondsEvent -> "billion_seconds_event"
    },
    unlockAtEpochMs            = (unlockCondition as? UnlockCondition.ExactDateTime)?.epochMillis,
    unlockProfileLocalId       = (unlockCondition as? UnlockCondition.BillionSecondsEvent)?.profileId
)

private fun CapsuleResponse.toTimeCapsule(idMap: Map<String, String>): TimeCapsule {
    val condition = when (unlockConditionType) {
        "exact_date_time"       -> UnlockCondition.ExactDateTime(unlockAtEpochMs ?: 0L)
        "billion_seconds_event" -> UnlockCondition.BillionSecondsEvent(
            idMap[unlockProfileId ?: ""] ?: unlockProfileId ?: ""
        )
        else -> UnlockCondition.ExactDateTime(unlockAtEpochMs ?: 0L)
    }
    return TimeCapsule(
        id                = id,
        title             = title,
        message           = message,
        recipientProfileId = idMap[recipientProfileId ?: ""] ?: recipientProfileId,
        unlockCondition   = condition,
        createdAt         = createdAt.isoToEpochSeconds() * 1000L,
        openedAt          = openedAt?.isoToEpochSeconds()?.times(1000L),
        isDraft           = isDraft
    )
}

private fun SettingsResponse.toAppSettings() = AppSettings(
    notificationsEnabled      = notificationsEnabled,
    milestoneRemindersEnabled = milestoneRemindersEnabled,
    familyRemindersEnabled    = familyRemindersEnabled,
    reengagementEnabled       = reengagementEnabled,
    approximateLabelsEnabled  = approximateLabelsEnabled,
    use24HourFormat           = use24HourFormat
)

// Minimal ISO-8601 UTC timestamp parsing (YYYY-MM-DDTHH:MM:SS.sssZ)
private fun String.isoToEpochSeconds(): Long {
    return try {
        val clean = trimEnd('Z').replace("T", " ")
        val parts = clean.split(" ")
        val dateParts  = parts[0].split("-")
        val timeParts  = parts.getOrNull(1)?.split(":")
        val year  = dateParts[0].toInt()
        val month = dateParts[1].toInt()
        val day   = dateParts[2].toInt()
        val hour   = timeParts?.getOrNull(0)?.toInt() ?: 0
        val minute = timeParts?.getOrNull(1)?.toInt() ?: 0
        val second = timeParts?.getOrNull(2)?.substringBefore(".")?.toInt() ?: 0
        // Days since Unix epoch
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 9 else month - 3
        val jdn = (365L * y) + (y / 4) - (y / 100) + (y / 400) +
                  ((153L * m + 2) / 5) + day - 719469
        jdn * 86400L + hour * 3600L + minute * 60L + second
    } catch (_: Exception) {
        0L
    }
}

private fun Long.epochSecondsToIso(): String {
    val s = this
    var days = s / 86400L
    val timeOfDay = s % 86400L
    val hour   = (timeOfDay / 3600).toInt()
    val minute = ((timeOfDay % 3600) / 60).toInt()
    val second = (timeOfDay % 60).toInt()
    days += 719469L
    val a = (4 * days + 3) / 146097
    val b = days - (146097 * a) / 4
    val c = (4 * b + 3) / 1461
    val d = b - (1461 * c) / 4
    val m = (5 * d + 2) / 153
    val day   = (d - (153 * m + 2) / 5 + 1).toInt()
    val month = (if (m < 10) m + 3 else m - 9).toInt()
    val year  = (a * 100 + c + if (month <= 2) 1 else 0).toInt()
    fun Int.pad(n: Int) = toString().padStart(n, '0')
    return "${year.pad(4)}-${month.pad(2)}-${day.pad(2)}T${hour.pad(2)}:${minute.pad(2)}:${second.pad(2)}Z"
}
