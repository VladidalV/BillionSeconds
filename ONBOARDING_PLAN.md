# Onboarding Flow — BillionSeconds KMP

## Контекст

Приложение сейчас — 2 экрана без онбординга: ввод даты → результат. Нужно добавить полноценный онбординг из 3 экранов (Intro → Input → Result), после которого пользователь попадает в основное приложение. При повторном запуске онбординг пропускается. Проект — KMP (Android/iOS/Web), Compose Multiplatform, MVI, без навигационной библиотеки.

---

## 2.1 Архитектура (MVI)

### Решение: единый `AppState` + единый `AppStore`, sealed class `AppScreen` для навигации

**Почему не добавлять в `BirthdayState`?**
`BirthdayState` уже отвечает за одну вещь. Добавление `currentScreen`, `progressPercent`, `unknownTime` нарушает SRP и делает reducer хаотичным.

**Почему не два отдельных Store?**
Screen 2 и Screen 3 онбординга работают с теми же данными о дате рождения, что и основное приложение. Два Store → дублирование данных → проблемы синхронизации.

### Слои

| Слой | Файл | Ответственность |
|---|---|---|
| UI | `ui/onboarding/*.kt` | Рендеринг, dispatch интентов |
| State | `mvi/AppState.kt` | Единственный источник правды |
| Intent | `mvi/AppIntent.kt` | Все действия пользователя |
| Reducer | `mvi/AppReducer.kt` | Чистая функция state × intent → state |
| Store | `mvi/AppStore.kt` | Side effects, StateFlow, корутины |
| Domain | `domain/BillionSecondsCalculator.kt` | Вычисления (расширяется) |
| Validation | `domain/BirthdayValidator.kt` | Чистая валидация |
| Navigation | `navigation/AppScreen.kt` | sealed class маршрутов |

---

## 2.2 Navigation

**Подход:** `sealed class AppScreen` в `AppState.screen`, переключается через `when` в `App.kt`.

```kotlin
// navigation/AppScreen.kt
sealed class AppScreen {
    data object OnboardingIntro  : AppScreen()
    data object OnboardingInput  : AppScreen()
    data object OnboardingResult : AppScreen()
    data object Main             : AppScreen()
}
```

**`App.kt` после рефакторинга:**

```kotlin
when (state.screen) {
    AppScreen.OnboardingIntro  -> OnboardingIntroScreen(onIntent = store::dispatch)
    AppScreen.OnboardingInput  -> OnboardingInputScreen(state, store::dispatch)
    AppScreen.OnboardingResult -> OnboardingResultScreen(state, store::dispatch)
    AppScreen.Main             -> MainAppContent(state, store::dispatch)
}
```

**Почему не навигационная библиотека (Decompose/Voyager)?**
Для 3–4 экранов state-based навигация идиоматична в KMP и не требует сторонних зависимостей. Переход на Decompose — дело двух часов при росте до 6+ экранов.

**Передача данных между экранами:** через `AppState` — Screen 3 читает `milestoneInstant`, `progressPercent`, `isMilestoneReached` напрямую из state. Никаких аргументов в конструкторах экранов.

---

## 2.3 State модель

```kotlin
// mvi/AppState.kt
data class AppState(
    // --- Navigation ---
    val screen: AppScreen = AppScreen.OnboardingIntro,

    // --- Onboarding Input ---
    val year: Int?           = null,
    val month: Int?          = null,
    val day: Int?            = null,
    val hour: Int            = 12,    // дефолт 12:00 при unknownTime = true
    val minute: Int          = 0,
    val unknownTime: Boolean = false,

    // --- Onboarding Result ---
    val milestoneInstant: Instant?  = null,
    val progressPercent: Float      = 0f,   // (now - birth) / 1_000_000_000
    val isMilestoneReached: Boolean = false,

    // --- Main App ---
    val secondsRemaining: Long  = 0L,
    val showMainResult: Boolean = false,    // внутренняя навигация основного экрана

    val error: String? = null
)
```

**Почему плоская структура, а не вложенные sub-state?**
`copy()` в Kotlin работает только на один уровень глубины. Вложенные data class требуют `copy(onboarding = state.onboarding.copy(...))` — многословно и ошибкоёмко при таком масштабе.

---

## 2.4 Intent / Action

```kotlin
// mvi/AppIntent.kt
sealed class AppIntent {

    // --- Онбординг ---
    data object StartClicked               : AppIntent()  // Экран 1 → 2
    data class  OnboardingDateChanged(
        val year: Int, val month: Int, val day: Int
    )                                      : AppIntent()  // Экран 2: дата
    data class  OnboardingTimeChanged(
        val hour: Int, val minute: Int
    )                                      : AppIntent()  // Экран 2: время
    data object UnknownTimeToggled         : AppIntent()  // Экран 2: переключатель
    data object OnboardingCalculateClicked : AppIntent()  // Экран 2 → 3
    data object OnboardingContinueClicked  : AppIntent()  // Экран 3 → Main

    // --- Основное приложение (без изменений) ---
    data class  DateChanged(val year: Int, val month: Int, val day: Int) : AppIntent()
    data class  TimeChanged(val hour: Int, val minute: Int)              : AppIntent()
    data object CalculateClicked : AppIntent()
    data object ClearClicked     : AppIntent()
}
```

**Почему отдельные `OnboardingDateChanged` и `DateChanged`?**
Одинаковый интент для двух разных экранов означает, что reducer должен проверять `state.screen` для понимания контекста — хрупкая связность. Отдельные имена делают лог интентов читаемым и reducer — исчерпывающим.

**Почему `UnknownTimeToggled`, а не `UnknownTimeChanged(Boolean)`?**
Toggle — это flip булева. Передавать новое значение избыточно и менее идиоматично в MVI.

---

## 2.5 Domain слой

### Расширение `BillionSecondsCalculator`

```kotlin
// Новые методы в domain/BillionSecondsCalculator.kt

// Вычислить процент прогресса (0f..1f)
fun calculateProgress(birthInstant: Instant, now: Instant): Float {
    val elapsed = (now - birthInstant).inWholeSeconds.toFloat()
    return (elapsed / BILLION).coerceIn(0f, 1f)
}

// Атомарный расчёт всех значений (избегает рассинхронизации)
fun computeAll(data: BirthdayData, now: Instant): MilestoneResult

// Новый value object
data class MilestoneResult(
    val milestoneInstant: Instant,
    val progressPercent: Float,
    val isMilestoneReached: Boolean,
    val secondsRemaining: Long
)
```

**Почему не отдельные UseCase-классы?**
В проекте нет DI-фреймворка. Создание класса `CalculateProgressUseCase` — чистая церемония без пользы для MVP. `BillionSecondsCalculator` — уже именованный pure object, добавить 2 метода — правильный масштаб. При введении Koin эти методы тривиально оборачиваются в use case классы.

### Тестирование

- Все методы — чистые функции без зависимостей
- Тесты в `commonTest`: `BillionSecondsCalculatorTest` расширяется новыми кейсами
- Тест: `calculateProgress` при рождении сегодня → `0f`, при дате 31+ лет назад → `>0.98f`

### `BirthdayValidator` (новый)

```kotlin
// domain/BirthdayValidator.kt
object BirthdayValidator {
    sealed class ValidationError {
        data object DateRequired   : ValidationError()
        data object DateInFuture   : ValidationError()
        data object DateInvalidDay : ValidationError()  // Feb 30 и пр.
        data object YearOutOfRange : ValidationError()  // < 1900
    }

    fun validate(year: Int?, month: Int?, day: Int?, now: Instant): ValidationError? { ... }
}
```

Validator живёт в `domain/`, тестируется в `commonTest/BirthdayValidatorTest`.

---

## 2.6 Валидация

| Правило | Условие | Ошибка |
|---|---|---|
| Дата обязательна | `year == null \|\| month == null \|\| day == null` | "Введите дату рождения" |
| Дата в прошлом | `birthInstant >= now` | "Дата рождения не может быть в будущем" |
| Корректный день | `LocalDate(year, month, day)` бросает исключение | "Некорректная дата" |
| Год в диапазоне | `year < 1900` | "Некорректный год" |

Пользователь, рождённый до ~1968 (milestone уже прошёл) — **не** ошибка. Это `isMilestoneReached = true`, экран 3 показывает поздравление.

Валидация выполняется в `AppStore.onboardingCalculate()` — не в reducer, который остаётся чистым.

---

## 2.7 Работа со временем

**Классы:** `kotlinx.datetime` (уже в проекте)

```
LocalDate / LocalDateTime / Instant / TimeZone
```

**Timezone стратегия:**
- Хранение milestone: в UTC (`Instant`) — платформонезависимо
- Отображение пользователю: `milestone.toLocalDateTime(TimeZone.currentSystemDefault())`
- При смене часового пояса на устройстве — отображаемое время сдвигается, сам `Instant` не меняется — корректное поведение

**Edge cases:**
- Год до 1678 → возможен `Instant` overflow в kotlinx-datetime → гард `year < 1900`
- `LocalDate(year, month, day)` для невалидных дат → `IllegalArgumentException` → ловим в `BirthdayValidator`

---

## 2.8 Data Persistence

**Решение:** расширить существующий `BirthdayStorage` двумя методами. Новый `expect/actual` файл не создаём.

```kotlin
// data/BirthdayStorage.kt (интерфейс)
interface BirthdayStorage {
    fun save(data: BirthdayData)
    fun load(): BirthdayData?
    fun clear()
    fun isOnboardingCompleted(): Boolean           // НОВЫЙ
    fun setOnboardingCompleted(value: Boolean)     // НОВЫЙ
}
```

**Ключ в каждой платформе:** `"onboarding_completed"`
- Android: `SharedPreferences.getBoolean` / `putBoolean`
- iOS: `NSUserDefaults.boolForKey` / `setBool`
- Web: `localStorage.getItem` / `setItem`

`clear()` во всех реализациях также очищает `"onboarding_completed"`.

**Когда записывается:**
1. `OnboardingCalculateClicked` → `repository.saveBirthday(data)` (сохраняем дату)
2. `OnboardingContinueClicked` → `repository.setOnboardingCompleted(true)` → переход на `AppScreen.Main`

**Skip onboarding при рестарте — `AppStore.init`:**

```kotlin
init {
    val onboardingDone = repository.isOnboardingCompleted()
    val saved = repository.getBirthday()

    // Миграция существующих пользователей (критично для апдейта, не откладывать)
    if (!onboardingDone && saved != null) {
        repository.setOnboardingCompleted(true)
    }

    if (repository.isOnboardingCompleted() && saved != null) {
        val result = BillionSecondsCalculator.computeAll(saved, now())
        _state.value = AppState(screen = AppScreen.Main, showMainResult = true, ...)
        startTick(result.milestoneInstant)
    }
    // else: остаётся AppScreen.OnboardingIntro (default)
}
```

---

## 2.9 Edge Cases

| Кейс | Обработка |
|---|---|
| Пользователь не знает время | `unknownTime = true` → `hour=12, minute=0` → Screen 3 показывает дисклеймер "Результат приблизительный" |
| Пользователь уже прожил 1 млрд сек | `isMilestoneReached = true` → `progressPercent = 1f` → Screen 3 показывает поздравление |
| Смена часового пояса | Отображаемое время сдвигается (Instant в UTC неизменен) — корректное поведение |
| Изменение системного времени | При рестарте tick пересчитывает с актуального `Clock.System.now()` |
| Приложение убито на Screen 2 | `onboardingCompleted = false`, данные не сохранены → рестарт с Screen 1 (приемлемо для MVP) |
| Невалидный день (Feb 30) | `BirthdayValidator` ловит `IllegalArgumentException` от `LocalDate()` |
| Год < 1900 | Явный гард в `BirthdayValidator` → ошибка "Некорректный год" |
| `onboardingCompleted = false`, но данные есть | Существующий пользователь после апдейта → тихая миграция в `init` |

---

## 2.10 Структура проекта

```
composeApp/src/commonMain/.../billionseconds/
├── navigation/
│   └── AppScreen.kt                           ← NEW
│
├── domain/
│   ├── BillionSecondsCalculator.kt            ← MODIFIED (+calculateProgress, +computeAll)
│   ├── BirthdayValidator.kt                   ← NEW
│   └── model/
│       └── MilestoneResult.kt                 ← NEW
│
├── data/
│   ├── BirthdayStorage.kt                     ← MODIFIED (+2 метода)
│   ├── BirthdayRepository.kt                  ← MODIFIED (+делегаты)
│   └── model/BirthdayData.kt                  ← без изменений
│
├── mvi/
│   ├── AppIntent.kt                           ← NEW (заменяет BirthdayIntent)
│   ├── AppState.kt                            ← NEW (заменяет BirthdayState)
│   ├── AppReducer.kt                          ← NEW (заменяет BirthdayReducer)
│   └── AppStore.kt                            ← NEW (заменяет BirthdayStore)
│
├── ui/
│   ├── onboarding/
│   │   ├── OnboardingIntroScreen.kt           ← NEW
│   │   ├── OnboardingInputScreen.kt           ← NEW
│   │   └── OnboardingResultScreen.kt          ← NEW
│   ├── components/
│   │   └── ProgressBar.kt                     ← NEW (общий компонент)
│   ├── BirthdayScreen.kt                      ← без изменений
│   └── ResultScreen.kt                        ← без изменений
│
├── util/ClockProvider.kt                      ← без изменений
└── App.kt                                     ← MODIFIED (when(state.screen))

Платформенные изменения (симметрично для всех трёх):
androidMain/.../data/AndroidBirthdayStorage.kt ← MODIFIED (+onboarding flag)
iosMain/.../data/IosBirthdayStorage.kt         ← MODIFIED (+onboarding flag)
webMain/.../data/WebBirthdayStorage.kt         ← MODIFIED (+onboarding flag)
```

---

## 2.11 Порядок реализации

### Фаза 1 — Модели и domain (нет изменений UI, всё тестируемо)

1. Создать `MilestoneResult` data class → `domain/model/MilestoneResult.kt`
2. Расширить `BillionSecondsCalculator`: `+calculateProgress()`, `+computeAll()`, внутренний `birthInstantFrom()`
3. Написать тесты на новые методы (`commonTest/BillionSecondsCalculatorTest`)
4. Создать `BirthdayValidator` → `domain/BirthdayValidator.kt`
5. Написать тесты validator (все ветки `ValidationError` + happy path)
6. Создать `AppScreen` sealed class → `navigation/AppScreen.kt`
7. Создать `AppState` data class → `mvi/AppState.kt`
8. Создать `AppIntent` sealed class → `mvi/AppIntent.kt`
9. Создать `AppReducer` pure object → `mvi/AppReducer.kt`
10. Написать тесты reducer (все ветки интентов)

### Фаза 2 — Storage

11. Расширить интерфейс `BirthdayStorage` (+2 метода)
12. Реализовать в `AndroidBirthdayStorage`, `IosBirthdayStorage`, `WebBirthdayStorage`
13. Обновить `clear()` во всех трёх реализациях (добавить очистку флага)
14. Расширить `BirthdayRepository` делегирующими методами

### Фаза 3 — Store

15. Создать `AppStore` → `mvi/AppStore.kt`:
    - `init` блок с миграцией и skip-логикой
    - `dispatch()` → reducer + side effects
    - `onboardingCalculate()`: validate → computeAll → save → update state → navigate
    - `onboardingContinue()`: setCompleted → navigate → startTick
    - Перенести `startTick()` из `BirthdayStore`

### Фаза 4 — UI онбординга

16. `OnboardingIntroScreen.kt` — stateless, кнопка "Начать" → `StartClicked`
17. `OnboardingInputScreen.kt` — `DateInputSection` + `Switch` + `AnimatedVisibility(TimeInputSection)` + текст ошибки + кнопка
18. `ProgressBar.kt` — обёртка над `LinearProgressIndicator` с label "XX.X%"
19. `OnboardingResultScreen.kt` — milestone datetime + `ProgressBar` + дисклеймер (если `unknownTime`) + поздравление (если `reached`) + кнопка

### Фаза 5 — Подключение

20. Изменить `App.kt`: `BirthdayStore` → `AppStore`, `if (showResult)` → `when (screen)`
21. Добавить строки локализации (RU/EN) для всех новых экранов

### Фаза 6 — Cleanup

22. Удалить `BirthdayIntent.kt`, `BirthdayState.kt`, `BirthdayReducer.kt`, `BirthdayStore.kt`
23. Переименовать и расширить тесты: `AppReducerTest`, `BillionSecondsCalculatorTest`, `BirthdayValidatorTest`

---

## 2.12 Что упрощаем в MVP

| Что | Статус | Когда делать |
|---|---|---|
| Back-навигация в онбординге | Defer | После MVP |
| Анимации между экранами | Defer | После MVP |
| Сохранение черновика при kill процесса | Defer | После MVP |
| Decompose / Voyager навигация | Defer | При 6+ экранах |
| Отдельный `OnboardingStore` | Defer | При росте фичи |
| Онбординг повторно из Settings | Defer | По запросу |
| Accessibility (`contentDescription`) | **До App Store** | До релиза |
| Миграция существующих пользователей | **MVP** | Критично для апдейта |

---

## Верификация

### End-to-end тест (ручной)

1. Fresh install → открывается Screen 1 (Intro)
2. "Начать" → Screen 2 (Input)
3. Нажать "Рассчитать" без даты → показывается ошибка
4. Ввести дату, toggle "Не знаю время" → поле времени скрывается
5. "Рассчитать" → Screen 3 с датой milestone + процент + дисклеймер "приблизительно"
6. "Продолжить" → основное приложение
7. Kill → relaunch → онбординг пропущен, сразу основное приложение
8. Дата рождения > 31 года назад → `isMilestoneReached = true` → поздравление на Screen 3

### Юнит тесты

- `AppReducerTest` — все ветки интентов
- `BillionSecondsCalculatorTest` — `calculateProgress`, `computeAll`
- `BirthdayValidatorTest` — все ветки `ValidationError`

### Критические файлы

| Файл | Почему критичен |
|---|---|
| `mvi/AppStore.kt` | Самый сложный файл — вся side-effect логика и init |
| `data/BirthdayStorage.kt` | Изменение интерфейса каскадирует на 3 платформы |
| `App.kt` | Точка интеграции всей навигации |
| `domain/BillionSecondsCalculator.kt` | Расширение domain без breaking changes |
