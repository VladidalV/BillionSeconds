# ТЗ на Backend — BillionSeconds

## Контекст

Приложение BillionSeconds считает, когда пользователь проживёт 1 миллиард секунд с момента рождения (≈31.7 лет), 
показывает статистику жизни, вехи на пути к событию, профили семьи, капсулы времени и celebration-экран в момент достижения. 
Сейчас все данные хранятся локально (SharedPreferences / NSUserDefaults / localStorage). 
Бэкенд создаётся с нуля — всё, что сейчас хранится локально, переезжает на сервер.

---

## 1. Общая архитектура

### Стек (рекомендуемый)
- **Runtime:** Node.js (NestJS) или Go — на усмотрение команды
- **БД:** PostgreSQL — основное хранилище
- **Auth:** JWT (access + refresh) + Apple Sign-In / Google Sign-In / анонимная авторизация
- **Push:** Firebase Cloud Messaging (FCM) для Android, APNs для iOS
- **Хранилище файлов:** S3-совместимое (для будущего медиа-контента)
- **API:** REST + JSON, versioned (`/api/v1/...`)

### Принципы
- Все временны́е метки — UTC, формат ISO 8601 или Unix epoch (seconds)
- Все ID — UUID v4
- Все вычисления (прогресс, статус вехи, статус капсулы) — на клиенте; бэкенд хранит сырые данные
- Мягкое удаление (`deleted_at`) для профилей и капсул

---

## 2. Аутентификация и пользователи

### 2.1 Таблица users

```
users
─────────────────────────────────────────────
id                  UUID, PK
created_at          TIMESTAMPTZ
updated_at          TIMESTAMPTZ
deleted_at          TIMESTAMPTZ (soft delete)
anonymous_id        VARCHAR (для анонимных пользователей)
email               VARCHAR UNIQUE NULLABLE
display_name        VARCHAR NULLABLE
auth_provider       ENUM: 'anonymous' | 'apple' | 'google' | 'email'
auth_provider_id    VARCHAR NULLABLE (sub из токена провайдера)
fcm_token           VARCHAR NULLABLE (токен устройства для push)
platform            ENUM: 'android' | 'ios' | 'web'
app_version         VARCHAR
timezone            VARCHAR (IANA, например 'Europe/Moscow')
locale              VARCHAR (например 'ru', 'en')
```

### 2.2 Эндпоинты авторизации

```
POST /api/v1/auth/anonymous
  Body:    { device_id: string }
  Response: { access_token, refresh_token, user_id }

POST /api/v1/auth/apple
  Body:    { identity_token: string, name?: string }
  Response: { access_token, refresh_token, user_id }

POST /api/v1/auth/google
  Body:    { id_token: string }
  Response: { access_token, refresh_token, user_id }

POST /api/v1/auth/refresh
  Body:    { refresh_token: string }
  Response: { access_token, refresh_token }

POST /api/v1/auth/logout
  Headers: Authorization: Bearer <token>
  Response: 204

POST /api/v1/auth/merge
  // Слияние анонимного аккаунта с провайдером (Apple/Google).
  // Данные анонимного аккаунта сохраняются, аккаунты объединяются.
  Body:    { anonymous_token: string, provider_token: string }
  Response: { access_token, refresh_token, user_id }
```

### 2.3 Обновление токена устройства

```
PUT /api/v1/me/device
  Body:    { fcm_token: string, platform: string, app_version: string, timezone: string }
  Response: 204
```

---

## 3. Профили (FamilyProfile)

Центральная сущность. У одного пользователя — до 5 профилей. Один из них — primary (сам пользователь, relationType = SELF).

### 3.1 Таблица profiles

```
profiles
─────────────────────────────────────────────
id                    UUID, PK
user_id               UUID, FK → users.id
created_at            TIMESTAMPTZ
updated_at            TIMESTAMPTZ
deleted_at            TIMESTAMPTZ (soft delete)

name                  VARCHAR(80) NOT NULL
relation_type         ENUM: 'SELF' | 'CHILD' | 'PARTNER' | 'MOTHER' | 'FATHER' | 'SIBLING' | 'OTHER'
custom_relation_name  VARCHAR(80) NULLABLE
  // Заполняется только если relation_type = 'OTHER'

birth_year            SMALLINT NOT NULL
birth_month           SMALLINT NOT NULL   (1–12)
birth_day             SMALLINT NOT NULL   (1–31)
birth_hour            SMALLINT DEFAULT 12 (0–23)
birth_minute          SMALLINT DEFAULT 0  (0–59)
unknown_birth_time    BOOLEAN DEFAULT false
  // Если true — клиент использует 12:00 для расчётов и помечает результат как приблизительный

is_primary            BOOLEAN DEFAULT false
  // Ровно один primary на пользователя (SELF-профиль, создаётся при онбординге)

sort_order            SMALLINT DEFAULT 0
```

**Ограничения:**
- Ровно один `is_primary = true` на пользователя
- Не более 5 активных (не soft-deleted) профилей на пользователя
- Primary-профиль нельзя удалять
- Нельзя удалять последний оставшийся профиль

### 3.2 Эндпоинты

```
GET /api/v1/profiles
  Response: { profiles: Profile[], active_profile_id: string | null }

POST /api/v1/profiles
  Body:    ProfileWriteBody
  Response: Profile (201)
  Errors:  422 MAX_PROFILES_REACHED если уже 5 активных профилей

PUT /api/v1/profiles/:id
  Body:    ProfileWriteBody
  Response: Profile

DELETE /api/v1/profiles/:id
  // При удалении активного профиля → сервер автоматически переключает active на primary
  Errors:  400 CANNOT_DELETE_PRIMARY | 400 CANNOT_DELETE_LAST_PROFILE
  Response: 204

PUT /api/v1/profiles/active
  Body:    { profile_id: string }
  Response: 204
```

### 3.3 ProfileWriteBody

```json
{
  "name":                  "Мама",
  "relation_type":         "MOTHER",
  "custom_relation_name":  null,
  "birth_year":            1965,
  "birth_month":           5,
  "birth_day":             15,
  "birth_hour":            12,
  "birth_minute":          0,
  "unknown_birth_time":    false,
  "sort_order":            1
}
```

### 3.4 Profile (ответ)

```json
{
  "id":                   "uuid",
  "name":                 "Мама",
  "relation_type":        "MOTHER",
  "custom_relation_name": null,
  "birth_year":           1965,
  "birth_month":          5,
  "birth_day":            15,
  "birth_hour":           12,
  "birth_minute":         0,
  "unknown_birth_time":   false,
  "is_primary":           false,
  "sort_order":           1,
  "created_at":           "2024-01-01T00:00:00Z",
  "updated_at":           "2024-01-01T00:00:00Z"
}
```

---

## 4. Настройки пользователя (AppSettings)

### 4.1 Таблица user_settings

```
user_settings
─────────────────────────────────────────────
user_id                      UUID, PK, FK → users.id
active_profile_id            UUID, FK → profiles.id, NULLABLE

onboarding_completed         BOOLEAN DEFAULT false

// Уведомления
notifications_enabled        BOOLEAN DEFAULT false
  // Master toggle — если false, все остальные флаги игнорируются
milestone_reminders_enabled  BOOLEAN DEFAULT true
  // Уведомление при приближении к вехе (за 7 дней, 1 день, 1 час и в момент)
family_reminders_enabled     BOOLEAN DEFAULT true
  // Аналогичные уведомления для профилей членов семьи
reengagement_enabled         BOOLEAN DEFAULT true
  // Периодические мотивационные уведомления (раз в 30 дней если не открывал)

// Отображение
approximate_labels_enabled   BOOLEAN DEFAULT true
  // Показывать ли пометку «приблизительно» когда unknown_birth_time = true
use_24_hour_format           BOOLEAN DEFAULT false
  // Формат времени в UI

updated_at                   TIMESTAMPTZ
```

### 4.2 Эндпоинты

```
GET /api/v1/settings
  Response: UserSettings

PATCH /api/v1/settings
  Body:    Partial<UserSettings> (любое подмножество полей)
  Response: UserSettings
```

### 4.3 UserSettings (ответ)

```json
{
  "active_profile_id":           "uuid | null",
  "onboarding_completed":        true,
  "notifications_enabled":       false,
  "milestone_reminders_enabled": true,
  "family_reminders_enabled":    true,
  "reengagement_enabled":        true,
  "approximate_labels_enabled":  true,
  "use_24_hour_format":          false,
  "updated_at":                  "2024-01-01T00:00:00Z"
}
```

---

## 5. История событий (EventHistory)

Отслеживает, был ли показан экран «Миллиард достигнут» для каждого профиля пользователя.

### 5.1 Таблица event_history

```
event_history
─────────────────────────────────────────────
id                      UUID, PK
user_id                 UUID, FK → users.id
profile_id              UUID, FK → profiles.id
UNIQUE (user_id, profile_id)

first_shown_at          TIMESTAMPTZ NULLABLE
  // Когда пользователь впервые открыл Event Screen для этого профиля.
  // null = ещё не открывал (EligibleFirstTime)

celebration_shown_at    TIMESTAMPTZ NULLABLE
  // Когда была проиграна celebration-анимация.
  // Заполняется один раз — идемпотентно.

share_prompt_shown_at   TIMESTAMPTZ NULLABLE
  // Когда показан share prompt.
  // Заполняется один раз — идемпотентно.

created_at              TIMESTAMPTZ
updated_at              TIMESTAMPTZ
```

### 5.2 Эндпоинты

```
GET /api/v1/event-history
  Response: { records: EventHistoryRecord[] }
  // Возвращает все записи для всех профилей пользователя

GET /api/v1/event-history/:profile_id
  Response: EventHistoryRecord
  Errors:   404 если записи нет (профиль ещё не открывал Event Screen)

PATCH /api/v1/event-history/:profile_id
  // Создаёт запись если нет, обновляет если есть.
  // Поля заполняются только если они ещё null (идемпотентность).
  Body: {
    "first_shown_at"?:        "2021-09-03T10:00:00Z",
    "celebration_shown_at"?:  "2021-09-03T10:01:00Z",
    "share_prompt_shown_at"?: null
  }
  Response: EventHistoryRecord
```

### 5.3 EventHistoryRecord (ответ)

```json
{
  "profile_id":             "uuid",
  "first_shown_at":         "2021-09-03T10:00:00Z",
  "celebration_shown_at":   "2021-09-03T10:01:00Z",
  "share_prompt_shown_at":  null,
  "updated_at":             "2021-09-03T10:01:00Z"
}
```

---

## 6. Капсулы времени (TimeCapsule)

### 6.1 Таблица time_capsules

```
time_capsules
─────────────────────────────────────────────
id                      UUID, PK
user_id                 UUID, FK → users.id
created_at              TIMESTAMPTZ
updated_at              TIMESTAMPTZ
deleted_at              TIMESTAMPTZ (soft delete)

title                   VARCHAR(80) NOT NULL
message                 TEXT NOT NULL
  // Максимум 2000 символов

recipient_profile_id    UUID, FK → profiles.id, NULLABLE
  // null = адресована самому пользователю (его primary-профилю)

unlock_condition_type   ENUM: 'exact_date_time' | 'billion_seconds_event'

// Поля для unlock_condition_type = 'exact_date_time':
unlock_at_epoch_ms      BIGINT NULLABLE
  // Unix timestamp в миллисекундах — когда капсула становится Available

// Поля для unlock_condition_type = 'billion_seconds_event':
unlock_profile_id       UUID, FK → profiles.id, NULLABLE
  // Профиль, чьё достижение 1 млрд секунд открывает капсулу

is_draft                BOOLEAN DEFAULT false
  // Черновик: не показывается в основном списке, нет уведомлений

opened_at               TIMESTAMPTZ NULLABLE
  // Заполняется когда пользователь открыл капсулу (нажал "Открыть")
  // После установки — не меняется (статус = Opened навсегда)
```

### 6.2 Эндпоинты

```
GET /api/v1/capsules
  Response: { capsules: TimeCapsule[] }
  // Включает черновики и открытые, НЕ включает soft-deleted

POST /api/v1/capsules
  Body:    CapsuleWriteBody
  Response: TimeCapsule (201)

PUT /api/v1/capsules/:id
  Body:    CapsuleWriteBody
  // Нельзя редактировать капсулу с opened_at != null (400)
  Response: TimeCapsule

DELETE /api/v1/capsules/:id
  Response: 204

POST /api/v1/capsules/:id/open
  // Помечает капсулу как открытую: opened_at = server_now
  // Только если капсула в статусе Available (сервер проверяет)
  // Идемпотентен: повторный вызов возвращает ту же запись
  Response: TimeCapsule
```

### 6.3 CapsuleWriteBody

```json
{
  "title":                  "Письмо в будущее",
  "message":                "Когда ты достигнешь миллиарда...",
  "recipient_profile_id":   null,
  "is_draft":               false,
  "unlock_condition_type":  "exact_date_time",
  "unlock_at_epoch_ms":     1893456000000,
  "unlock_profile_id":      null
}
```

Или для условия по событию:

```json
{
  "title":                  "Поздравляю, мама!",
  "message":                "Ты достигла миллиарда...",
  "recipient_profile_id":   "uuid-мамы",
  "is_draft":               false,
  "unlock_condition_type":  "billion_seconds_event",
  "unlock_at_epoch_ms":     null,
  "unlock_profile_id":      "uuid-мамы"
}
```

### 6.4 TimeCapsule (ответ)

```json
{
  "id":                     "uuid",
  "title":                  "Письмо в будущее",
  "message":                "Когда ты достигнешь миллиарда...",
  "recipient_profile_id":   null,
  "unlock_condition_type":  "exact_date_time",
  "unlock_at_epoch_ms":     1893456000000,
  "unlock_profile_id":      null,
  "is_draft":               false,
  "opened_at":              null,
  "created_at":             "2024-01-01T00:00:00Z",
  "updated_at":             "2024-01-01T00:00:00Z"
}
```

---

## 7. Прогресс по вехам (Milestone Progress)

Список самих вех фиксирован и зашит в клиенте. Бэкенд хранит только `last_seen_reached_id` — ID последней вехи, которую пользователь уже видел (нужно для детектирования «новой» достигнутой вехи при следующем запуске → триггер celebration).

### Фиксированный список вех

| ID | Порог (секунды) | Название | Главная |
|----|-----------------|----------|---------|
| `100m` | 100 000 000 | 100 миллионов секунд | нет |
| `250m` | 250 000 000 | 250 миллионов секунд | нет |
| `500m` | 500 000 000 | Полмиллиарда секунд | нет |
| `750m` | 750 000 000 | 750 миллионов секунд | нет |
| `1b` | 1 000 000 000 | Миллиард секунд | **да** |

### 7.1 Таблица user_milestone_progress

```
user_milestone_progress
─────────────────────────────────────────────
user_id               UUID (part of PK)
profile_id            UUID (part of PK), FK → profiles.id
last_seen_reached_id  VARCHAR NULLABLE
  // Пример: null → '100m' → '250m' → ... → '1b'
  // Если last_seen_reached_id != последней достигнутой вехи →
  //   клиент показывает celebration для новой вехи
updated_at            TIMESTAMPTZ
```

### 7.2 Эндпоинты

```
GET /api/v1/profiles/:profile_id/milestone-progress
  Response: { profile_id: string, last_seen_reached_id: string | null }

PUT /api/v1/profiles/:profile_id/milestone-progress
  Body:    { last_seen_reached_id: string }
  Response: 204
```

---

## 8. Синхронизация (начальная загрузка)

Один запрос при старте приложения — возвращает полное состояние.

```
GET /api/v1/sync
  Response:
  {
    "server_time": "2026-04-14T10:00:00Z",
      // Клиент сравнивает с device time. Если разница > 60 сек — предупреждение об
      // неточности расчётов.

    "user": { ...User },

    "settings": { ...UserSettings },
      // Включает active_profile_id и onboarding_completed

    "profiles": [ ...Profile[] ],

    "event_history": [ ...EventHistoryRecord[] ],
      // Все записи по всем профилям

    "capsules": [ ...TimeCapsule[] ],
      // Все капсулы включая черновики, без soft-deleted

    "milestone_progress": [
      { "profile_id": "uuid", "last_seen_reached_id": "500m" },
      ...
    ]
  }
```

---

## 9. Push-уведомления

### 9.1 Типы уведомлений

| Тип | Триггер | Зависит от настройки |
|-----|---------|----------------------|
| `milestone_approaching` | За 7 дней, 1 день, 1 час до вехи | `milestone_reminders_enabled` |
| `milestone_reached` | В момент достижения вехи | `milestone_reminders_enabled` |
| `family_milestone_approaching` | То же для профилей семьи | `family_reminders_enabled` |
| `family_milestone_reached` | То же для профилей семьи | `family_reminders_enabled` |
| `reengagement` | Если пользователь не открывал > 30 дней | `reengagement_enabled` |
| `capsule_unlocked` | Когда капсула стала Available | `notifications_enabled` |

### 9.2 Таблица scheduled_notifications

```
scheduled_notifications
─────────────────────────────────────────────
id                  UUID, PK
user_id             UUID, FK → users.id
profile_id          UUID, FK → profiles.id, NULLABLE
capsule_id          UUID, FK → time_capsules.id, NULLABLE

type                ENUM (см. выше)
scheduled_at        TIMESTAMPTZ   // Когда отправить
sent_at             TIMESTAMPTZ NULLABLE
cancelled_at        TIMESTAMPTZ NULLABLE
  // Заполняется при удалении профиля, смене даты рождения или отключении уведомлений

payload             JSONB
  // { title, body, deeplink }
```

### 9.3 Deeplinks в payload

```
billionseconds://event?profile_id=<uuid>&source=notification
billionseconds://capsules/<capsule_id>
billionseconds://milestones?profile_id=<uuid>
```

### 9.4 Логика пересчёта уведомлений

При `POST/PUT /api/v1/profiles/:id` сервер автоматически:
1. Отменяет старые `scheduled_notifications` для этого профиля (`cancelled_at = now`)
2. Вычисляет даты всех 5 вех: `birth_datetime + threshold_seconds`
3. Создаёт новые записи в `scheduled_notifications`: за 7 дней, 1 день, 1 час и точно в момент

При `PATCH /api/v1/settings` с изменением notification-флагов сервер:
- Отменяет соответствующие уведомления если флаг выключен

---

## 10. Аналитика

Клиент собирает события и отправляет батчами. Сервер только принимает и хранит.

### 10.1 Эндпоинт

```
POST /api/v1/analytics/events
  Body:    { events: AnalyticsEvent[] }  // Батч, max 100 событий
  Response: 204
```

### 10.2 AnalyticsEvent

```json
{
  "event_type":  "milestone_reached",
  "occurred_at": "2021-09-03T02:46:40Z",
  "properties": {
    "profile_id":   "uuid",
    "milestone_id": "1b"
  }
}
```

### 10.3 Список событий

| event_type | Когда | Свойства |
|-----------|-------|----------|
| `onboarding_completed` | Первый профиль сохранён | profile_id |
| `milestone_reached` | Клиент детектировал достижение вехи | profile_id, milestone_id |
| `milestone_shared` | Нажата кнопка Share на вехе | profile_id, milestone_id |
| `event_screen_opened` | Открыт Event Screen | profile_id, source (AUTO/MANUAL/NOTIFICATION/DEEPLINK), mode (FIRST_TIME/REPEAT) |
| `event_celebration_completed` | Анимация праздника завершена | profile_id |
| `event_action_tapped` | Нажата post-event кнопка | profile_id, action (SHARE/OPEN_MILESTONES/GO_HOME/...) |
| `capsule_created` | Создана капсула | capsule_id, condition_type |
| `capsule_opened` | Капсула открыта | capsule_id |
| `profile_added` | Добавлен семейный профиль | profile_id, relation_type |
| `profile_set_active` | Сменён активный профиль | profile_id |
| `settings_changed` | Изменена настройка | setting_name, new_value |
| `app_opened` | Открытие приложения | source (cold_start/notification/deeplink) |

---

## 11. Управление данными и GDPR

```
DELETE /api/v1/me
  // Hard delete: удаляет пользователя и все его данные из БД
  Response: 204

POST /api/v1/me/reset
  // Сброс онбординга: удаляет профили, капсулы, историю событий.
  // Аккаунт (users + user_settings) сохраняется.
  Body:    { "confirm": true }
  Response: 204

GET /api/v1/me/export
  // GDPR: экспорт всех данных в JSON
  Response: {
    user, settings, profiles, event_history, capsules, milestone_progress
  }
```

---

## 12. Миграция локальных данных (первый вход)

При первом входе или привязке анонимного аккаунта к провайдеру клиент отправляет все локально накопленные данные.

```
POST /api/v1/migrate
  Body:
  {
    "profiles": [
      {
        "local_id":           "timestamp_local_id",
        "name":               "Иван",
        "relation_type":      "SELF",
        ...все поля профиля кроме id...
      }
    ],
    "active_profile_local_id": "timestamp_local_id",
    "settings": { ...UserSettings без active_profile_id },
    "event_history": [
      {
        "profile_local_id":    "timestamp_local_id",
        "first_shown_at":      "2021-09-03T10:00:00Z",
        "celebration_shown_at": "2021-09-03T10:01:00Z",
        "share_prompt_shown_at": null
      }
    ],
    "capsules": [
      {
        "local_id":               "local_capsule_id",
        "title":                  "Письмо",
        "unlock_profile_local_id": "timestamp_local_id",
        ...все поля капсулы...
      }
    ],
    "milestone_progress": [
      {
        "profile_local_id":       "timestamp_local_id",
        "last_seen_reached_id":   "500m"
      }
    ]
  }

  Response:
  {
    // Маппинг локальных ID → серверных UUID (нужен клиенту для обновления локального стейта)
    "id_mapping": [
      { "local_id": "timestamp_local_id", "server_id": "uuid" }
    ],
    // Полное состояние как в /sync
    ...sync response
  }
```

**Логика на сервере:**
- Если у пользователя уже есть данные (например, зашёл с другого устройства) → merge: профили с совпадающим именем+датой рождения объединяются, остальные добавляются
- Если конфликт — приоритет у серверных данных

---

## 13. Валидация на сервере

| Поле | Правило |
|------|---------|
| `name` (Profile) | Не пустое, max 80 символов |
| `birth_year` | >= 1900, <= текущий год |
| `birth_month` | 1–12 |
| `birth_day` | 1–31, корректный для данного месяца/года |
| `birth_hour` | 0–23 |
| `birth_minute` | 0–59 |
| Дата рождения целиком | Не в будущем |
| Кол-во профилей | Максимум 5 активных на пользователя |
| `title` (Capsule) | Не пустое, max 80 символов |
| `message` (Capsule) | Не пустое, max 2000 символов |
| `unlock_at_epoch_ms` | Минимум `now + 3 600 000` мс (1 час в будущем) |
| `unlock_profile_id` | Существует, принадлежит текущему пользователю |
| `recipient_profile_id` | Если указан — существует, принадлежит пользователю |

---

## 14. Коды ошибок

```
400 Bad Request       — невалидные поля (тело: { error: { code, message, field? } })
401 Unauthorized      — токен отсутствует, невалиден или просрочен
403 Forbidden         — попытка доступа к чужому ресурсу
404 Not Found         — ресурс не существует
409 Conflict          — нарушение уникальности (дублирующий primary-профиль и т.п.)
422 Unprocessable     — бизнес-правило нарушено (max 5 профилей, нельзя открыть капсулу и т.д.)
429 Too Many Requests — rate limiting
500 Internal Error    — серверная ошибка
```

Формат ошибки:
```json
{
  "error": {
    "code":    "MAX_PROFILES_REACHED",
    "message": "Нельзя создать более 5 профилей",
    "field":   null
  }
}
```

Коды бизнес-ошибок (`422`):

| code | Описание |
|------|----------|
| `MAX_PROFILES_REACHED` | Достигнут лимит в 5 профилей |
| `CANNOT_DELETE_PRIMARY` | Нельзя удалить primary-профиль |
| `CANNOT_DELETE_LAST_PROFILE` | Нельзя удалить последний профиль |
| `CAPSULE_ALREADY_OPENED` | Капсула уже была открыта |
| `CAPSULE_NOT_AVAILABLE` | Капсула ещё не Available |
| `CAPSULE_IS_DRAFT` | Нельзя открыть черновик |
| `PROFILE_NOT_FOUND` | Профиль для UnlockCondition не найден |

---

## 15. Rate Limiting

| Эндпоинт | Лимит |
|----------|-------|
| `POST /api/v1/auth/*` | 10 запросов / мин / IP |
| `POST /api/v1/analytics/events` | 10 запросов / мин / user, max 100 событий / батч |
| `POST /api/v1/migrate` | 3 запроса / час / user |
| Остальные | 60 запросов / мин / user |

---

## 16. Сводка таблиц БД

| Таблица | Описание |
|---------|----------|
| `users` | Аккаунты, FCM-токены, платформа |
| `profiles` | Профили пользователя и семьи (до 5 на юзера) |
| `user_settings` | Настройки приложения + active_profile_id + onboarding_completed |
| `event_history` | История показов Event Screen (first_shown, celebration, share) |
| `time_capsules` | Капсулы времени с условиями открытия |
| `user_milestone_progress` | last_seen_reached_id для каждого профиля |
| `scheduled_notifications` | Очередь push-уведомлений |
| `analytics_events` | Лог пользовательских событий |

---

## 17. Вне MVP (будущее)

- Медиа-вложения в капсулах (фото, видео)
- Создание видео-поздравлений (PostEventAction.CREATE_VIDEO)
- Premium-подписка / Billing
- Реферальная система
- Социальные функции (поделиться профилем)
- Leaderboard
