# Интеграция BillionSeconds с BillionSecondsBackend

## Содержание

1. [Обзор архитектуры](#1-обзор-архитектуры)
2. [Модуль core-network](#2-модуль-core-network)
3. [Аутентификация и токены](#3-аутентификация-и-токены)
4. [HTTP-клиент](#4-http-клиент)
5. [API-классы](#5-api-классы)
6. [SyncManager — логика синхронизации](#6-syncmanager--логика-синхронизации)
7. [Сценарий первого запуска (миграция)](#7-сценарий-первого-запуска-миграция)
8. [Сценарий повторного запуска (pull)](#8-сценарий-повторного-запуска-pull)
9. [Применение серверного состояния локально](#9-применение-серверного-состояния-локально)
10. [Интеграция с AppStore](#10-интеграция-с-appstore)
11. [Платформенные реализации TokenStorage](#11-платформенные-реализации-tokenstorage)
12. [Маршруты бэкенда](#12-маршруты-бэкенда)
13. [Структура файлов](#13-структура-файлов)

---

## 1. Обзор архитектуры

Приложение работает по принципу **local-first**: все данные хранятся локально и доступны без сети. Бэкенд подключается в фоне при старте приложения и выполняет одностороннюю синхронизацию.

```
┌─────────────────────────────────────────────┐
│              composeApp (UI)                │
│  App.kt → AppStore → dispatch(SyncCompleted)│
└────────────────────┬────────────────────────┘
                     │ createSyncManager()
┌────────────────────▼────────────────────────┐
│           core-network                      │
│  SyncManager                                │
│    ├── TokenManager  (JWT)                  │
│    ├── HttpClient    (Ktor + Bearer Auth)   │
│    ├── AuthApi                              │
│    └── SyncApi                              │
└────────────────────┬────────────────────────┘
                     │ читает/пишет
┌────────────────────▼────────────────────────┐
│           core-data                         │
│  FamilyProfileRepository                   │
│  AppSettingsRepository                      │
│  EventHistoryRepository                     │
│  TimeCapsuleRepository                      │
│  BirthdayRepository                         │
└─────────────────────────────────────────────┘
```

Бэкенд является источником правды для зарегистрированных пользователей, локальное хранилище — для анонимных и при отсутствии сети.

---

## 2. Модуль core-network

Модуль расположен в `core/core-network` и собирается для всех KMP-таргетов:

| Таргет | Движок Ktor |
|---|---|
| Android | `ktor-client-android` |
| iOS (arm64 + simulator) | `ktor-client-darwin` |
| JS Browser | `ktor-client-js` |
| WASM JS | `ktor-client-js` |

JS и WASM JS разделяют код через промежуточный source set `jsAndWasmSharedMain` — стандартное KMP-имя, которое IDE распознаёт как browser-окружение и корректно резолвит `kotlinx.browser`.

### Конфигурация

```kotlin
// NetworkConfig.kt
object NetworkConfig {
    const val BASE_URL            = "https://api.billionseconds.app"
    const val API_PREFIX          = "/api/v1"
    const val CONNECT_TIMEOUT_MS  = 30_000L
    const val REQUEST_TIMEOUT_MS  = 60_000L
}
```

Итоговый адрес любого эндпоинта: `BASE_URL + API_PREFIX + путь`,  
например: `https://api.billionseconds.app/api/v1/auth/anonymous`.

---

## 3. Аутентификация и токены

Бэкенд использует пару JWT-токенов:

| Токен | TTL | Назначение |
|---|---|---|
| `access_token` | 15 минут | Авторизация запросов (`Authorization: Bearer …`) |
| `refresh_token` | 30 дней | Получение новой пары токенов |

### TokenStorage

Интерфейс `TokenStorage` хранит токены и идентификатор устройства. Реализован отдельно для каждой платформы:

```
TokenStorage (interface, commonMain)
├── AndroidTokenStorage  → SharedPreferences ("bs_token_prefs")
├── IosTokenStorage      → NSUserDefaults
└── WebTokenStorage      → localStorage (jsAndWasmSharedMain)
```

Поля хранилища:

| Ключ | Описание |
|---|---|
| `bs_access_token` | JWT access token |
| `bs_refresh_token` | JWT refresh token |
| `bs_user_id` | UUID пользователя на сервере |
| `bs_device_id` | Уникальный UUID устройства (генерируется один раз при первом запуске) |

`device_id` **не сбрасывается** при вызове `clear()` — он нужен для привязки анонимных сессий к конкретному устройству.

### Device ID

При первом запуске генерируется случайный UUID через `Uuid.random()` (Kotlin 2.0+) и сохраняется навсегда:

```kotlin
fun TokenStorage.getOrCreateDeviceId(): String =
    getDeviceId() ?: Uuid.random().toString().also { setDeviceId(it) }
```

### TokenManager

`TokenManager` — thread-safe обёртка над `TokenStorage`. Использует `Mutex` для предотвращения гонок при параллельном обновлении токенов:

```kotlin
suspend fun <T> withRefreshLock(block: suspend () -> T): T =
    refreshMutex.withLock { block() }
```

---

## 4. HTTP-клиент

`HttpClientFactory.kt` создаёт единственный экземпляр `HttpClient` со следующими плагинами:

### ContentNegotiation
```kotlin
json(Json {
    ignoreUnknownKeys = true   // не падать на новых полях сервера
    isLenient         = true   // терпимость к невалидному JSON
    encodeDefaults    = true   // отправлять поля с дефолтными значениями
})
```

### Bearer Auth (автоматическое обновление токена)

Ktor `Auth` плагин обрабатывает весь цикл обновления прозрачно:

```
Запрос → сервер вернул 401
        ↓
refreshTokens { ... }
  1. Захватить Mutex (только один поток обновляет)
  2. Проверить: может, другой поток уже обновил?
  3. Отправить POST /auth/refresh
  4. Сохранить новые токены
  5. Повторить оригинальный запрос
        ↓
Если refresh не удался → clearTokens(), вернуть null
```

При одновременных запросах, которые получат 401, Mutex гарантирует, что `POST /auth/refresh` выполнится ровно один раз.

### HttpTimeout
- Connect timeout: 30 сек
- Request timeout: 60 сек

---

## 5. API-классы

Каждый API-класс получает `HttpClient` и инкапсулирует HTTP-вызовы к одной группе эндпоинтов. Все пути используют `NetworkConfig.API_PREFIX`.

| Класс | Эндпоинты |
|---|---|
| `AuthApi` | `POST /auth/anonymous`, `/auth/apple`, `/auth/google`, `/auth/refresh`, `/auth/logout`, `/auth/merge`; `PUT /me/device` |
| `ProfilesApi` | `GET /profiles`, `POST /profiles`, `PUT /profiles/:id`, `DELETE /profiles/:id`, `PUT /profiles/active` |
| `SettingsApi` | `GET /settings`, `PATCH /settings` |
| `EventHistoryApi` | `GET /event-history`, `GET /event-history/:profile_id`, `PATCH /event-history/:profile_id` |
| `CapsulesApi` | `GET /capsules`, `POST /capsules`, `PUT /capsules/:id`, `DELETE /capsules/:id`, `POST /capsules/:id/open` |
| `MilestonesApi` | `GET /profiles/:id/milestone-progress`, `PUT /profiles/:id/milestone-progress` |
| `SyncApi` | `GET /sync`, `POST /migrate` |
| `AnalyticsApi` | `POST /analytics/events` |

> **Важно:** `PUT /profiles/active` объявлен в контроллере **до** `PUT /profiles/:id`, чтобы NestJS не поглотил строку `"active"` как UUID-параметр.

---

## 6. SyncManager — логика синхронизации

`SyncManager` — центральный класс модуля. Запускается один раз при старте приложения в фоновом корутине.

### Публичный API

```kotlin
val status: StateFlow<SyncStatus>  // IDLE | SYNCING | SUCCESS | ERROR

suspend fun syncOnStart()          // вызвать при старте в фоне
```

### Алгоритм `syncOnStart()`

```
syncOnStart()
│
├─ ensureAuthenticated()
│    └─ если нет access_token:
│         POST /auth/anonymous  { device_id }
│         → сохранить { access_token, refresh_token, user_id }
│
├─ isFirstSync?
│    = !onboardingCompleted || profiles.isEmpty()
│
├─ isFirstSync == true  →  migrateLocalData()   // первый запуск
└─ isFirstSync == false →  pullAndApply()        // повторный запуск
```

`status` обновляется в `SYNCING` в начале и в `SUCCESS` / `ERROR` по завершении.  
Ошибки сети не крашат приложение — статус переходит в `ERROR`, UI продолжает работать с локальными данными.

---

## 7. Сценарий первого запуска (миграция)

Используется при первом входе пользователя, у которого уже есть локальные данные (onboarding пройден или есть профили).

### Что отправляется

`POST /api/v1/migrate` принимает всё локальное состояние одним запросом:

```json
{
  "profiles": [
    {
      "local_id": "local-uuid-1",
      "name": "Мария",
      "relation_type": "SELF",
      "birth_year": 1992,
      "birth_month": 3,
      "birth_day": 20,
      "birth_hour": 10,
      "birth_minute": 30,
      "unknown_birth_time": false,
      "sort_order": 0
    }
  ],
  "active_profile_local_id": "local-uuid-1",
  "settings": {
    "onboarding_completed": true,
    "notifications_enabled": true
  },
  "event_history": [
    {
      "profile_local_id": "local-uuid-1",
      "first_shown_at": "2025-01-10T09:00:00Z",
      "celebration_shown_at": "2025-01-10T09:05:00Z"
    }
  ],
  "capsules": [
    {
      "local_id": "cap-local-1",
      "title": "Письмо себе",
      "message": "Привет из прошлого!",
      "is_draft": false,
      "unlock_condition_type": "billion_seconds_event",
      "unlock_profile_local_id": "local-uuid-1"
    }
  ],
  "milestone_progress": []
}
```

### Что возвращается — таблица маппинга ID

Сервер создаёт новые UUID для каждого локального объекта и возвращает таблицу соответствия:

```json
{
  "id_mapping": [
    { "local_id": "local-uuid-1", "server_id": "a1b2c3d4-..." },
    { "local_id": "cap-local-1",  "server_id": "e5f6g7h8-..." }
  ],
  "profiles":          [...],
  "settings":          {...},
  "event_history":     [...],
  "capsules":          [...]
}
```

### Как применяется таблица маппинга

После миграции `SyncManager` строит `Map<String, String>` (localId → serverId) и применяет его при конвертации всех вложенных ссылок:

- `active_profile_id` в настройках: `idMap[localId] ?: localId`
- `unlock_profile_id` у капсул: ссылка на профиль тоже переводится в серверный ID
- `recipient_profile_id` у капсул: аналогично

Это гарантирует целостность ссылок после того, как локальные UUID заменяются серверными.

---

## 8. Сценарий повторного запуска (pull)

Если пользователь уже аутентифицирован и данные есть локально:

```
GET /api/v1/sync
→ { server_time, user, settings, profiles, event_history, capsules, milestone_progress }
→ applyServerState(...)
```

Сервер возвращает полный снимок состояния пользователя. Локальные данные полностью заменяются серверными (server wins).

---

## 9. Применение серверного состояния локально

`applyServerState()` работает одинаково для обоих сценариев:

```
profiles    → familyRepository.saveProfiles(...)
              familyRepository.setActiveProfileId(...)

settings    → settingsRepository.saveSettings(...)
              birthdayRepository.setOnboardingCompleted(true)  // если onboarding_completed

eventHistory → для каждой записи:
               - конвертировать ISO timestamp → epochSeconds
               - объединить с существующей локальной записью (не затирать поля, которые сервер не вернул)
               - eventHistoryRepository.saveRecord(...)

capsules    → timeCapsuleRepository.clearAll()
              для каждой капсулы: timeCapsuleRepository.save(...)
```

### Конвертация временных меток

Бэкенд использует ISO 8601 UTC (`2026-04-15T05:10:05.108Z`), локальное хранилище — Unix epoch seconds (`Long`). Конвертация реализована без внешних зависимостей в `SyncManager.kt` через арифметику юлианских дней — это позволяет не тащить date-time библиотеку только ради парсинга формата.

---

## 10. Интеграция с AppStore

### Создание SyncManager

В `App.kt` все репозитории создаются один раз и передаются как в `AppStore`, так и в фабричную функцию `createSyncManager()`:

```kotlin
@Composable
fun App() {
    val store = remember {
        val birthdayRepo     = BirthdayRepository(createBirthdayStorage())
        val familyRepo       = FamilyProfileRepository(createFamilyProfileStorage())
        val settingsRepo     = AppSettingsRepository(createAppSettingsStorage())
        val eventHistoryRepo = EventHistoryRepository(createEventHistoryStorage())
        val timeCapsuleRepo  = TimeCapsuleRepository(createTimeCapsuleStorage())

        val syncManager = createSyncManager(
            familyRepository       = familyRepo,
            settingsRepository     = settingsRepo,
            eventHistoryRepository = eventHistoryRepo,
            timeCapsuleRepository  = timeCapsuleRepo,
            birthdayRepository     = birthdayRepo
        )
        AppStore(..., syncManager = syncManager)
    }
}
```

`createSyncManager()` внутри создаёт `TokenStorage → TokenManager → HttpClient → SyncManager`.

### Фоновая синхронизация в AppStore

`AppStore` принимает `syncManager` как опциональный параметр (значение по умолчанию `null` — для совместимости с тестами):

```kotlin
class AppStore(
    ...,
    private val syncManager: SyncManager? = null
) {
    init {
        // ... обычная инициализация из локального хранилища ...

        if (syncManager != null) {
            scope.launch(Dispatchers.Default) {
                syncManager.syncOnStart()
                dispatch(AppIntent.SyncCompleted)
            }
        }
    }
}
```

Синхронизация не блокирует UI-инициализацию: приложение открывается с локальными данными, а после завершения синхронизации UI пересобирается.

### AppIntent.SyncCompleted

После завершения `syncOnStart()` диспатчится `AppIntent.SyncCompleted`. В `AppStore.onSyncCompleted()` состояние пересобирается заново из локального хранилища (которое к этому моменту уже обновлено серверными данными):

```
syncOnStart() завершился
    ↓
dispatch(AppIntent.SyncCompleted)
    ↓
AppReducer: no-op (состояние не меняется в редьюсере)
    ↓
AppStore.onSyncCompleted():
    getActiveProfileOrFallback()
    BillionSecondsCalculator.computeAll(...)
    _state.update { ... }   ← UI перерисовывается с серверными данными
    startTick(...)
```

---

## 11. Платформенные реализации TokenStorage

| Платформа | Хранилище | Файл |
|---|---|---|
| Android | `SharedPreferences` (`bs_token_prefs`) | `androidMain/…/AndroidTokenStorage.kt` |
| iOS | `NSUserDefaults` (стандартный домен) | `iosMain/…/IosTokenStorage.kt` |
| JS / WASM JS | `localStorage` | `jsAndWasmSharedMain/…/WebTokenStorage.kt` |

Все три реализации подключаются через `expect fun createTokenStorage(): TokenStorage` — стандартный KMP-механизм.

> **Примечание по безопасности:** `NSUserDefaults` и `localStorage` не являются защищёнными хранилищами. Для продакшена рекомендуется перейти на iOS Keychain и Android EncryptedSharedPreferences.

---

## 12. Маршруты бэкенда

Все маршруты доступны по базовому адресу `https://api.billionseconds.app/api/v1`.

### Аутентификация (`/auth`)

| Метод | Путь | Тело | Описание |
|---|---|---|---|
| `POST` | `/auth/anonymous` | `{ device_id }` | Вход без аккаунта |
| `POST` | `/auth/apple` | `{ identity_token, name? }` | Вход через Apple ID |
| `POST` | `/auth/google` | `{ id_token }` | Вход через Google |
| `POST` | `/auth/refresh` | `{ refresh_token }` | Обновление пары токенов |
| `POST` | `/auth/logout` | — | Инвалидация refresh токена |
| `POST` | `/auth/merge` | `{ anonymous_token, provider_token }` | Привязка соц. аккаунта к анонимному |

### Пользователь (`/me`)

| Метод | Путь | Тело | Описание |
|---|---|---|---|
| `PUT` | `/me/device` | `{ fcm_token, platform, app_version, timezone }` | Обновление данных устройства |
| `POST` | `/me/reset` | — | Сброс всех данных пользователя |
| `DELETE` | `/me` | — | Полное удаление аккаунта (GDPR) |
| `GET` | `/me/export` | — | Экспорт данных (GDPR) |

### Профили (`/profiles`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/profiles` | Список всех профилей + active_profile_id |
| `POST` | `/profiles` | Создать профиль (макс. 5) |
| `PUT` | `/profiles/active` | Сменить активный профиль |
| `PUT` | `/profiles/:id` | Обновить профиль |
| `DELETE` | `/profiles/:id` | Удалить профиль (soft delete) |

### Настройки (`/settings`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/settings` | Получить все настройки |
| `PATCH` | `/settings` | Обновить настройки (любые поля, partial) |

### История событий (`/event-history`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/event-history` | Все записи |
| `GET` | `/event-history/:profile_id` | Запись для конкретного профиля |
| `PATCH` | `/event-history/:profile_id` | Обновить временные метки |

### Капсулы времени (`/capsules`)

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/capsules` | Список всех капсул |
| `POST` | `/capsules` | Создать капсулу |
| `PUT` | `/capsules/:id` | Обновить капсулу |
| `DELETE` | `/capsules/:id` | Удалить капсулу |
| `POST` | `/capsules/:id/open` | Открыть капсулу (записывает opened_at) |

### Прогресс по милестоунам

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/profiles/:id/milestone-progress` | Получить прогресс |
| `PUT` | `/profiles/:id/milestone-progress` | Обновить last_seen_reached_id |

Допустимые значения `last_seen_reached_id`: `100m`, `250m`, `500m`, `750m`, `1b`.

### Синхронизация и миграция

| Метод | Путь | Описание |
|---|---|---|
| `GET` | `/sync` | Полный снимок состояния пользователя |
| `POST` | `/migrate` | Загрузить локальные данные на сервер (первый вход) |

### Аналитика (`/analytics`)

| Метод | Путь | Описание |
|---|---|---|
| `POST` | `/analytics/events` | Отправить пакет событий |

---

## 13. Структура файлов

```
core/core-network/
├── build.gradle.kts
└── src/
    ├── commonMain/kotlin/…/network/
    │   ├── NetworkConfig.kt            ← BASE_URL, API_PREFIX, таймауты
    │   ├── NetworkModule.kt            ← createSyncManager() — единая точка сборки
    │   ├── api/
    │   │   ├── AuthApi.kt
    │   │   ├── ProfilesApi.kt
    │   │   ├── SettingsApi.kt
    │   │   ├── EventHistoryApi.kt
    │   │   ├── CapsulesApi.kt
    │   │   ├── MilestonesApi.kt
    │   │   ├── SyncApi.kt
    │   │   └── AnalyticsApi.kt
    │   ├── client/
    │   │   └── HttpClientFactory.kt   ← Ktor + Auth + JSON + Logging
    │   ├── model/                      ← @Serializable DTO для всех эндпоинтов
    │   │   ├── AuthModels.kt
    │   │   ├── ProfileModels.kt
    │   │   ├── SettingsModels.kt
    │   │   ├── EventHistoryModels.kt
    │   │   ├── CapsuleModels.kt
    │   │   ├── MilestoneModels.kt
    │   │   ├── SyncModels.kt
    │   │   └── AnalyticsModels.kt
    │   ├── sync/
    │   │   └── SyncManager.kt         ← основная логика синхронизации
    │   └── token/
    │       ├── TokenStorage.kt        ← interface + expect fun createTokenStorage()
    │       └── TokenManager.kt        ← thread-safe обёртка + refresh lock
    ├── androidMain/…/token/
    │   └── AndroidTokenStorage.kt     ← SharedPreferences
    ├── iosMain/…/token/
    │   └── IosTokenStorage.kt         ← NSUserDefaults
    └── jsAndWasmSharedMain/…/token/
        └── WebTokenStorage.kt         ← localStorage
```
