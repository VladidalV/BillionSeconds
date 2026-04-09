# Countdown Screen — Технический план

## Контекст

Главный экран приложения — центральный хаб, куда пользователь возвращается чаще всего после онбординга. Текущий `ResultScreen.kt` — минимальная заглушка: показывает дату, обратный отсчёт и одну кнопку. Нужно спроектировать полноценный production-ready Countdown Screen с блоком события, живым отсчётом, прогрессом и 5 action-кнопками.

Онбординг уже реализован. Данные пользователя (`BirthdayData`, `milestoneInstant`) уже доступны в `AppState` через `AppStore`.

---

## Анализ существующей архитектуры

| Компонент | Файл | Что уже есть |
|---|---|---|
| State | `mvi/AppState.kt` | `milestoneInstant`, `progressPercent`, `isMilestoneReached`, `secondsRemaining`, `unknownTime`, birth fields |
| Intent | `mvi/AppIntent.kt` | `ClearClicked`, `CalculateClicked` — только базовые |
| Reducer | `mvi/AppReducer.kt` | Чистая функция, легко расширяется |
| Store | `mvi/AppStore.kt` | `startTick()` уже работает, `mainClear()`, `mainCalculate()` |
| Domain | `domain/BillionSecondsCalculator.kt` | `computeAll()`, `calculateProgress()`, `isReached()`, `secondsUntil()` |
| Navigation | `navigation/AppScreen.kt` | 4 экрана, State-based routing |
| UI | `ui/ResultScreen.kt` | Минимальный экран — заменяем |
| Effects | — | **Не существует** — нужно добавить |

**Критический gap:** В проекте нет механизма одноразовых событий (Effects). Все переходы сейчас через `state.screen`. Для action-кнопок нужен `SharedFlow<AppEffect>`.

---

## 4.1 Архитектура (MVI + Effects)

### Слои и ответственности

```
UI (CountdownScreen.kt)
  ↓ dispatch(AppIntent)          ↑ collect(AppState)
AppStore                         ↑ collect(AppEffect) via SharedFlow
  ↓ AppReducer.reduce()
  ↓ side effects / use cases
Domain (use cases, calculator)
  ↓
Repository → Storage
```

### Что куда идёт

| Тип | Примеры | Где живёт |
|---|---|---|
| **State** | milestone date, countdown, progress, loading, eventStatus | `AppState` + `CountdownUiState` |
| **Intent** | ShareClicked, LifeStatsClicked, CountdownScreenResumed | `AppIntent` |
| **Effect** | NavigateToShare, ShowComingSoon | `AppEffect` (новый `SharedFlow`) |
| **Countdown логика** | ticker, secondsRemaining update | `AppStore.startTick()` — уже есть |
| **Прогресс** | `calculateProgress()` | `BillionSecondsCalculator` — уже есть |
| **Форматирование** | "12.05.2025", "05:30:45" | `CountdownFormatter` object в `domain/` |
| **EventStatus** | Upcoming / Today / Reached | `EventStatus` sealed class в `domain/model/` |

### Почему Effects через SharedFlow, а не через State

State хранит **текущее положение дел** — его нужно сохранять и восстанавливать. Navigating to Share — **одноразовое событие**: если его положить в state и пользователь повернёт экран, навигация случится повторно. `SharedFlow` с `replay = 0` гарантирует exactly-once доставку.

```kotlin
// AppStore
private val _effect = MutableSharedFlow<AppEffect>(replay = 0, extraBufferCapacity = 64)
val effect: SharedFlow<AppEffect> = _effect.asSharedFlow()
```

```kotlin
// App.kt — сбор эффектов
LaunchedEffect(store) {
    store.effect.collect { effect ->
        when (effect) {
            is AppEffect.ShareText      -> shareTextPlatform(effect.text)
            is AppEffect.ShowComingSoon -> showComingSoonSheet(effect.feature)
            // ...
        }
    }
}
```

---

## 4.2 Navigation

### Роль Countdown Screen

Countdown Screen = **NavigationHub**. Он не переходит на экраны напрямую — он эмитирует Effects, которые `App.kt` обрабатывает.

### Переходы и их тип

| Действие | Тип перехода | Механизм |
|---|---|---|
| Share | `AppEffect.ShareText(text)` | System Share Sheet — нет отдельного экрана |
| Create Video | `AppEffect.ShowComingSoon("video")` | Bottom Sheet stub |
| Write Letter | `AppEffect.ShowComingSoon("letter")` | Bottom Sheet stub |
| Add Family | `AppEffect.ShowComingSoon("family")` | Bottom Sheet stub |
| Life Stats | `AppEffect.NavigateToLifeStats` | `AppScreen.LifeStats` — новый экран |
| Milestone Details | `AppEffect.NavigateToMilestoneDetails` | `AppScreen.MilestoneDetails` — опционально |
| Change Date | `AppIntent.ClearClicked` → state | `AppScreen.Main`, `showMainResult = false` |

### Обновление `AppScreen`

```kotlin
sealed class AppScreen {
    data object OnboardingIntro  : AppScreen()
    data object OnboardingInput  : AppScreen()
    data object OnboardingResult : AppScreen()
    data object Main             : AppScreen()
    data object LifeStats        : AppScreen()   // NEW — stub
    data object MilestoneDetails : AppScreen()   // NEW — опционально
}
```

**Почему Life Stats — отдельный `AppScreen`, а не Effect:**
Life Stats — полноценный экран с данными, нужна back-навигация через `AppIntent.BackClicked`. Share — системный sheet, не Compose-экран.

---

## 4.3 State модель

### `CountdownUiState` — sub-state внутри `AppState`

Отдельный sub-state оправдан: ~12 полей только для главного экрана, включая форматированные строки. Вложение лучше чем раздуть плоский `AppState`.

```kotlin
// domain/model/EventStatus.kt
sealed class EventStatus {
    data object Upcoming : EventStatus()
    data object Today    : EventStatus()
    data object Reached  : EventStatus()
}

// mvi/CountdownUiState.kt
data class CountdownUiState(
    val isLoading: Boolean              = true,
    val eventStatus: EventStatus        = EventStatus.Upcoming,

    // Raw данные
    val milestoneInstant: Instant?      = null,
    val progressFraction: Float         = 0f,
    val secondsRemaining: Long          = 0L,
    val isUnknownBirthTime: Boolean     = false,

    // Форматированные строки для UI (пересчитываются в Store, не в Composable)
    val formattedMilestoneDate: String  = "",
    val formattedMilestoneTime: String  = "",
    val formattedCountdown: String      = "",
    val formattedProgress: String       = "",   // "63.4%"

    // Error
    val error: CountdownError?          = null
)

sealed class CountdownError {
    data object NoProfileData  : CountdownError()
    data object CorruptedData  : CountdownError()
}
```

### Расширение `AppState`

```kotlin
data class AppState(
    val screen: AppScreen = AppScreen.OnboardingIntro,
    // ... поля онбординга без изменений
    val countdown: CountdownUiState = CountdownUiState(),   // NEW
)
```

**Почему форматированные строки в state, а не в Composable?**
Держать форматирование в Composable = пересчёт при каждом рекомпозите. В state — пересчёт только при изменении данных (раз в секунду в тике).

---

## 4.4 Intent / Action

```kotlin
// Добавить в AppIntent.kt
sealed class AppIntent {
    // ... существующие интенты

    // Lifecycle
    data object CountdownScreenStarted  : AppIntent()   // первый показ
    data object CountdownScreenResumed  : AppIntent()   // возврат с другого экрана

    // Action buttons
    data object ShareClicked            : AppIntent()
    data object CreateVideoClicked      : AppIntent()
    data object WriteLetterClicked      : AppIntent()
    data object AddFamilyClicked        : AppIntent()
    data object LifeStatsClicked        : AppIntent()
    data object MilestoneDetailsClicked : AppIntent()

    // Navigation
    data object BackClicked             : AppIntent()   // для LifeStats и других экранов
}
```

**Почему `OnCountdownTick` — не Intent:**
Тик происходит каждую секунду. Гонять через `dispatch()` — лишний overhead и усложнение цикла. Тик обновляет `_state` напрямую внутри `AppStore`.

### Разбивка по источнику

| Источник | Интенты |
|---|---|
| Пользователь | ShareClicked, CreateVideoClicked, WriteLetterClicked, AddFamilyClicked, LifeStatsClicked, MilestoneDetailsClicked, BackClicked, ClearClicked |
| Lifecycle | CountdownScreenStarted, CountdownScreenResumed |
| Система (внутри Store) | Tick — не Intent, прямое обновление state |

---

## 4.5 Effect / One-time events

```kotlin
// mvi/AppEffect.kt — НОВЫЙ ФАЙЛ
sealed class AppEffect {
    data object NavigateToLifeStats        : AppEffect()
    data object NavigateToMilestoneDetails : AppEffect()
    data class  ShareText(val text: String): AppEffect()
    data class  ShowComingSoon(val feature: String) : AppEffect()
    data class  ShowError(val message: String)      : AppEffect()
}
```

**Почему Effect, а не State:**

| Effect | Причина |
|---|---|
| `NavigateToLifeStats` | При повороте экрана навигация повторится, если хранить в state |
| `ShareText` | Системный sheet — не Compose-экран, не нужен в state |
| `ShowComingSoon` | Временный bottom sheet, не персистентен |

**Безопасная обработка в Compose:**
```kotlin
// App.kt
LaunchedEffect(store) {           // корутина живёт столько, сколько store
    store.effect.collect { effect ->
        when (effect) {
            is AppEffect.NavigateToLifeStats  -> store.dispatch(...)   // меняет screen через state
            is AppEffect.ShareText            -> shareTextPlatform(effect.text)
            is AppEffect.ShowComingSoon       -> showComingSoonSheet(effect.feature)
            is AppEffect.ShowError            -> showSnackbar(effect.message)
            else -> Unit
        }
    }
}
```

`SharedFlow(replay=0)` — нет риска повторной доставки при пересоздании.

---

## 4.6 Domain слой

### Что уже есть — переиспользовать

- `BillionSecondsCalculator.computeAll(data, now)` → `MilestoneResult`
- `BillionSecondsCalculator.calculateProgress(birth, now)` → `Float`
- `BillionSecondsCalculator.isReached(milestone, now)` → `Boolean`
- `BirthdayValidator.validate(...)` → `ValidationError?`

### Что добавить

**`EventStatus` маппер** — чистая extension-функция, не use case:

```kotlin
// domain/model/EventStatus.kt
fun MilestoneResult.toEventStatus(now: Instant): EventStatus {
    if (isMilestoneReached) return EventStatus.Reached
    val milestoneDate = milestoneInstant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return if (milestoneDate == today) EventStatus.Today else EventStatus.Upcoming
}
```

**`CountdownFormatter`** — KMP-общий object в `domain/`:

```kotlin
// domain/CountdownFormatter.kt
object CountdownFormatter {
    fun formatMilestoneDate(instant: Instant): String   // "12.05.2025"
    fun formatMilestoneTime(instant: Instant): String   // "18:42"
    fun formatCountdown(secondsRemaining: Long): String // "123д 05:30:45" / "05:30:45"
    fun formatProgress(fraction: Float): String         // "63.4%"
}
```

**Почему форматирование в `domain/`, а не в `ui/`:**
- Тестируется unit-тестами без Compose
- KMP: одна логика для Android/iOS/Web
- Composable остаётся stateless

**Почему не нужны `GetUserCountdownDataUseCase`, `CalculateRemainingTimeUseCase`:**
Нет DI-фреймворка. Один источник данных (local storage). Вся логика уже в `BillionSecondsCalculator`. Добавить use case ради одного вызова — церемония без пользы. Оправдано при появлении network или multi-source.

---

## 4.7 Countdown механика

### Ticker

`AppStore.startTick()` уже реализован. Расширить: при каждом тике обновлять форматированные строки и `eventStatus`.

```kotlin
private fun startTick(milestone: Instant) {
    tickJob?.cancel()
    tickJob = scope.launch {
        while (true) {
            delay(1000)
            val now = currentInstant()
            val remaining = maxOf(0L, BillionSecondsCalculator.secondsUntil(milestone, now))
            _state.update { state ->
                state.copy(
                    countdown = state.countdown.copy(
                        secondsRemaining = remaining,
                        isMilestoneReached = BillionSecondsCalculator.isReached(milestone, now),
                        formattedCountdown = CountdownFormatter.formatCountdown(remaining),
                        eventStatus = computeEventStatus(milestone, now)
                    )
                )
            }
        }
    }
}
```

### Lifecycle safety

| Механизм | Статус |
|---|---|
| `tickJob` в `AppStore` (не в Composable) | Уже есть — нет утечек при рекомпозите |
| `CoroutineScope(Dispatchers.Main + SupervisorJob())` | Уже есть |
| `dispose()` отменяет scope | Уже есть |
| `DisposableEffect(store) { onDispose { store.dispose() } }` | Уже есть в `App.kt` |

### При уходе с экрана

Ticker не останавливается при переходе между Compose-экранами — `AppStore` живёт на уровне приложения. State актуален при возврате. Остановка тика при onPause — **defer**.

### Мгновенное обновление при возврате

`CountdownScreenResumed` intent → пересчёт немедленно, без ожидания следующего тика:

```kotlin
AppIntent.CountdownScreenResumed -> {
    val milestone = _state.value.countdown.milestoneInstant ?: return
    val now = currentInstant()
    updateCountdownState(milestone, now)   // приватный метод Store
}
```

---

## 4.8 Работа со временем

### Классы (KMP: `kotlinx.datetime`)

| Назначение | Тип | Где |
|---|---|---|
| Хранение milestone | `Instant` (UTC) | `CountdownUiState.milestoneInstant` |
| Отображение пользователю | `LocalDateTime` | конвертация через `TimeZone.currentSystemDefault()` |
| Определение "сегодня" | `LocalDate` сравнение | `EventStatus.toEventStatus()` |
| Тик | `Clock.System.now()` через `currentInstant()` | `AppStore` |

### Timezone

- `milestoneInstant` хранится как UTC `Instant` — не меняется при смене TZ
- Отображаемое время сдвигается при смене TZ — **корректное поведение**
- `CountdownScreenResumed` пересчитывает форматированные строки — TZ подхватывается автоматически

### Edge cases времени

| Ситуация | Поведение |
|---|---|
| Ручная смена системного времени | Ticker подхватит через ≤1 сек |
| `secondsRemaining < 0` | Обёрнуто в `maxOf(0L, ...)` — уже есть |
| Смена TZ в ночь перед событием | `EventStatus.Today` пересчитается при следующем тике или Resume |

### Approximate birth time

`unknownTime = true` → в `BirthdayData` записан `hour=12, minute=0`. Логика расчёта не меняется. UI показывает disclaimer: "* Время приблизительное".

---

## 4.9 Источник данных

### Схема

```
AppStore.init
  → repository.getBirthday()              // BirthdayData из storage
  → BillionSecondsCalculator.computeAll() // MilestoneResult
  → buildCountdownUiState(result, now)    // CountdownUiState с форматированием
  → startTick(milestone)
```

### Хранить ли `targetDateTime` в storage?

**Нет.** Вычислять при каждом запуске:
- Расчёт быстрый — константа + арифметика на `Instant`
- `BirthdayData` — единственная source of truth
- Нет риска drift при обновлении библиотеки

Кэширование оправдано только при дорогих вычислениях (сетевой запрос). Здесь — нет.

---

## 4.10 Data Persistence

### Текущее хранилище

| Ключ | Тип |
|---|---|
| year / month / day / hour / minute | Int |
| saved | Boolean |
| onboarding_completed | Boolean |

### Что добавить

| Ключ | Тип | Зачем |
|---|---|---|
| `unknown_time` | Boolean | Disclaimer на CountdownScreen при рестарте |

**Почему `unknown_time` нужно персистировать:**
Сейчас это in-memory поле в `AppState`. При рестарте приложения теряется — Countdown Screen не покажет disclaimer. Добавить в `BirthdayStorage` интерфейс (+3 платформенные реализации).

**DataStore:** Не нужен. SharedPreferences/NSUserDefaults/localStorage достаточно для MVP. DataStore оправдан при реактивных данных (Flow) или сложных миграциях.

**Room:** Не нужен. Нет реляционных данных.

---

## 4.11 Edge Cases

| Кейс | Ожидаемое поведение |
|---|---|
| Нет данных профиля (storage пуст, флаг completed) | `error = NoProfileData` → кнопка "Настроить" → `BirthdayScreen` |
| Повреждённые данные (year=0, month=13) | `BirthdayValidator` → `error = CorruptedData` → предложить ввести дату заново |
| Дата рождения в будущем | `ValidationError.DateInFuture` → ошибка, предложить исправить |
| Событие уже наступило | `EventStatus.Reached` → CelebrationContent, прогресс = 100%, countdown скрыт |
| Событие сегодня | `EventStatus.Today` → banner "Сегодня твой миллиард!" |
| Неизвестно время рождения | `isUnknownBirthTime = true` → disclaimer под датой и прогрессом |
| Смена timezone | `CountdownScreenResumed` пересчитывает date/time/eventStatus |
| Ручная смена системного времени | Ticker подхватит через ≤1 сек |
| Экран открыт часами | Coroutine с `SupervisorJob` — нет утечек |
| Быстрое сворачивание/разворачивание | `CountdownScreenResumed` пересчитывает snapshot немедленно |

---

## 4.12 Форматирование

### Где живёт

`domain/CountdownFormatter.kt` — KMP-общий object. Нет Android/iOS-зависимости. Тестируется unit-тестами.

### Сигнатуры

```kotlin
object CountdownFormatter {
    // "12.05.2025"
    fun formatMilestoneDate(instant: Instant): String

    // "18:42"
    fun formatMilestoneTime(instant: Instant): String

    // days > 0: "123д 05:30:45"
    // только часы: "05:30:45"
    fun formatCountdown(secondsRemaining: Long): String

    // "63.4%"
    fun formatProgress(fraction: Float): String
}
```

### Locale

`kotlinx.datetime` не имеет встроенной локализации названий месяцев. **MVP:** числовой формат `DD.MM.YYYY` — универсален. Локализованные названия месяцев — `expect/actual` с `DateTimeFormatter` — defer.

---

## 4.13 Структура проекта

```
composeApp/src/commonMain/.../billionseconds/
│
├── navigation/
│   └── AppScreen.kt                       ← MODIFIED (+LifeStats, +MilestoneDetails)
│
├── domain/
│   ├── BillionSecondsCalculator.kt        ← без изменений
│   ├── BirthdayValidator.kt               ← без изменений
│   ├── CountdownFormatter.kt              ← NEW
│   └── model/
│       ├── MilestoneResult.kt             ← без изменений
│       └── EventStatus.kt                ← NEW
│
├── mvi/
│   ├── AppState.kt                        ← MODIFIED (+countdown: CountdownUiState)
│   ├── AppIntent.kt                       ← MODIFIED (+9 новых интентов)
│   ├── AppEffect.kt                       ← NEW
│   ├── AppReducer.kt                      ← MODIFIED (+новые ветки)
│   ├── AppStore.kt                        ← MODIFIED (+SharedFlow<AppEffect>, расширен startTick)
│   └── CountdownUiState.kt               ← NEW
│
├── ui/
│   ├── countdown/
│   │   ├── CountdownScreen.kt             ← NEW (заменяет ResultScreen.kt)
│   │   ├── EventBlock.kt                  ← NEW (дата / время / статус-badge)
│   │   ├── CountdownBlock.kt              ← NEW (живой отсчёт)
│   │   ├── ProgressBlock.kt               ← NEW (прогресс бар + %)
│   │   └── ActionsBlock.kt               ← NEW (5 кнопок)
│   ├── lifestats/
│   │   └── LifeStatsScreen.kt             ← NEW (stub)
│   ├── shared/
│   │   └── ComingSoonSheet.kt             ← NEW (stub bottom sheet)
│   ├── components/
│   │   └── ProgressBar.kt                 ← без изменений (MilestoneProgressBar)
│   ├── BirthdayScreen.kt                  ← без изменений
│   └── ResultScreen.kt                    ← DELETED
│
├── data/
│   ├── BirthdayStorage.kt                 ← MODIFIED (+unknownTime)
│   ├── BirthdayRepository.kt              ← MODIFIED (+unknownTime)
│   └── model/BirthdayData.kt             ← без изменений
│
└── App.kt                                 ← MODIFIED (+LaunchedEffect effects, +новые экраны)

Платформенные (симметрично):
androidMain/.../data/AndroidBirthdayStorage.kt  ← MODIFIED (+unknownTime)
iosMain/.../data/IosBirthdayStorage.kt          ← MODIFIED (+unknownTime)
webMain/.../data/WebBirthdayStorage.kt          ← MODIFIED (+unknownTime)
```

---

## 4.14 Взаимодействие с модулями

| Модуль | Что Countdown получает | Что Countdown отправляет |
|---|---|---|
| Onboarding | `BirthdayData`, `isOnboardingCompleted` | ничего |
| Profile | `BirthdayData` (одиночный профиль) | `AppEffect.ShowComingSoon("family")` |
| Life Stats | ничего | `AppEffect.NavigateToLifeStats` |
| Share | ничего | `AppEffect.ShareText(text)` |
| Create Video | ничего | `AppEffect.ShowComingSoon("video")` |
| Time Capsule | ничего | `AppEffect.ShowComingSoon("letter")` |

**Как не зашивать связанность:**
- Countdown Screen не знает про реализацию Share — только эмитирует `AppEffect.ShareText`
- `App.kt` — единственный оркестратор, знающий platform-specific share API
- Будущие экраны получают данные через свои Store или аргументы навигации — не через singleton AppStore

---

## 4.15 Порядок реализации

### Фаза 1 — Domain (без UI, всё тестируемо)

1. Создать `EventStatus` sealed class + `toEventStatus(now)` extension → `domain/model/EventStatus.kt`
2. Создать `CountdownFormatter` object → `domain/CountdownFormatter.kt`
3. Написать тесты: `CountdownFormatterTest`, `EventStatusTest`

### Фаза 2 — MVI расширение

4. Создать `CountdownUiState` + `CountdownError` → `mvi/CountdownUiState.kt`
5. Создать `AppEffect` sealed class → `mvi/AppEffect.kt`
6. Расширить `AppState`: добавить `countdown: CountdownUiState`
7. Расширить `AppIntent`: добавить 9 новых интентов
8. Расширить `AppReducer`: добавить ветки (только state-трансформации, без side effects)
9. Написать тесты: `AppReducerTest` новые кейсы

### Фаза 3 — Store расширение

10. Добавить `_effect: MutableSharedFlow<AppEffect>` + публичный `effect` в `AppStore`
11. Добавить `buildCountdownUiState(result, now, unknownTime)` приватный метод
12. Расширить `AppStore.init`: строить `CountdownUiState` при восстановлении
13. Расширить `startTick()`: обновлять `formattedCountdown`, `eventStatus`
14. Реализовать side effects для новых интентов (ShareClicked → `_effect.emit(ShareText(...))`)
15. Реализовать `CountdownScreenResumed` → немедленный пересчёт через `updateCountdownState()`

### Фаза 4 — Storage

16. Добавить `unknownTime` в `BirthdayStorage` интерфейс
17. Реализовать в `AndroidBirthdayStorage`, `IosBirthdayStorage`, `WebBirthdayStorage`
18. Сохранять `unknownTime` при `onboardingCalculate()` в AppStore
19. Загружать `unknownTime` в `AppStore.init` при восстановлении

### Фаза 5 — UI

20. Создать `CountdownScreen.kt` — корневой composable экрана
21. Создать `EventBlock.kt` — дата, время, статус-badge (Upcoming / Today / Reached)
22. Создать `CountdownBlock.kt` — `state.countdown.formattedCountdown` + метка
23. Создать `ProgressBlock.kt` — `MilestoneProgressBar` + `formattedProgress`
24. Создать `ActionsBlock.kt` — 5 кнопок, каждая диспатчит соответствующий Intent
25. Создать `ComingSoonSheet.kt` — stub Modal Bottom Sheet
26. Создать `LifeStatsScreen.kt` — stub экран (текст + BackClicked)
27. Удалить `ResultScreen.kt`

### Фаза 6 — Navigation + Effects Wire-up

28. Обновить `AppScreen.kt`: добавить `LifeStats`, `MilestoneDetails`
29. Обновить `App.kt`:
    - `LaunchedEffect(store) { store.effect.collect { ... } }`
    - `when (state.screen)` — добавить новые ветки
    - `expect fun shareText(text: String)` для platform-specific share

### Фаза 7 — Edge cases и полировка

30. `CountdownError.NoProfileData` → UI с кнопкой "Настроить"
31. `CountdownError.CorruptedData` → UI с предложением ввести дату
32. `EventStatus.Today` → banner вверху экрана
33. `isUnknownBirthTime = true` → disclaimer под временем и прогрессом

---

## 4.16 Тестирование

### Критично в первую очередь (Unit)

| Тест | Что проверяем |
|---|---|
| `CountdownFormatterTest` | `formatCountdown(0L)`, `formatCountdown(86400 + 3661)`, `formatProgress(0f / 0.634f / 1f)`, форматирование дат |
| `EventStatusTest` | `toEventStatus` → Upcoming / Today / Reached; граничные случаи (полночь, за 1 сек до) |
| `AppReducerTest` | Все новые intent-ветки; `CountdownScreenResumed` не меняет `screen` |
| `BillionSecondsCalculatorTest` | Уже есть; расширить edge cases |

### Store тесты (при наличии времени)

- `ShareClicked` → `effect` получает `AppEffect.ShareText`
- `CountdownScreenResumed` → `countdown.formattedCountdown` обновляется
- `init` с пустым storage → `countdown.error = NoProfileData`

### UI тесты

Defer для MVP — Compose UI тесты на KMP требуют значительной инфраструктуры.

---

## 4.17 MVP vs Defer

### MVP — обязательно

- `CountdownScreen` с 4 блоками: Event, Countdown, Progress, Actions
- Живой тик (расширить существующий)
- `EventStatus` (Upcoming / Today / Reached)
- Стабы для CreateVideo / WriteLetter / AddFamily через `ComingSoonSheet`
- Share — базовый system text share
- `LifeStatsScreen` stub
- Persist `unknown_time` для disclaimer
- `CountdownScreenResumed` для актуализации при возврате

### Defer — после MVP

| Что | Причина |
|---|---|
| Остановка тика при onPause | Lifecycle-aware в KMP — дополнительная сложность |
| Локализованные названия месяцев | `expect/actual` с `DateTimeFormatter` — трудоёмко |
| Multi-profile support | Отдельная большая фича |
| Push-уведомления при приближении события | Background execution, permissions |
| Widget | Platform-specific, отдельный релиз |
| Анимации при Reached (конфетти) | Украшение, не функционал |

---

## 4.18 Итого

### Новые файлы

| Файл | Тип |
|---|---|
| `domain/model/EventStatus.kt` | sealed class + extension |
| `domain/CountdownFormatter.kt` | object |
| `mvi/CountdownUiState.kt` | data class + CountdownError |
| `mvi/AppEffect.kt` | sealed class |
| `ui/countdown/CountdownScreen.kt` | Composable |
| `ui/countdown/EventBlock.kt` | Composable |
| `ui/countdown/CountdownBlock.kt` | Composable |
| `ui/countdown/ProgressBlock.kt` | Composable |
| `ui/countdown/ActionsBlock.kt` | Composable |
| `ui/lifestats/LifeStatsScreen.kt` | Composable stub |
| `ui/shared/ComingSoonSheet.kt` | Composable stub |

### Изменяемые файлы

| Файл | Изменение |
|---|---|
| `navigation/AppScreen.kt` | +LifeStats, +MilestoneDetails |
| `mvi/AppState.kt` | +countdown: CountdownUiState |
| `mvi/AppIntent.kt` | +9 новых интентов |
| `mvi/AppReducer.kt` | +новые ветки |
| `mvi/AppStore.kt` | +SharedFlow<AppEffect>, расширен startTick и init |
| `data/BirthdayStorage.kt` | +unknownTime (интерфейс + 3 платформы) |
| `data/BirthdayRepository.kt` | +unknownTime делегат |
| `App.kt` | +LaunchedEffect effects, +новые экраны |

### Удаляемые файлы

- `ui/ResultScreen.kt` → заменяется `ui/countdown/CountdownScreen.kt`

---

## Основные риски

| Риск | Решение |
|---|---|
| `SharedFlow` + пересоздание composable | `LaunchedEffect(store)` — store стабилен через `remember` в `App.kt` |
| Форматирование в каждом тике | Дёшево (≤1 мс), но замерить при появлении сложных форматов |
| `unknown_time` persistence | Явное изменение контракта `BirthdayStorage` — синхронизировать с командой |
| KMP Locale для месяцев | Числовой формат DD.MM.YYYY — безопасный fallback для MVP |

---

## Верификация

1. Fresh install → онбординг → Countdown Screen показывает корректные данные
2. Свернуть/развернуть → countdown актуален, не "замёрзший"
3. `ShareClicked` → system share sheet с текстом
4. `CreateVideoClicked` → bottom sheet "Coming soon"
5. `LifeStatsClicked` → stub экран + back работает
6. Принудительно изменить системное время → данные обновились через ≤1 сек
7. Дата рождения 31+ лет назад → `EventStatus.Reached` → celebration UI
8. Дата milestone = сегодня → `EventStatus.Today` → banner
9. `unknownTime = true` → disclaimer виден на Countdown Screen после рестарта
10. Запустить тесты: `./gradlew :composeApp:testDebugUnitTest`
