# BillionSeconds — Implementation Plan

Платформы: **Android + iOS + Web (WASM)** одновременно через KMP + Compose Multiplatform.

---

## Стек и зависимости

### Добавить в `libs.versions.toml`

```toml
[versions]
kotlinxDatetime = "0.6.1"
room = "2.7.0-rc04"
sqlite = "2.5.0-rc01"
ksp = "2.3.0-2.0.1"

[libraries]
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinxDatetime" }
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

### В `composeApp/build.gradle.kts`

```kotlin
// plugins
alias(libs.plugins.ksp)
alias(libs.plugins.room)

// commonMain dependencies
implementation(libs.kotlinx.datetime)

// androidMain dependencies
implementation(libs.room.runtime)
implementation(libs.sqlite.bundled)

// ksp (Android only — Room кодогенерация)
add("kspAndroid", libs.room.compiler)

// room schema location
room { schemaDirectory("$projectDir/schemas") }
```

> iOS и Web используют **in-memory хранилище** (UserDefaults через expect/actual при желании).
> Room подключается только к Android через `kspAndroid`.

---

## Структура файлов (commonMain)

```
composeApp/src/commonMain/kotlin/com/example/billionseconds/
│
├── domain/
│   └── BillionSecondsCalculator.kt       # Вся математика
│
├── data/
│   ├── BirthdayStorage.kt                # expect interface
│   ├── BirthdayRepository.kt             # репозиторий поверх storage
│   └── model/
│       └── BirthdayData.kt               # data class (дата + время)
│
├── mvi/
│   ├── BirthdayIntent.kt                 # sealed class действий пользователя
│   ├── BirthdayState.kt                  # data class состояния UI
│   ├── BirthdayReducer.kt                # чистая функция state + intent → state
│   └── BirthdayStore.kt                  # ViewModel-подобный объект (StateFlow)
│
├── ui/
│   ├── BirthdayScreen.kt                 # экран ввода даты
│   ├── ResultScreen.kt                   # экран результата (countdown / поздравление)
│   └── components/
│       ├── DatePickerField.kt            # expect composable
│       └── TimePickerField.kt            # expect composable
│
├── util/
│   ├── DateTimeFormatter.kt              # форматирование дат для UI
│   └── InstantExtensions.kt             # вспомогательные extension-функции
│
└── App.kt                                # Navigation host (замена текущего)
```

---

## Шаг 1 — Domain Layer

**Файл:** `domain/BillionSecondsCalculator.kt`

```kotlin
object BillionSecondsCalculator {
    const val BILLION = 1_000_000_000L

    fun calculateMilestone(birthInstant: Instant): Instant =
        birthInstant.plus(BILLION, DateTimeUnit.SECOND)

    fun secondsUntil(milestone: Instant, now: Instant): Long =
        (milestone - now).inWholeSeconds

    fun isReached(milestone: Instant, now: Instant): Boolean =
        now >= milestone
}
```

Покрывается юнит-тестами в `commonTest` — никаких платформенных зависимостей.

---

## Шаг 2 — Data Layer

### `data/model/BirthdayData.kt`
```kotlin
data class BirthdayData(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int
)
```

### `data/BirthdayStorage.kt` (expect/actual)
```kotlin
// commonMain
expect class BirthdayStorage {
    fun save(data: BirthdayData)
    fun load(): BirthdayData?
    fun clear()
}
```

| Платформа | Реализация |
|---|---|
| Android | Room Database (`@Entity`, `@Dao`, `@Database`) |
| iOS | NSUserDefaults через Kotlin/Native interop |
| Web (WASM) | `window.localStorage` через `kotlinx.browser` |

### `data/BirthdayRepository.kt`
```kotlin
class BirthdayRepository(private val storage: BirthdayStorage) {
    fun getBirthday(): BirthdayData? = storage.load()
    fun saveBirthday(data: BirthdayData) = storage.save(data)
}
```

---

## Шаг 3 — MVI Layer

### `mvi/BirthdayIntent.kt`
```kotlin
sealed class BirthdayIntent {
    data class DateSelected(val year: Int, val month: Int, val day: Int) : BirthdayIntent()
    data class TimeSelected(val hour: Int, val minute: Int) : BirthdayIntent()
    object CalculateClicked : BirthdayIntent()
    object ClearClicked : BirthdayIntent()
}
```

### `mvi/BirthdayState.kt`
```kotlin
data class BirthdayState(
    val selectedYear: Int? = null,
    val selectedMonth: Int? = null,
    val selectedDay: Int? = null,
    val selectedHour: Int = 0,
    val selectedMinute: Int = 0,
    val milestoneInstant: Instant? = null,
    val secondsRemaining: Long? = null,
    val isMilestoneReached: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### `mvi/BirthdayStore.kt`
```kotlin
class BirthdayStore(private val repository: BirthdayRepository) : ViewModel() {
    private val _state = MutableStateFlow(BirthdayState())
    val state: StateFlow<BirthdayState> = _state.asStateFlow()

    init {
        // Загрузить сохранённую дату при старте
        repository.getBirthday()?.let { saved ->
            // восстановить состояние и пересчитать
        }
        // Запустить live-обновление счётчика каждую секунду
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { recalculate(it) }
            }
        }
    }

    fun dispatch(intent: BirthdayIntent) {
        _state.update { BirthdayReducer.reduce(it, intent) }
        if (intent is BirthdayIntent.CalculateClicked) calculate()
    }
}
```

---

## Шаг 4 — UI Layer

### Навигация (App.kt)
Два экрана через `remember { mutableStateOf<Screen> }`:
- `Screen.Input` — ввод даты
- `Screen.Result` — результат / праздник

Compose Navigation не нужен — переходов всего два.

### `ui/BirthdayScreen.kt`
- `DatePickerField` — платформенный пикер даты
- `TimePickerField` — платформенный пикер времени  
- Кнопка "Рассчитать"
- Валидация: дата не в будущем

### `ui/ResultScreen.kt`
- Если `!isMilestoneReached`: живой счётчик секунд + дата события
- Если `isMilestoneReached`: поздравительный экран (анимация + текст)
- Кнопка "Изменить дату"

### Platform DatePicker/TimePicker (expect/actual composables)

| Платформа | Реализация |
|---|---|
| Android | `DatePickerDialog` / `TimePickerDialog` из Material3 |
| iOS | `UIDatePicker` через `UIKitView` |
| Web | HTML `<input type="date">` / `<input type="time">` через `HtmlActualNode` или кастомный Compose-пикер |

---

## Шаг 5 — Локализация

**Файлы:**
- `composeResources/values/strings.xml` — EN (default)
- `composeResources/values-ru/strings.xml` — RU

**Ключевые строки:**
```xml
<string name="title">Billion Seconds</string>
<string name="enter_birthday">Enter your birthday</string>
<string name="calculate">Calculate</string>
<string name="your_milestone">Your billion seconds moment</string>
<string name="time_remaining">Time remaining</string>
<string name="congratulations">Congratulations! 🎉</string>
<string name="milestone_reached">You've lived a billion seconds!</string>
<string name="seconds_left">%1$d seconds left</string>
```

---

## Шаг 6 — Тесты

| Тест | Где | Что проверяем |
|---|---|---|
| `BillionSecondsCalculatorTest` | `commonTest` | Корректность расчёта milestone |
| `BillionSecondsCalculatorTest` | `commonTest` | Граничный случай: milestone = сейчас |
| `BirthdayReducerTest` | `commonTest` | Reducer — чистая функция, детерминирована |
| `DateTimeFormatterTest` | `commonTest` | Форматирование дат |

---

## Порядок реализации (очерёдность задач)

```
[ ] 1. Обновить зависимости (build.gradle.kts, libs.versions.toml)
[ ] 2. BirthdayData + BillionSecondsCalculator + юнит-тесты
[ ] 3. BirthdayStorage expect/actual (Android Room, iOS UserDefaults, Web localStorage)
[ ] 4. BirthdayRepository
[ ] 5. MVI: Intent, State, Reducer, Store
[ ] 6. BirthdayScreen UI (без пикеров — сначала с текстовым вводом)
[ ] 7. ResultScreen UI (countdown + поздравление)
[ ] 8. App.kt: навигация между экранами
[ ] 9. Platform DatePicker/TimePicker (Android)
[ ] 10. Platform DatePicker/TimePicker (iOS)
[ ] 11. Platform DatePicker/TimePicker (Web)
[ ] 12. Локализация RU/EN
[ ] 13. Полная проверка на всех трёх платформах
```

---

## Проверка результата

- **Android**: запустить эмулятор/устройство, ввести дату, убедиться что countdown работает в реальном времени, данные сохраняются после перезапуска
- **iOS**: запустить через Xcode симулятор, то же самое
- **Web**: `./gradlew wasmJsBrowserDevelopmentRun`, проверить в браузере
- **Тесты**: `./gradlew :composeApp:allTests`
