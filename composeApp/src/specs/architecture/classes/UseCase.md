# UseCase

## Определение

**UseCase** — это класс, инкапсулирующий бизнес-логику приложения. UseCase находится между Repository и ViewModel в архитектурной иерархии.

UseCase — редкий класс в разработке. Не следует создавать UseCase для каждой операции. Используйте его только при наличии веских причин.

## Когда нужен UseCase

### Тип 1: Комбинированная логика (для переиспользования)

Используйте этот тип, когда бизнес-логика:
- Ссылается на **два или более репозитория** одновременно
- **Переиспользуется** в нескольких местах или фичах

**Критерии необходимости:**
- Бизнес-логика используется в нескольких фичах
- Логика требует взаимодействия с несколькими репозиториями

**Реализация:**
1. Интерфейс UseCase размещается в слое `entity` (публичный API)
2. Реализация (`*Impl`) размещается в слое `model` (internal)
3. DI-конфигурация для внедрения зависимости
4. Использование в feature через entity-уровень

**Пример хорошего применения:**

```kotlin
// entity слой (публичный интерфейс)
public interface NotificationCountUseCase {
    public suspend fun getNotificationCount(): Int
}

// model слой (internal реализация)
internal class NotificationCountUseCaseImpl(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val notificationRepository: NotificationRepository,
) : NotificationCountUseCase {
    override suspend fun getNotificationCount(): Int {
        val config = remoteConfigRepository.getConfiguration()?.providersConfig?.notificationCenterConfig
        if (config != null) {
            val url = config.baseURL + config.countEndpoint
            return try {
                withTimeout(TIMEOUT_MILLIS) {
                    notificationRepository.getNotificationCount(url)
                }
            } catch (_: Exception) {
                0
            }
        }
        return 0
    }
}
```

### Тип 2: Декомпозиция ViewModel

Используйте этот тип, когда нужно вынести значимый кусок кода из ViewModel для улучшения читаемости.

**Реализация:**
1. Класс UseCase объявляется как `internal`
2. **Не добавляется** в DI-контейнер
3. Инстанс создается напрямую в конструкторе ViewModel через `get()`

**Пример хорошего применения:**

```kotlin
internal class SaveTaskUseCase {
    suspend fun someAction(task: Pair<String, String>) {
        // Много кода
    }
}

// Использование в ViewModel
internal class SampleViewModel(
    private val saveTaskUseCase: SaveTaskUseCase,
    // ...
) : ViewModel() {
    fun createNewTask() {
        viewModelScope.launch {
            val newTask = Pair(uiState.value.title, uiState.value.description)
            saveTaskUseCase.someAction(newTask)
        }
    }
}
```

## Когда НЕ нужен UseCase

### 1. Один репозиторий
Если логика работает только с одним репозиторием:
- Добавьте функцию прямо в интерфейс репозитория
- Или разместите код в ViewModel

### 2. Простая логика
Если логика простая и понятная:
- Пишите её прямо в ViewModel
- Дублирование кода не всегда является проблемой в больших системах

### 3. Сложная логика
Если логика сложная, но не требует нескольких репозиториев:
- Вынесите её в extension-функцию
- Или разместите в репозитории

## Примеры из кодовой базы

### Хорошие примеры

#### 1. NotificationCountUseCase (Комбинированная логика)
**Почему хорошо:**
- Использует два репозитория: `RemoteConfigRepository` и `NotificationRepository`
- Переиспользуется в нескольких местах
- Имеет публичный интерфейс в entity слое

```kotlin
public interface NotificationCountUseCase {
    public suspend operator fun invoke(): Int
}

internal class NotificationCountUseCaseImpl(
    private val remoteConfigRepository: RemoteConfigRepository,
    private val notificationRepository: NotificationRepository,
) : NotificationCountUseCase { /* ... */ }
```

#### 2. AddProductUseCase (Комбинированная логика)
**Почему хорошо:**
- Комбинирует логику из репозитория и других UseCase
- Содержит бизнес-правила (проверка лимитов)
- Переиспользуется в разных ViewModel

```kotlin
public class AddProductUseCase(
    private val repository: ShoppingListRepository,
    private val shopListUseListIdToggle: ShopListUseListIdToggle,
    private val createShoppingListUseCase: CreateNewShoppingListUseCase,
) {
    public suspend fun add(listId: Long?, plu: Plu, step: Double): Result {
        if (isShoppingListLimitReached(listId, plu)) return Result.LIMIT_REACHED
        // ...
    }
}
```

#### 3. SaveTaskUseCase (Декомпозиция ViewModel)
**Почему хорошо:**
- Internal класс, не в DI
- Выносит логику сохранения из ViewModel
- Создается прямо в конструкторе

```kotlin
internal class SaveTaskUseCase {
    suspend fun someAction(task: Pair<String, String>) {
        // Много кода
    }
}
```

### Плохие примеры

#### 1. GetDefaultListNameUseCase (Избыточный UseCase)
**Почему плохо:**
- Использует только локальные данные (дата, ресурсы)
- Не взаимодействует с репозиториями
- Логика слишком простая для отдельного класса

```kotlin
public class GetDefaultListNameUseCase {
    @OptIn(ExperimentalTime::class)
    public suspend operator fun invoke(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val day = now.day
        val month = now.getMonthNameRu()
        return getString(Res.string.default_list_name_base, day, month)
    }
}
```

**Как лучше:** Вынести в extension-функцию или оставить в ViewModel.

#### 2. ProvideChatDeeplinkUseCase (Избыточный UseCase)
**Почему плохо:**
- Использует только один репозиторий (`RemoteConfigRepository`)
- Логика простая (получение конфигурации и формирование URL)
- Не переиспользуется

```kotlin
public interface ProvideChatDeeplinkUseCase {
    public operator fun invoke(): String?
}

public class ProvideChatDeeplinkUseCaseImpl(
    private val remoteConfigRepository: RemoteConfigRepository,
) : ProvideChatDeeplinkUseCase {
    override fun invoke(): String? {
        val configuration = remoteConfigRepository.getConfiguration() ?: return null
        val url = configuration.tabsConfigs.find { it.id == FEEDBACK_TAB_ID }?.url ?: return null
        return FEEDBACK_DEEPLINK_TEMPLATE + (url + FEEDBACK_CHAT_PATH_PART).encode()
    }
}
```

**Как лучше:** Добавить метод в `RemoteConfigRepository` или оставить в ViewModel.

## Правила именования

- Используйте суффикс `UseCase` в названии класса
- Используйте глагол или глагольную фразу: `Get`, `Provide`, `Add`, `Update`, `Delete`, `Sync`
- Примеры: `NotificationCountUseCase`, `AddProductUseCase`, `SyncShoppingListsUseCase`

## Структура файлов

### Для комбинированной логики (переиспользуемая)

```
tc-shared/myFeature/
├── entity/
│   └── MyUseCase.kt              # public interface
└── model/
    └── internal/
        └── MyUseCaseImpl.kt          # internal implementation
```

### Для декомпозиции ViewModel

```
tc-shared/myFeature/
└── feature/
    └── internal/
        └── MyUseCase.kt              # internal class
```

## DI-конфигурация

### Для комбинированной логики

```kotlin
// model/di/MyFeatureDataModule.kt
public fun myFeatureDataModule(): Module = module {
    factory<MyUseCase> {
        MyUseCaseImpl(
            repository1 = get(),
            repository2 = get(),
        )
    }
}
```

### Для декомпозиции ViewModel

**НЕ добавляйте в DI!** Создавайте инстанс напрямую:

```kotlin
viewModel {
    MyViewModel(
        myUseCase = MyUseCase(),  // Прямое создание
        repository = get(),
    )
}
```

## Тестирование

НАДО ДОПИСАТЬ

```

## Чек-лист для создания UseCase

Перед созданием UseCase ответьте на вопросы:

1. **Использует ли логика несколько репозиториев?**
   - Да → Рассмотрите UseCase (Тип 1)
   - Нет → Перейдите к вопросу 2

2. **Переиспользуется ли логика в нескольких фичах?**
   - Да → Рассмотрите UseCase (Тип 1)
   - Нет → Перейдите к вопросу 3

3. **Усложняет ли логика ViewModel?**
   - Да → Рассмотрите UseCase (Тип 2)
   - Нет → Оставьте в ViewModel

4. **Можно ли вынести логику в extension или репозиторий?**
   - Да → Используйте extension или репозиторий
   - Нет → Рассмотрите UseCase

## Резюме для ИИ

При анализе кода и принятии решений о создании UseCase:

1. **Приоритет:** Избегайте создания UseCase без веских причин
2. **Тип 1 (Комбинированная логика):**
   - Проверьте: 2+ репозитория И переиспользование
   - Интерфейс в entity, реализация в model
   - Добавьте в DI
3. **Тип 2 (Декомпозиция ViewModel):**
   - Проверьте: значимый кусок кода в ViewModel
   - Internal класс, НЕ в DI
   - Создавайте через явное объяление в конструкторе ViewModel
4. **Альтернативы:**
   - 1 репозиторий → функция в репозитории или ViewModel
   - Простая логика → ViewModel
   - Сложная логика → extension или репозиторий
