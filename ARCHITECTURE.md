# Architecture — BillionSeconds

KMP (Kotlin Multiplatform) приложение с поддержкой Android, iOS и Web.  
UI — Compose Multiplatform. Архитектурный паттерн — MVI с многомодульной структурой.

---

## Структура модулей

```
BillionSeconds/
├── build-logic/                  # Convention Gradle-плагины
├── composeApp/                   # :app — точка входа, MVI-слой
├── core/
│   ├── core-domain/              # Доменная логика, модели
│   ├── core-data/                # Storage, Repository, expect/actual
│   ├── core-ui/                  # Переиспользуемые Compose-компоненты
│   └── core-navigation/          # Навигация: AppScreen, NavCommand, AppNavigator
└── feature/
    ├── feature-onboarding/
    ├── feature-countdown/
    ├── feature-lifestats/
    ├── feature-milestones/
    ├── feature-family/
    ├── feature-event/
    ├── feature-timecapsule/
    └── feature-profile/
```

### Граф зависимостей

```
:app
 ├── :core:core-domain
 ├── :core:core-data
 ├── :core:core-ui
 ├── :core:core-navigation
 └── :feature:feature-*  (все 8 модулей)

:core:core-navigation  →  :core:core-domain
:core:core-data        →  :core:core-domain
:core:core-ui          →  (только Compose, без core-зависимостей)

:feature:feature-*     →  :core:core-domain + :core:core-ui
:feature:feature-event        (также) →  :core:core-data
:feature:feature-timecapsule  (также) →  :core:core-data
```

**Правило:** фичи никогда не зависят друг от друга. Только `:app` интегрирует всё вместе.

---

## Convention Plugins (`build-logic/`)

Два плагина устраняют дублирование Gradle-конфигурации.

### `kmp-core-convention`
Для `core-domain` и `core-navigation` — чистый KMP без Compose UI.
Таргеты: `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `js`, `wasmJs`.

### `kmp-feature-convention`
Для всех `feature-*` и `core-ui`, `core-data` модулей — KMP + Compose Multiplatform.
Добавляет `compose.runtime`, `compose.foundation`, `compose.material3`.

Пример `build.gradle.kts` фича-модуля:
```kotlin
plugins { id("kmp-feature-convention") }

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.coreDomain)
            implementation(projects.core.coreUi)
        }
    }
}

android { namespace = "com.example.billionseconds.countdown" }
```

---

## Слои архитектуры

### core-domain
Чистая бизнес-логика без Compose и Android-зависимостей.

| Файл | Назначение |
|------|-----------|
| `BillionSecondsCalculator` | Считает milestone (ДР + 1 млрд секунд), прогресс, обратный отсчёт |
| `BirthdayValidator` | Валидирует дату рождения, возвращает `sealed class ValidationError` |
| `data/model/BirthdayData` | Дата и время рождения |
| `data/model/FamilyProfile` | Профиль члена семьи (@Serializable, включает RelationType) |
| `data/model/AppSettings` | Настройки пользователя (@Serializable) |
| `domain/event/model/*` | `EventDomainModel`, `EventMode`, `EventEligibilityStatus`, `EventSource` |
| `domain/model/*` | `MilestoneResult`, `TimeCapsule`, `UnlockCondition`, `CapsuleStatus` |

### core-data
Storage-интерфейсы + Repository + платформенные реализации через `expect/actual`.

**Интерфейсы (commonMain):**
```kotlin
interface BirthdayStorage { fun save(data); fun load(): BirthdayData?; fun clear(); ... }
interface FamilyProfileStorage { ... }
interface AppSettingsStorage { ... }
interface TimeCapsuleStorage { ... }
interface EventHistoryStorage { ... }

// Фабрики — expect/actual:
expect fun createBirthdayStorage(): BirthdayStorage
expect fun createFamilyProfileStorage(): FamilyProfileStorage
// ...
```

**Реализации:**

| Платформа | Механизм хранения |
|-----------|------------------|
| Android (`androidMain`) | `SharedPreferences` |
| iOS (`iosMain`) | `NSUserDefaults` |
| Web (`webMain`) | `localStorage` |

**Repository** — тонкая обёртка над Storage, не содержит бизнес-логики:
```kotlin
class BirthdayRepository(private val storage: BirthdayStorage) {
    fun getBirthday(): BirthdayData? = storage.load()
    fun saveBirthday(data: BirthdayData) = storage.save(data)
    fun isOnboardingCompleted(): Boolean = storage.isOnboardingCompleted()
    // ...
}
```

### core-ui
Переиспользуемые Compose-компоненты без бизнес-логики.

| Файл | Описание |
|------|---------|
| `AppColors` | Цветовая палитра приложения |
| `AppConstants` | Константы (размеры, отступы) |
| `ComingSoonSheet` | Bottom sheet "Скоро" |
| `ProgressBar` | Кастомный прогресс-бар |
| `DateInputSection` | `expect` — платформенный DatePicker (Android/iOS/Web) |
| `PlatformBackHandler` | `expect` — обработчик системной кнопки "Назад" |

### core-navigation
Весь навигационный контракт без зависимости от UI-фреймворков.

---

## Навигация

### Модель экранов (`AppScreen`)
```kotlin
sealed class AppScreen {
    data object OnboardingIntro   : AppScreen()
    data object OnboardingInput   : AppScreen()
    data object OnboardingResult  : AppScreen()
    data class  Main(val tab: MainTab = MainTab.Home) : AppScreen()
    data class  EventScreen(val profileId: String, val source: EventSource) : AppScreen()
    data object TimeCapsule       : AppScreen()
}
```

### Вкладки главного экрана (`MainTab`)
```kotlin
enum class MainTab(val index: Int, val label: String) {
    Home(0, "Главная"),         // CountdownScreen
    Stats(1, "Статистика"),     // LifeStatsScreen
    Family(2, "Семейный"),      // FamilyScreen
    Milestones(3, "Достижения"),// MilestonesScreen
    Profile(4, "Профиль")       // ProfileScreen
}
```

### Команды навигации (`NavCommand`)
```kotlin
sealed interface NavCommand {
    data class  Forward(val screen: AppScreen) : NavCommand   // push
    data class  Replace(val screen: AppScreen) : NavCommand   // replace top
    data class  NewRoot(val screen: AppScreen) : NavCommand   // clear stack
    data object Back                           : NavCommand   // pop
    data class  BackTo(val screenClass: KClass<out AppScreen>, val inclusive: Boolean) : NavCommand
    data class  SwitchTab(val tab: MainTab)    : NavCommand
    data object FinishFlow                     : NavCommand   // alias Back
}
```

### `AppNavigator`
Управляет back stack и текущим экраном:
```kotlin
class AppNavigator {
    val current: StateFlow<AppScreen>   // текущий экран (наблюдаемый)
    val canGoBack: Boolean
    val currentTab: MainTab?
    fun execute(command: NavCommand)    // выполняет команду навигации
}
```
Back stack — `ArrayDeque<AppScreen>`. `App()` подписывается на `navigator.current` и рендерит соответствующий экран.

---

## MVI архитектура

Весь MVI-слой живёт в модуле `:app` (`composeApp/src/commonMain/mvi/`).

```
User Action
    │
    ▼
FeatureAction  (определён в feature-модуле)
    │  ActionAdapter (в :app)
    ▼
AppIntent
    │
    ▼
AppStore.dispatch()
    ├──► AppReducer.reduce()  →  новый AppState  →  UI обновляется
    └──► handleIntent()
             ├──► navigator.execute(NavCommand)  →  смена экрана
             └──► emit(AppEffect)  →  App() обрабатывает (share, dialog, ...)
```

### `AppState`
Единый контейнер состояния всего приложения:
```kotlin
data class AppState(
    // Поля онбординга
    val year: Int?, val month: Int?, val day: Int?,
    val hour: Int, val minute: Int, val unknownTime: Boolean,
    val milestoneInstant: Instant?, val progressPercent: Float,
    val isMilestoneReached: Boolean, val error: String?,

    // UiState каждой фичи
    val countdown:   CountdownUiState   = CountdownUiState(),
    val lifeStats:   LifeStatsUiState   = LifeStatsUiState(),
    val milestones:  MilestonesUiState  = MilestonesUiState(),
    val family:      FamilyUiState      = FamilyUiState(),
    val profile:     ProfileUiState     = ProfileUiState(),
    val event:       EventUiState       = EventUiState(),
    val timeCapsule: TimeCapsuleUiState = TimeCapsuleUiState(),
) {
    // Вычисляемое свойство — собирает поля онбординга в объект
    val onboarding: OnboardingUiState get() = OnboardingUiState(year, month, ...)
}
```

### `AppStore`
```kotlin
class AppStore(
    val navigator: AppNavigator,
    private val repository: BirthdayRepository,
    private val familyRepository: FamilyProfileRepository,
    private val settingsRepository: AppSettingsRepository,
    private val eventHistoryRepository: EventHistoryRepository
) {
    val state: StateFlow<AppState>
    val effect: Flow<AppEffect>

    fun dispatch(intent: AppIntent)
    fun dispose()   // отменяет CoroutineScope
}
```
Внутри `dispatch`: сначала применяется `AppReducer` (синхронно), затем выполняются side-effects (асинхронно в `scope`).

### `AppEffect`
Одноразовые события для `App()`:
```kotlin
sealed class AppEffect {
    data object ExitApp
    data class  ShareText(val text: String)
    data class  ShowComingSoon(val feature: String)
    data class  ShowMilestoneCelebration(val milestoneId: String)
    data class  NavigateToEventScreen(val profileId: String, val source: EventSource)
    data object CloseEventScreen
    data class  ShareEventPayload(val payload: EventSharePayload)
    // ... и другие
}
```

---

## Adapter Pattern

Проблема: фича-модули не могут импортировать `AppIntent` (он в `:app`) — это создало бы циклическую зависимость.

**Решение:** каждый фича-модуль определяет свой `sealed class *Action`. В `:app` создаётся функция-адаптер, которая маппит `FeatureAction → AppIntent`.

```kotlin
// feature-countdown — определяет свои действия:
sealed class CountdownAction {
    data object ScreenStarted      : CountdownAction()
    data object ShareClicked       : CountdownAction()
    data object AddFamilyClicked   : CountdownAction()
    // ...
}

// :app — адаптер:
fun countdownAdapter(dispatch: (AppIntent) -> Unit): (CountdownAction) -> Unit = { action ->
    when (action) {
        CountdownAction.ScreenStarted    -> dispatch(AppIntent.CountdownScreenStarted)
        CountdownAction.ShareClicked     -> dispatch(AppIntent.ShareClicked)
        CountdownAction.AddFamilyClicked -> dispatch(AppIntent.AddFamilyClicked)
        // ...
    }
}

// MainScaffold — использование:
CountdownScreen(
    uiState  = state.countdown,
    onAction = countdownAdapter(onIntent)   // onIntent = store::dispatch
)
```

Список адаптеров в `:app/mvi/`:
- `countdownAdapter`
- `lifeStatsAdapter`
- `milestonesAdapter`
- `familyAdapter`
- `profileAdapter`
- `onboardingAdapter`
- `eventAdapter`
- `timeCapsuleAdapter`

---

## Точки входа

### `App.kt` (commonMain)
Корневой Composable. Создаёт `AppStore`, подписывается на `state` и `effect`, рендерит текущий экран:

```kotlin
@Composable
fun App() {
    val store = remember {
        AppStore(
            repository             = BirthdayRepository(createBirthdayStorage()),
            familyRepository       = FamilyProfileRepository(createFamilyProfileStorage()),
            settingsRepository     = AppSettingsRepository(createAppSettingsStorage()),
            eventHistoryRepository = EventHistoryRepository(createEventHistoryStorage())
        )
    }
    val state         by store.state.collectAsState()
    val currentScreen by store.navigator.current.collectAsState()

    // Одноразовые эффекты
    LaunchedEffect(store) {
        store.effect.collect { effect ->
            when (effect) {
                is AppEffect.ShareText    -> shareText(effect.text)
                is AppEffect.ExitApp      -> exitApp()
                // ...
            }
        }
    }

    MaterialTheme {
        when (val screen = currentScreen) {
            AppScreen.OnboardingIntro  -> OnboardingIntroScreen(onAction = onboardingAdapter(store::dispatch))
            AppScreen.OnboardingInput  -> OnboardingInputScreen(uiState = state.onboarding, ...)
            AppScreen.OnboardingResult -> OnboardingResultScreen(...)
            is AppScreen.Main          -> MainScaffold(state, screen.tab, store::dispatch)
            is AppScreen.EventScreen   -> EventScreen(state.event, eventAdapter(store::dispatch))
            AppScreen.TimeCapsule      -> TimeCapsuleScreen(state.timeCapsule, timeCapsuleAdapter(store::dispatch))
        }
    }
}
```

### `MainScaffold.kt`
Контейнер главного экрана с 5 вкладками и `BottomBar`:

```kotlin
@Composable
fun MainScaffold(state: AppState, selectedTab: MainTab, onIntent: (AppIntent) -> Unit) {
    Box(Modifier.fillMaxSize()) {
        when (selectedTab) {
            MainTab.Home       -> CountdownScreen(state.countdown,  countdownAdapter(onIntent))
            MainTab.Stats      -> LifeStatsScreen(state.lifeStats,  lifeStatsAdapter(onIntent))
            MainTab.Family     -> FamilyScreen(state.family,        familyAdapter(onIntent))
            MainTab.Milestones -> MilestonesScreen(state.milestones, milestonesAdapter(onIntent))
            MainTab.Profile    -> ProfileScreen(state.profile,      profileAdapter(onIntent))
        }
        BottomBar(selectedTab) { tab -> onIntent(AppIntent.TabSelected(tab)) }
    }
}
```

### Платформенные точки входа

| Платформа | Файл | Точка входа |
|-----------|------|------------|
| Android | `androidMain/MainActivity.kt` | `setContent { App() }` |
| iOS | `iosMain/MainViewController.kt` | `ComposeUIViewController { App() }` |
| Web (JS/WASM) | `webMain/main.kt` | `ComposeViewport { App() }` |

---

## Пример полного потока

**Сценарий: пользователь переключает вкладку Family**

```
1. BottomBar.onTabSelected(MainTab.Family)
   → onIntent(AppIntent.TabSelected(MainTab.Family))

2. AppStore.dispatch(AppIntent.TabSelected(Family))
   → AppReducer.reduce(state, intent) 
   → state.copy(family = state.family.copy(isActive = true), ...)
   → navigator.execute(NavCommand.SwitchTab(Family))

3. AppNavigator обновляет _current
   → currentScreen = AppScreen.Main(tab = MainTab.Family)

4. App() видит новый currentScreen
   → рендерит MainScaffold(selectedTab = MainTab.Family)

5. MainScaffold рендерит FamilyScreen(
       uiState = state.family,
       onAction = familyAdapter(onIntent)
   )

6. FamilyScreen отображается
```

---

## Специальные паттерны

### triggerCelebration через state (не SharedFlow)
`EventScreen` может показывать анимацию праздника. Так как `AppEffect` недоступен в фича-модуле, используется флаг в `EventUiState`:

```kotlin
data class EventUiState(
    val triggerCelebration: Boolean = false,  // AppStore ставит true
    // ...
)

// EventScreen реагирует:
LaunchedEffect(uiState.triggerCelebration) {
    if (uiState.triggerCelebration) {
        celebrationVisible = true
        onAction(EventAction.CelebrationDisplayed)  // сбрасывает флаг через reducer
    }
}
```

### expect/actual для платформо-зависимого кода
Используется в двух местах:

1. **Storage** (`core-data`) — разные механизмы хранения на каждой платформе.
2. **UI компоненты** (`core-ui`) — `DateInputSection` и `PlatformBackHandler` имеют платформенные реализации.

```kotlin
// commonMain:
expect fun createBirthdayStorage(): BirthdayStorage

// androidMain:
actual fun createBirthdayStorage(): BirthdayStorage = AndroidBirthdayStorage()

// iosMain:
actual fun createBirthdayStorage(): BirthdayStorage = IosBirthdayStorage()
```
