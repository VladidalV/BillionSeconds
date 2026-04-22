# Авторизация — полная документация

## Обзор

Система авторизации построена на принципе **анонимная сессия → опциональный вход через провайдер**.
При первом запуске приложение автоматически создаёт анонимный токен. Пользователь может в любой момент
войти через Google или Apple, что привязывает данные анонимной сессии к постоянному аккаунту (merge).

---

## Структура модулей

```
core/core-domain        — доменные модели (AuthState, AuthUser, ...)
core/core-network       — TokenManager, AuthManager, AuthApi, HttpClientFactory
feature/feature-auth    — UI экрана авторизации
composeApp (MVI)        — AppIntent.Auth.*, AppEffect.*, AppStore, AuthActionAdapter
```

---

## Доменные модели (`core/core-domain`)

### `AuthState`
```
sealed class AuthState
├── Guest(userId: String)       — анонимная сессия (токен есть, провайдер нет)
├── Authenticated(user: AuthUser) — вошёл через Google или Apple
├── Unauthenticated             — нет токена вообще (первый запуск)
├── Loading                     — идёт попытка входа
└── Error(type: AuthErrorType)  — ошибка авторизации
```
Extension-свойства: `isSignedIn`, `isGuest`.

### `AuthUser`
| Поле | Тип | Описание |
|---|---|---|
| `userId` | `String` | ID пользователя с сервера |
| `email` | `String?` | Email (сейчас сервер не возвращает, `null`) |
| `displayName` | `String?` | Имя (только Apple передаёт при первом входе) |
| `provider` | `AuthProvider` | `GOOGLE`, `APPLE`, `ANONYMOUS` |
| `isAnonymous` | `Boolean` | `false` после реального входа |

### `AuthErrorType`
```
sealed class AuthErrorType
├── NetworkError            — нет интернета / таймаут
├── Cancelled               — пользователь закрыл диалог провайдера
├── AccountAlreadyExists    — аккаунт уже привязан к другому устройству
├── SessionExpired          — refresh token истёк, нужно перелогиниться
└── Unknown(message)        — прочие ошибки
```

### `AuthSource`
Откуда был открыт экран авторизации — влияет на навигацию после успешного входа.
```
enum class AuthSource { PROFILE, PREMIUM, SYNC, BACKUP, CLOUD_FEATURE }
```
- `PROFILE` / `PREMIUM` → просто закрыть экран (Back)
- `SYNC` / `BACKUP` / `CLOUD_FEATURE` → закрыть + запустить `syncManager.syncOnStart()`

### `AuthProvider`
```
enum class AuthProvider { GOOGLE, APPLE, ANONYMOUS }
```

---

## Сетевой слой (`core/core-network`)

### `TokenStorage` (интерфейс)
Платформо-зависимое хранилище токенов. Методы: `getAccessToken`, `setAccessToken`,
`getRefreshToken`, `setRefreshToken`, `getUserId`, `setUserId`, `getDeviceId`, `setDeviceId`, `clear`.

| Платформа | Реализация | Хранилище |
|---|---|---|
| Android | `AndroidTokenStorage` | `EncryptedSharedPreferences` |
| iOS | `IosTokenStorage` | **iOS Keychain** (Security framework) |
| Web | `WebTokenStorage` | `localStorage` |

**iOS Keychain детали:** `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` — токены доступны
после первой разблокировки (в т.ч. для фонового sync), не синхронизируются с iCloud.
Автоматическая миграция из NSUserDefaults при первом чтении.

### `TokenManager`
Обёртка над `TokenStorage`. Добавляет mutex для безопасного параллельного обновления токенов.
```kotlin
TokenManager(storage: TokenStorage)
```
Методы: `getAccessToken()`, `getRefreshToken()`, `getUserId()`, `getDeviceId()`, `saveTokens(AuthResponse)`,
`clearTokens()`, `isAuthenticated()`, `withRefreshLock { }`.

### `AuthApi`
HTTP-клиент для auth-эндпоинтов.

| Метод | Эндпоинт | Описание |
|---|---|---|
| `loginAnonymous(deviceId)` | `POST /auth/anonymous` | Создать анонимную сессию |
| `loginWithGoogle(idToken)` | `POST /auth/google` | Войти через Google |
| `loginWithApple(identityToken, name?)` | `POST /auth/apple` | Войти через Apple |
| `refreshToken(refreshToken)` | `POST /auth/refresh` | Обновить access token |
| `mergeAccount(anonymousToken, providerToken)` | `POST /auth/merge` | Привязать провайдер к анонимной сессии |
| `logout()` | `POST /auth/logout` | Отозвать токены на сервере |
| `updateDevice(fcmToken, platform, appVersion, timezone)` | `PUT /me/device` | Обновить данные устройства |

### `AuthManager`
Бизнес-логика авторизации, хранит `StateFlow<AuthState>`.

```kotlin
AuthManager(tokenManager: TokenManager, authApi: AuthApi)
```

| Метод | Описание |
|---|---|
| `signInWithGoogle(idToken)` | Merge (если есть анонимный токен) или прямой вход |
| `signInWithApple(identityToken, name?)` | Аналогично Google |
| `signOut()` | `POST /auth/logout` + очистка токенов + `Unauthenticated` |
| `setGuestState(userId)` | Устанавливает `Guest` стейт (вызывается после anonymous login) |
| `onSessionExpired()` | Устанавливает `Error(SessionExpired)` (вызывается из HTTP-клиента) |

### `HttpClientFactory`
Ktor-клиент с автоматическим обновлением токенов (Bearer auth plugin).

**Логика обновления:**
1. Access token истёк → Ktor вызывает `refreshTokens`
2. Если refresh успешен → сохраняет новые токены, продолжает запрос
3. Если refresh провалился → `tokenManager.clearTokens()` + `onSessionExpired()` callback → UI получает уведомление

Принимает `onSessionExpired: () -> Unit` — проводится из `NetworkModule` через `authManager::onSessionExpired`.

---

## UI слой (`feature/feature-auth`)

### `AuthUiState`
| Поле | Описание |
|---|---|
| `source: AuthSource` | Откуда открыт экран |
| `isGoogleLoading: Boolean` | Показывать индикатор на кнопке Google |
| `isAppleLoading: Boolean` | Показывать индикатор на кнопке Apple |
| `error: AuthErrorType?` | Текущая ошибка (отображается в UI) |

### `AuthAction`
```
sealed class AuthAction
├── SignInWithGoogleClicked
├── SignInWithAppleClicked
├── ContinueAsGuestClicked
└── DismissErrorClicked
```

### `AuthEntryScreen`
Экран авторизации. Показывает:
- Кнопку "Войти через Google" (скрыта на iOS до реализации GIDSignIn)
- Кнопку "Войти через Apple" (только iOS)
- Ссылку "Продолжить без аккаунта"
- Сообщение об ошибке при `error != null`

---

## MVI слой (`composeApp`)

### `AppIntent.Auth.*`
```
AppIntent.Auth
├── ScreenOpened(source: AuthSource)     — открыть экран авторизации
├── SignInWithGoogleClicked               — нажата кнопка Google
├── SignInWithAppleClicked                — нажата кнопка Apple
├── ContinueAsGuestClicked               — "без аккаунта"
├── GoogleTokenReceived(idToken)         — платформа вернула токен Google
├── AppleTokenReceived(identityToken, name?) — платформа вернула токен Apple
├── SignInFailed(error: AuthErrorType)   — ошибка на уровне платформы
├── LogoutClicked                         — нажата кнопка выхода
├── LogoutConfirmed                       — подтверждение диалога выхода
├── DismissError                          — закрыть сообщение об ошибке
└── SessionExpired                        — диспатчится автоматически AppStore при истечении сессии
```

### `AppEffect` (auth-related)
```
AppEffect
├── LaunchGoogleSignIn    — запустить платформенный Google Sign-In SDK
├── LaunchAppleSignIn     — запустить ASAuthorizationController (iOS)
├── AuthSuccess           — вход успешен → snackbar "Добро пожаловать!"
├── DismissAuthScreen     — закрыть экран авторизации
├── ShowLogoutConfirmDialog — (legacy, не используется — заменён confirmDialog в state)
└── SessionExpiredBanner  — сессия истекла → snackbar "Сессия истекла. Войдите снова."
```

### `AuthActionAdapter`
Конвертирует `AuthAction` (feature-auth) в `AppIntent.Auth.*` (appStore).

### Обработка в `AppStore`

**Вход:**
1. `ScreenOpened` → `AuthUiState(source)` + `NavCommand.Forward(AppScreen.AuthEntry)`
2. `SignInWithGoogleClicked` → `emitEffect(LaunchGoogleSignIn)`
3. `LaunchGoogleSignIn` обрабатывается в `App.kt` → вызывает `onLaunchGoogleSignIn(dispatch)`
4. Платформа возвращает `GoogleTokenReceived(idToken)` → `AuthManager.signInWithGoogle()`
5. Успех → `onAuthSuccess(source)` → Back + refresh profile + (sync если нужен) + `AuthSuccess`

**Выход:**
1. `LogoutClicked` → reducer устанавливает `confirmDialog = ProfileConfirmDialog.SignOut`
2. UI показывает `SignOutConfirmDialog` (AlertDialog в ProfileRootContent)
3. Подтверждение → `ConfirmDangerousAction` → `onConfirmDangerousAction()` → `dispatch(LogoutConfirmed)`
4. `LogoutConfirmed` → `AuthManager.signOut()` + refresh profile

**Истечение сессии:**
1. Ktor refresh провалился → `onSessionExpired()` callback → `AuthManager` переходит в `Error(SessionExpired)`
2. `AppStore.init` наблюдает `authManager.authState` → детектирует `SessionExpired` → `dispatch(Auth.SessionExpired)`
3. `onSessionExpired()` → refresh profile + Back (если открыт auth экран) + `SessionExpiredBanner`
4. `App.kt` показывает snackbar "Сессия истекла. Войдите снова."

### Платформенный код (`expect/actual`)

```
expect fun onLaunchGoogleSignIn(dispatch: (AppIntent) -> Unit)
expect fun onLaunchAppleSignIn(dispatch: (AppIntent) -> Unit)
```

| Платформа | Google | Apple |
|---|---|---|
| Android | Credential Manager API (`GetGoogleIdOption`) | `dispatch(SignInFailed(Cancelled))` |
| iOS | **TODO** — stub `Cancelled` | `ASAuthorizationController` + `ASAuthorizationControllerDelegateProtocol` |
| Web | — | — |

---

## Profile screen — auth-related UI

**`AuthAccountSection`** (в `ProfileRootContent`) — блок аккаунта:
- `Authenticated` → показывает email/провайдер + кнопку "Выйти из аккаунта"
- `Guest` / `Unauthenticated` → показывает кнопку "Войти"

**`SignOutConfirmDialog`** (в `ProfileRootContent`) — диалог подтверждения:
- Показывается когда `uiState.confirmDialog == ProfileConfirmDialog.SignOut`
- Кнопки: "Выйти" (красный) / "Отмена"

**`ProfileConfirmDialog.SignOut`** — вариант в sealed class, добавлен рядом с `ResetOnboarding` и `ClearAllData`.

---

## Что нужно от бэкенда

### Эндпоинты

#### `POST /api/v1/auth/anonymous`
Создать анонимную сессию по device_id.

**Request:**
```json
{ "device_id": "string" }
```
**Response:**
```json
{
  "access_token": "string",
  "refresh_token": "string",
  "user_id": "string"
}
```

---

#### `POST /api/v1/auth/google`
Прямой вход через Google (если нет анонимного токена).

**Request:**
```json
{ "id_token": "string" }
```
**Response:** `AuthResponse` (см. выше)

---

#### `POST /api/v1/auth/apple`
Прямой вход через Apple.

**Request:**
```json
{
  "identity_token": "string",
  "name": "string | null"
}
```
**Response:** `AuthResponse`

---

#### `POST /api/v1/auth/merge`
Привязать существующую анонимную сессию к аккаунту Google/Apple. Данные анонимного пользователя
переносятся на постоянный аккаунт.

**Request:**
```json
{
  "anonymous_token": "string",
  "provider_token": "string"
}
```
**Response:** `AuthResponse` с новыми токенами постоянного аккаунта

> Клиент всегда делает merge если есть `anonymous_token`. Бэкенд должен обработать случай,
> когда `provider_token` уже привязан к другому аккаунту — вернуть ошибку, которую клиент
> замапит в `AuthErrorType.AccountAlreadyExists`.

---

#### `POST /api/v1/auth/refresh`
Обновить access token по refresh token.

**Request:**
```json
{ "refresh_token": "string" }
```
**Response:** `AuthResponse`

> Если refresh token истёк или отозван — вернуть `401`. Клиент очистит токены и покажет
> пользователю "Сессия истекла. Войдите снова."

---

#### `POST /api/v1/auth/logout`
Отозвать refresh token на сервере (best-effort — клиент игнорирует ошибки).

**Headers:** `Authorization: Bearer <access_token>`

**Response:** `200 OK` (тело не важно)

---

#### `PUT /api/v1/me/device`
Обновить данные устройства (для push-уведомлений в будущем).

**Request:**
```json
{
  "fcm_token": "string",
  "platform": "android | ios | web",
  "app_version": "string",
  "timezone": "string"
}
```
**Response:** `200 OK`

---

### Что сейчас не хватает от бэкенда

| Проблема | Описание |
|---|---|
| `email` и `displayName` не возвращаются | `AuthResponse` содержит только `access_token`, `refresh_token`, `user_id`. Profile screen показывает "Подключён через google" вместо email. Нужно добавить `email` и `display_name` в `AuthResponse` или в отдельный `GET /api/v1/me`. |
| `AccountAlreadyExists` — нет кода ошибки | Нужен договорённый HTTP статус/код ошибки чтобы клиент мог показать корректное сообщение. |
| Merge — поведение при конфликте | Нужно уточнить: если anonymous_token и provider_token принадлежат разным пользователям, что происходит с данными? |

---

## Внешние настройки (не код)

Подробно в `EXTERNAL_SETUP.md`. Кратко:

| | Статус |
|---|---|
| Google Sign-In Android (Web Client ID) | Нужен реальный ID из Google Cloud Console |
| Google Sign-In iOS (GIDSignIn SDK) | Код не написан — нужен `GoogleService-Info.plist` |
| Apple Sign-In iOS (capability) | Код готов — нужно включить в Xcode + Apple Developer Console |
| iOS Keychain Sharing (capability) | Код готов — нужно добавить в Xcode |
