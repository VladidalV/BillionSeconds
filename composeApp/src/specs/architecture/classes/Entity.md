# Entity (Модель)

## Определение

**Entity** — это data-класс, представляющий бизнес-сущность предметной области. Entity содержит публичное описание данных, которые используются в приложении.

Entity — это фундаментальный элемент архитектуры, который описывает структуру данных без привязки к конкретным источникам (API, база данных, UI).

## Назначение Entity

Entity решает следующие задачи:

1. **Описывает бизнес-сущности** — продукты, заказы, пользователи, корзины и т.д.
2. **Является контрактом** между слоями архитектуры
3. **Изолирует доменную модель** от внешних представлений (DTO, UI-модели)
4. **Обеспечивает типобезопасность** при передаче данных между модулями

## Размещение в FSD

Согласно Feature-Sliced Design, Entity может находиться в двух местах:

### 1. В слое `model` (локальная сущность)

Если сущность используется только внутри одного фич-модуля:

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
└── Product.kt              # public data class
```

**Характеристики:**
- Объявляется как `public`
- Может использоваться другими модулями
- Является частью публичного API модуля

### 2. В слое `entity` (общая сущность)

Если сущность используется в нескольких фичах:

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
└── Product.kt              # public data class
```

**Характеристики:**
- Объявляется как `public`
- Может использоваться другими модулями
- Является частью публичного API модуля

## Когда использовать entity вместо model

Используйте слой `entity`, когда:

1. **Сущность переиспользуется** в нескольких фичах
2. **Сущность является частью публичного API** модуля
3. **Сущность используется в интерфейсах** (Repository, UseCase)

Используйте слой `model`, когда:

1. **Сущность используется только** внутри одного модуля
2. **Сущность является деталью реализации** (DTO, мапперы)

## Структура Entity

### Базовая структура

```kotlin
// entity слой (публичная сущность)
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Double,
    public val imageUrl: String?,
)
```

### Entity с бизнес-логикой

```kotlin
public data class ShoppingCart(
    public val items: List<CartItem>,
    public val deliveryAddress: Address?,
) {
    public val isEmpty: Boolean
        get() = items.isEmpty()

    public val totalAmount: Money
        get() = items.sumOf { it.price * it.quantity }

    public fun canCheckout(): Boolean {
        return items.isNotEmpty() && deliveryAddress != null
    }
}
```

## Mock данные (обязательное требование)

Каждая Entity **должна** иметь публичные mock-данные. Mock-данные — это фейковые данные, которые необходимы для:

1. **Тестов** — unit-тесты, интеграционные тесты
2. **Preview представлений** — Compose Preview в feature слое
3. **Демонстрации UI** — показ компонентов дизайнерам и стейкхолдерам

### Правила создания mock-данных

1. **Объявляйте как `public`** — mock-данные должны быть доступны в feature слое
2. **Размещайте в том же файле** что и Entity или в отдельном файле `Mocks.kt`
3. **Используйте понятные имена** — `Product.mock()`, `Product.mocks()`, `ProductMocks.default()`
4. **Используйте функции вместо свойств** — `fun mock()` вместо `val mock` для гибкости
5. **Создавайте несколько вариантов** — для разных сценариев (default, empty, error, etc.)

### Примеры mock-данных

#### 1. Mock в companion object

```kotlin
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,
    public val imageUrl: String?,
) {
    public companion object {
        public fun mock(): Product = Product(
            id = "mock-product-1",
            name = "Молоко Домик в деревне",
            price = Money.fromRubles(89.90),
            imageUrl = "https://example.com/milk.jpg",
        )

        public fun mockEmpty(): Product = Product(
            id = "",
            name = "",
            price = Money.fromRubles(0.0),
            imageUrl = null,
        )

        public fun mocks(): List<Product> = listOf(
            mock(),
            Product(
                id = "mock-product-2",
                name = "Хлеб Бородинский",
                price = Money.fromRubles(45.50),
                imageUrl = "https://example.com/bread.jpg",
            ),
            Product(
                id = "mock-product-3",
                name = "Яйца 10 шт",
                price = Money.fromRubles(99.00),
                imageUrl = "https://example.com/eggs.jpg",
            ),
        )
    }
}
```

#### 2. Mock в отдельном файле

```kotlin
// Product.kt
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,
    public val imageUrl: String?,
)

// ProductMocks.kt
public object ProductMocks {
    public fun default(): Product = Product(
        id = "mock-product-1",
        name = "Молоко Домик в деревне",
        price = Money.fromRubles(89.90),
        imageUrl = "https://example.com/milk.jpg",
    )

    public fun empty(): Product = Product(
        id = "",
        name = "",
        price = Money.fromRubles(0.0),
        imageUrl = null,
    )

    public fun list(): List<Product> = listOf(
        default(),
        Product(
            id = "mock-product-2",
            name = "Хлеб Бородинский",
            price = Money.fromRubles(45.50),
            imageUrl = "https://example.com/bread.jpg",
        ),
    )
}
```

#### 3. Mock для сложной Entity

```kotlin
public data class ShoppingCart(
    public val items: List<CartItem>,
    public val deliveryAddress: Address?,
) {
    public val isEmpty: Boolean
        get() = items.isEmpty()

    public val totalAmount: Money
        get() = items.sumOf { it.price * it.quantity }

    public companion object {
        public fun mock(): ShoppingCart = ShoppingCart(
            items = listOf(
                CartItem(
                    productId = "mock-product-1",
                    name = "Молоко",
                    price = Money.fromRubles(89.90),
                    quantity = 2,
                ),
                CartItem(
                    productId = "mock-product-2",
                    name = "Хлеб",
                    price = Money.fromRubles(45.50),
                    quantity = 1,
                ),
            ),
            deliveryAddress = Address.mock(),
        )

        public fun mockEmpty(): ShoppingCart = ShoppingCart(
            items = emptyList(),
            deliveryAddress = null,
        )
    }
}
```

### Использование mock-данных в Preview

```kotlin
// В feature слое
@Composable
fun ProductCardPreview() {
    ProductCard(
        product = Product.mock(),  // Используем mock из Entity
        onAddToCart = {},
    )
}

@Composable
fun ProductListPreview() {
    ProductList(
        products = Product.mocks(),  // Используем список mock-данных
        onProductClick = {},
    )
}
```

### Использование mock-данных в тестах

```kotlin
class ProductViewModelTest {
    @Test
    fun `should load product successfully`() {
        val mockProduct = Product.mock()
        val viewModel = ProductViewModel(
            repository = FakeProductRepository(mockProduct)
        )

        viewModel.loadProduct("mock-product-1")

        assertEquals(mockProduct, viewModel.product.value)
    }
}
```

## Отличия от других типов моделей

### Entity vs DTO

**DTO (Data Transfer Object)** — используется для передачи данных между слоями (API, база данных):

```kotlin
// DTO для API
@Serializable
internal data class ProductDto(
    @SerialName("product_id")
    val id: String,
    @SerialName("product_name")
    val name: String,
    @SerialName("price_rub")
    val price: Double,
)

// Entity (бизнес-сущность)
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,  // Типобезопасная обёртка
)
```

### Entity vs UiModel

**UiModel** — используется для отображения в UI:

```kotlin
// Entity (бизнес-сущность)
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,
    public val imageUrl: String?,
)

// UiModel (для отображения)
public data class ProductUiModel(
    public val id: String,
    public val name: String,
    public val priceText: String,  // Отформатированная цена
    public val imageUrl: String?,
    public val isFavorite: Boolean,  // UI-состояние
)
```

## Примеры из кодовой базы

### Хорошие примеры

#### 1. Product (Общая сущность в entity)

**Почему хорошо:**
- Используется в нескольких фичах (каталог, корзина, избранное)
- Является частью публичного API модуля
- Содержит только бизнес-данные

```kotlin
// entity слой
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,
    public val imageUrl: String?,
)
```

#### 2. OrderItem (Локальная сущность в model)

**Почему хорошо:**
- Используется только внутри модуля заказов
- Не нужна другим модулям
- Объявлена как `internal`

```kotlin
// model слой
internal data class OrderItem(
    val productId: String,
    val quantity: Int,
    val price: Money,
)
```

#### 3. ShoppingCart (Entity с бизнес-логикой)

**Почему хорошо:**
- Содержит вычисляемые свойства
- Имеет методы для бизнес-операций
- Инкапсулирует логику корзины

```kotlin
public data class ShoppingCart(
    public val items: List<CartItem>,
    public val deliveryAddress: Address?,
) {
    public val isEmpty: Boolean
        get() = items.isEmpty()

    public val totalAmount: Money
        get() = items.sumOf { it.price * it.quantity }

    public fun canCheckout(): Boolean {
        return items.isNotEmpty() && deliveryAddress != null
    }
}
```

### Плохие примеры

#### 1. ProductResponse (DTO вместо Entity)

**Почему плохо:**
- Название указывает на источник данных (Response)
- Содержит поля, специфичные для API
- Не абстрагирован от внешнего представления

```kotlin
// Плохо: DTO вместо Entity
@Serializable
public data class ProductResponse(
    @SerialName("product_id")
    public val id: String,
    @SerialName("product_name")
    public val name: String,
    @SerialName("price_rub")
    public val price: Double,
    @SerialName("is_available")
    public val available: Boolean,
)
```

**Как лучше:** Создать отдельный Entity и маппер из DTO:

```kotlin
// Entity
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,
    public val isAvailable: Boolean,
)

// DTO
@Serializable
public data class ProductDto(
    @SerialName("product_id")
    public val id: String,
    @SerialName("product_name")
    public val name: String,
    @SerialName("price_rub")
    public val price: Double,
    @SerialName("is_available")
    public val available: Boolean,
)

// Маппер
public fun ProductDto.toEntity(): Product = Product(
    id = id,
    name = name,
    price = Money.fromRubles(price),
    isAvailable = available,
)
```

#### 2. ProductUiModel в entity слое

**Почему плохо:**
- Содержит UI-специфичные поля
- Смешивает бизнес-логику и представление
- Не должна быть в entity слое

```kotlin
// Плохо: UiModel в entity слое
public data class ProductUiModel(
    public val id: String,
    public val name: String,
    public val priceText: String,  // UI-форматирование
    public val imageUrl: String?,
    public val isFavorite: Boolean,  // UI-состояние
    public val isLoading: Boolean,   // UI-состояние
)
```

**Как лучше:** Разделить на Entity и UiModel:

```kotlin
// Entity (в entity слое)
public data class Product(
    public val id: String,
    public val name: String,
    public val price: Money,
    public val imageUrl: String?,
)

// UiModel (в feature слое)
public data class ProductUiModel(
    public val id: String,
    public val name: String,
    public val priceText: String,
    public val imageUrl: String?,
    public val isFavorite: Boolean,
    public val isLoading: Boolean,
)
```

## Правила именования

- Используйте существительное в единственном числе: `Product`, `Order`, `User`
- Избегайте суффиксов, указывающих на источник: `ProductResponse`, `ProductEntity`, `ProductModel`
- Используйте понятные имена полей: `id`, `name`, `price` вместо `productId`, `productName`, `productPrice`
- Для списков используйте множественное число: `items`, `products`, `orders`

## Структура файлов

### Для локальной сущности (model)

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
└── Product.kt              # public data class
```

### Для общей сущности (entity)

```
tc-shared/model/src/kotlin/com/xfivetech/myFeature/
└── Product.kt              # public data class
```

### Для сущности с маппером

```
tc-shared/model/src/kotlin/com/xfivetech/myFeature/
├── Product.kt              # public data class
└── internal/
    └── ProductMapper.kt    # internal маппер из DTO
```

## Маппинг между слоями

### DTO → Entity

```kotlin
internal fun ProductDto.toEntity(): Product = Product(
    id = id,
    name = name,
    price = Money.fromRubles(price),
    imageUrl = imageUrl,
)
```

### Entity → UiModel

```kotlin
public fun Product.toUiModel(isFavorite: Boolean): ProductUiModel = ProductUiModel(
    id = id,
    name = name,
    priceText = price.format(),
    imageUrl = imageUrl,
    isFavorite = isFavorite,
    isLoading = false,
)
```

## Чек-лист для создания Entity

Перед созданием Entity ответьте на вопросы:

1. **Используется ли сущность в нескольких фичах?**
   - Да → Разместите в слое `entity` (public)
   - Нет → Перейдите к вопросу 2

2. **Является ли сущность частью публичного API модуля?**
   - Да → Разместите в слое `entity` (public)
   - Нет → Разместите в слое `model` (internal)

3. **Содержит ли сущность только бизнес-данные?**
   - Да → Это Entity
   - Нет → Возможно, это DTO или UiModel

4. **Нужно ли маппить сущность из/в DTO?**
   - Да → Создайте отдельный DTO и маппер
   - Нет → Entity может использоваться напрямую

5. **Созданы ли публичные mock-данные?**
   - Да → Отлично, Entity готова к использованию
   - Нет → **Обязательно** создайте mock-данные (companion object или отдельный файл `Mocks.kt`)

## Резюме для ИИ

При анализе кода и принятии решений о создании Entity:

1. **Приоритет:** Entity — это бизнес-сущность, не привязанная к источнику данных
2. **Размещение:**
   - Мультифичное использование → слой `entity` (public)
   - Локальное использование → слой `model` (internal)
3. **Отличия:**
   - Entity — бизнес-сущность (Product, Order)
   - DTO — передача данных (ProductDto, ProductResponse)
   - UiModel — отображение в UI (ProductUiModel)
4. **Маппинг:**
   - DTO → Entity → UiModel
   - Используйте extension-функции для маппинга
5. **Бизнес-логика:**
   - Entity может содержать вычисляемые свойства
   - Entity может содержать методы для бизнес-операций
   - Не смешивайте UI-логику с бизнес-логикой
6. **Mock-данные (ОБЯЗАТЕЛЬНО):**
   - Каждая Entity должна иметь публичные mock-данные
   - Используйте companion object или отдельный файл `Mocks.kt`
   - Mock-данные нужны для тестов и Compose Preview
   - Создавайте несколько вариантов: default, empty, list
