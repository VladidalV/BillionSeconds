# Инструкции по написанию тестов

## Обзор

Этот документ содержит инструкции для написания unit-тестов в кодовой базе. Следуйте этим рекомендациям при добавлении или изменении кода, который требует покрытия тестами.

## Расположение тестов

Размещайте все unit-тесты в source sets `androidUnitTest` (не `commonUnitTest` или `test`).

## Когда писать тесты

### Всегда пишите тесты для:

**1. Бизнес-логика:**
- Классы UseCase / Interactor
- Реализации репозиториев (не интерфейсы)
- Доменные сервисы
- Конечные автоматы, Reducers, ViewModels с логикой

**2. Преобразование данных:**
- Классы Mapper (DTO → Домен, Домен → UI)
- Классы Parser / Decoder
- Логика сериализации

**3. Валидация и обработка:**
- Классы Validator
- Форматтеры (даты, валюта, номера телефонов)
- Обработчики ошибок с разветвлённой логикой

**4. Утилиты:**
- Функции-расширения с логикой (не просто обёртки)
- Вспомогательные/утилитные классы
- Алгоритмы и вычисления

**5. Операции с данными:**
- Кеширование с логикой инвалидации
- Пагинация с логикой
- Фильтрация / сортировка

### Не пишите тесты для:

**1. Только UI:**
- Fragment / Activity / ViewController (UI часть)
- Composable / SwiftUI View
- XML-разметки, Storyboards, XIBs
- Adapters/ViewHolders без логики
- UI-стили и темы

**2. Ресурсы и конфигурация:**
- Строки локализации
- Цвета, размеры, шрифты
- Иконки и изображения
- build.gradle, Podfile, Package.swift
- Правила ProGuard, Info.plist

**3. Структуры без поведения:**
- Data class / struct без методов
- Enum без связанной логики
- Интерфейсы / протоколы (объявления)
- DI-модули (provides/binds)

**4. Тривиальные изменения:**
- Переименование без изменения поведения
- Форматирование кода
- Обновление зависимостей (версии)
- Удаление неиспользуемого кода

## Требования к покрытию тестами

При написании тестов для бизнес-логики:
- **Тестируйте только самые важные сценарии** — обычно от 2 до 5 ключевых случаев, которые покрывают:
    - Основной успешный путь (основной поток успеха),
    - Один критический случай ошибки (например, сбой сети, неверный ввод),
    - Максимум один осмысленный граничный или альтернативный случай (например, пустой ответ, граничное условие).
- **НЕ включайте тривиальные, избыточные или малоценные сценарии** (например, "метод вызывается с null", если это не задокументированный контракт).
- Покрывайте все ветки и граничные случаи
- Проверяйте изменения состояния
- Тестируйте граничные условия
- Мокайте все внешние зависимости

## Контекст

- **Парадигма:** Behavior-Driven Development (BDD)
- **Язык:** Kotlin
- **Тестовый фреймворк:** Kotlin Test
- **Фреймворк для моков:** Mockito (через mockito-kotlin)
- **Инструменты для асинхронного тестирования:** kotlinx-coroutines-test (StandardTestDispatcher, runTest, advanceUntilIdle)

## Примеры

### Писать тесты для:

```kotlin
class ValidateEmailUseCase {
    fun execute(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            !email.contains("@") -> ValidationResult.Error("Invalid email format")
            else -> ValidationResult.Success
        }
    }
}
```
→ Валидация бизнес-логики, тесты требуются

### Не писать тесты для:

```kotlin
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
) {
    val state by viewModel.state.collectAsState()
    Column {
        Text(state.userName)
        Button(onClick = { viewModel.onLogoutClick() }) {
            Text("Logout")
        }
    }
}
```
→ Только UI код, тесты не требуются

### Не писать тесты для:

```kotlin
data class UserDto(
    val id: String,
    val name: String,
    val email: String
)
```
→ DTO без логики, тесты не требуются

## Пример использования

Когда пользователь просит создать тест для класса такого вида:

```kotlin
class ProductListViewModel(
    private val repository: ProductRepository,
    coroutineScope: CoroutineScope,
) : ViewModel(coroutineScope) {

    private val productsFlow = flow {
        emit(ProductState.Loading)
        runCatching {
            repository.getProducts()
        }.fold(
            onSuccess = { products ->
                emit(ProductState.Success(products))
            },
            onFailure = { error ->
                emit(ProductState.Error(error.message ?: "Unknown error"))
            }
        )
    }

    val uiState: StateFlow<ProductState> = productsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ProductState.Loading
    )
}
```

1. **Happy path**: успешная загрузка продуктов автоматически запускается при инициализации
2. **Случай ошибки**: репозиторий генерирует исключение, состояние ошибки отправляется
3. **Граничный случай**: возвращается пустой список продуктов, отображается пустое состояние

Создайте тест в наборе исходников `androidUnitTest`:

```kotlin
@RunWith(MockitoJUnitRunner::class)
class ProductListViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher)
    private val repository: ProductRepository = mock()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadSuccess_displaysProducts`() = runTest {
        val products = listOf(Product("1", "Product 1"), Product("2", "Product 2"))

        whenever(repository.getProducts()).thenReturn(products)

        val viewModel = ProductListViewModel(repository, testScope)
        advanceUntilIdle()

        val contentState = viewModel.uiState.value as ProductState.Success
        assertEquals(2, contentState.products.size)
        verify(repository).getProducts()
    }

    @Test
    fun `loadError_displaysErrorState`() = runTest {
        val exception = RuntimeException("Network error")

        whenever(repository.getProducts()).thenThrow(exception)

        val viewModel = ProductListViewModel(repository, testScope)
        advanceUntilIdle()

        assertEquals(ProductState.Error("Network error"), viewModel.uiState.value)
    }

    @Test
    fun `loadSuccessWithEmptyProducts_displaysEmptyState`() = runTest {

        whenever(repository.getProducts()).thenReturn(emptyList())

        val viewModel = ProductListViewModel(repository, testScope)
        advanceUntilIdle()

        assertEquals(emptyList<Product>(), (viewModel.uiState.value as ProductState.Success).products)
    }
}
```
