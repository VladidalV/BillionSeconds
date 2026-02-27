# Repository (Репозиторий)

## Определение

**Repository** — это класс оркестрации данных моделей (Entity) из разных источников данных (DataSource). Repository объединяет работу с API, DataStore и Preferences, предоставляя единый интерфейс для работы с данными.

Repository — это ключевой элемент архитектуры, который скрывает сложность работы с разными источниками данных и предоставляет высокоуровневые сценарии для бизнес-логики.

## Назначение Repository

Repository решает следующие задачи:

1. **Оркестрирует данные** из разных источников (API, DataStore, Preferences)
2. **Предоставляет высокоуровневые сценарии** для работы с данными
3. **Скрывает сложность** работы с разными источниками
4. **Обеспечивает кэширование** и офлайн-режим
5. **Изолирует бизнес-логику** от деталей реализации источников данных

## Размещение в FSD

Согласно Feature-Sliced Design, Repository состоит из двух частей:

### 1. Интерфейс Repository

Интерфейс Repository располагается по тем же принципам, что и Entity:

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
└── ProductRepository.kt      # public interface
```

**Характеристики:**
- Объявляется как `public`
- Может использоваться другими модулями
- Является частью публичного API модуля
- Располагается в том же пакете, что и Entity

### 2. Реализация Repository

Реализация Repository всегда находится в модуле `model` и является `internal`:

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/
└── ProductRepositoryImpl.kt  # internal implementation
```

**Характеристики:**
- Объявляется как `internal`
- Скрыта от внешних модулей
- Использует внутренние DataSource (API, DataStore, Preferences)
- Внедряется через DI (Koin)

## Источники данных для Repository

Repository может использовать различные источники данных для оркестрации:

### 1. DataSource (основные источники)

- **API** — сетевой источник данных для получения/отправки данных на сервер
- **DataStore** — локальное хранилище для кэширования данных
- **Preferences** — хранилище настроек и пользовательских предпочтений

### 2. Базовые сервисы (вспомогательные источники)

Помимо основных DataSource, Repository может принимать другие базовые сервисы для выполнения своих задач, например:

- **TokenProvider** — предоставляет токены авторизации для API запросов
- **AppModeDataStore** — хранит текущий режим приложения (например, delivery/instore)
- **UserSessionProvider** — предоставляет информацию о текущей сессии пользователя
- **DeviceInfoProvider** — предоставляет информацию об устройстве
- **FeatureToggleProvider** — предоставляет состояние фич-тогглов
- **AnalyticsProvider** — отправляет аналитические события

**Пример использования базовых сервисов:**

```kotlin
internal class ProductRepositoryImpl(
    private val api: ProductApi,                    // DataSource
    private val dataStore: ProductDataStore,         // DataSource
    private val tokenProvider: TokenProvider,        // Базовый сервис
    private val appModeDataStore: AppModeDataStore,  // Базовый сервис
) : ProductRepository {

    override fun getProducts(withCache: Boolean): Flow<List<Product>> = flow {
        val token = tokenProvider.getAccessToken()
        val appMode = appModeDataStore.appMode.first()
        
        val products = api.getProducts(token, appMode)
        
        if (withCache) {
            products.forEach { dataStore.saveProduct(it) }
        }
        
        emit(products)
    }
}
```

**Важно:** Базовые сервисы не являются DataSource, но используются Repository для выполнения своих задач (например, для получения токена авторизации или определения режима приложения).

## Структура Repository

### Интерфейс Repository

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/ProductRepository.kt
package com.xfivetech.myFeature

import kotlinx.coroutines.flow.Flow

public interface ProductRepository {
    /**
     * Получает список продуктов.
     *
     * @param withCache Опциональный параметр. Если true, сначала возвращает данные из кэша (DataStore),
     *                 затем обновляет из API. Если false, только из API.
     *                 Используется только при наличии DataStore в репозитории.
     */
    public fun getProducts(withCache: Boolean = true): Flow<List<Product>>

    /**
     * Получает продукт по ID.
     *
     * @param id Идентификатор продукта
     * @param withCache Опциональный параметр. Если true, сначала возвращает данные из кэша,
     *                 затем обновляет из API. Если false, только из API.
     *                 Используется только при наличии DataStore в репозитории.
     */
    public fun getProduct(id: String, withCache: Boolean = true): Flow<Product?>

    /**
     * Сохраняет продукт в локальное хранилище.
     */
    public suspend fun saveProduct(product: Product)

    /**
     * Удаляет продукт из локального хранилища.
     */
    public suspend fun deleteProduct(id: String)

    /**
     * Очищает все продукты из локального хранилища.
     */
    public suspend fun clearProducts()
}
```

### Реализация Repository

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/ProductRepositoryImpl.kt
package com.xfivetech.myFeature.internal

import com.xfivetech.myFeature.Product
import com.xfivetech.myFeature.ProductRepository
import com.xfivetech.myFeature.internal.api.ProductApi
import com.xfivetech.myFeature.internal.db.ProductDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

internal class ProductRepositoryImpl(
    private val api: ProductApi,
    private val dataStore: ProductDataStore,
) : ProductRepository {

    override fun getProducts(withCache: Boolean): Flow<List<Product>> = flow {
        if (withCache) {
            // Сначала возвращаем данные из кэша
            emit(dataStore.products.first())
        }

        // Затем загружаем свежие данные из API
        try {
            val products = api.getProducts()
            // Сохраняем в кэш
            products.forEach { product ->
                dataStore.saveProduct(product)
            }
            emit(products)
        } catch (e: Exception) {
            if (withCache) {
                // Если API недоступен, возвращаем кэшированные данные
                emit(dataStore.products.first())
            } else {
                throw e
            }
        }
    }.catch { e ->
        // Обработка ошибок
        throw ProductRepositoryException("Failed to get products", e)
    }

    override fun getProduct(id: String, withCache: Boolean): Flow<Product?> = flow {
        if (withCache) {
            // Сначала возвращаем данные из кэша
            emit(dataStore.getProduct(id))
        }

        // Затем загружаем свежие данные из API
        try {
            val product = api.getProduct(id)
            // Сохраняем в кэш
            dataStore.saveProduct(product)
            emit(product)
        } catch (e: Exception) {
            if (withCache) {
                // Если API недоступен, возвращаем кэшированные данные
                emit(dataStore.getProduct(id))
            } else {
                throw e
            }
        }
    }.catch { e ->
        throw ProductRepositoryException("Failed to get product $id", e)
    }

    override suspend fun saveProduct(product: Product) {
        dataStore.saveProduct(product)
    }

    override suspend fun deleteProduct(id: String) {
        dataStore.deleteProduct(id)
    }

    override suspend fun clearProducts() {
        dataStore.clear()
    }
}

internal class ProductRepositoryException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
```

## Основные сценарии работы с данными

Repository предоставляет API для основных флоу работы с данными:

### 1. Получение данных с кэшированием

```kotlin
// Сначала из кэша, затем из API
repository.getProducts(withCache = true)
    .collect { products ->
        // Первая эмиссия: кэшированные данные
        // Вторая эмиссия: свежие данные из API
    }
```

### 2. Получение данных только из API

```kotlin
// Только из API, без кэша
repository.getProducts(withCache = false)
    .collect { products ->
        // Только свежие данные из API
    }
```

### 3. Сохранение данных

```kotlin
// Сохранение в локальное хранилище
repository.saveProduct(product)
```

### 4. Удаление данных

```kotlin
// Удаление из локального хранилища
repository.deleteProduct(productId)
```

## Сценарии оркестрации данных

Repository реализует различные сценарии работы с данными:

### Сценарий 1: Cache-First (сначала кэш, затем API)

```kotlin
override fun getProducts(withCache: Boolean): Flow<List<Product>> = flow {
    if (withCache) {
        // 1. Сначала возвращаем кэшированные данные
        emit(dataStore.products.first())
    }

    // 2. Загружаем свежие данные из API
    val products = api.getProducts()

    // 3. Обновляем кэш
    products.forEach { product ->
        dataStore.saveProduct(product)
    }

    // 4. Возвращаем свежие данные
    emit(products)
}
```

**Когда использовать:**
- Когда нужно показать данные быстро (из кэша)
- Когда нужно обновить данные в фоне
- Для списков, каталогов, продуктов

### Сценарий 2: Network-Only (только API)

```kotlin
override fun getProducts(withCache: Boolean): Flow<List<Product>> = flow {
    // 1. Загружаем данные только из API
    val products = api.getProducts()

    // 2. Обновляем кэш (опционально)
    products.forEach { product ->
        dataStore.saveProduct(product)
    }

    // 3. Возвращаем данные
    emit(products)
}
```

**Когда использовать:**
- Когда нужны только свежие данные
- Для операций, требующих актуальности (оплата, заказ)
- Для чувствительных к времени данных

### Сценарий 3: Cache-Only (только кэш)

```kotlin
override fun getProducts(): Flow<List<Product>> {
    // Возвращаем только кэшированные данные
    return dataStore.products
}
```

**Когда использовать:**
- Для офлайн-режима
- Для данных, которые редко меняются
- Для быстрого доступа без сети

### Сценарий 4: Network-First (сначала API, затем кэш при ошибке)

```kotlin
override fun getProducts(): Flow<List<Product>> = flow {
    try {
        // 1. Пытаемся загрузить из API
        val products = api.getProducts()

        // 2. Обновляем кэш
        products.forEach { product ->
            dataStore.saveProduct(product)
        }

        // 3. Возвращаем данные
        emit(products)
    } catch (e: Exception) {
        // 4. При ошибке возвращаем кэшированные данные
        emit(dataStore.products.first())
    }
}
```

**Когда использовать:**
- Когда приоритет свежие данные
- Когда нужно иметь fallback при ошибке сети
- Для критически важных данных

## Правила создания Repository

### 1. Интерфейс Repository

1. **Объявляйте как `public`** — интерфейс должен быть доступен в других модулях
2. **Размещайте в том же пакете, что и Entity** — для удобства навигации
3. **Используйте Flow для реактивных данных** — подписчики получают обновления
4. **Добавляйте параметр `withCache`** — для управления стратегией загрузки
5. **Документируйте методы** — описывайте поведение и параметры
6. **Используйте доменные модели (Entity)** — не DTO или DatabaseEntity

### 2. Реализация Repository

1. **Объявляйте как `internal`** — реализация скрыта от внешних модулей
2. **Используйте внутренние DataSource** — API, DataStore, Preferences
3. **Реализуйте сценарии оркестрации** — Cache-First, Network-Only, etc.
4. **Обрабатывайте ошибки** — используйте try-catch и Flow.catch
5. **Обновляйте кэш** — сохраняйте данные из API в DataStore
6. **Создавайте свои исключения** — для ошибок Repository

## Тестирование Repository

Для тестирования Repository используйте реальную реализацию [`ProductRepositoryImpl`](specs/architecture/classes/Repository.md:455) с mock-классами DataSource:

```kotlin
// tc-shared/model/src/commonTest/kotlin/com/xfivetech/myFeature/internal/ProductRepositoryTest.kt
package com.xfivetech.myFeature.internal

import com.xfivetech.myFeature.ProductRepository
import com.xfivetech.myFeature.internal.api.ProductApiMock
import com.xfivetech.myFeature.internal.data.ProductDataStoreMock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductRepositoryTest {

    @Test
    fun `getProducts with cache returns cached data first`() = runTest {
        // Arrange
        val api = ProductApiMock()
        val dataStore = ProductDataStoreMock()
        val repository: ProductRepository = ProductRepositoryImpl(api, dataStore)
        
        // Act
        val products = repository.getProducts(withCache = true)
        
        // Assert
        products.collect { result ->
            // Проверяем, что данные получены из кэша
            assertEquals(expected, result)
        }
    }
}
```

**Важно:** Не создавайте отдельные mock-классы для Repository (например, `ProductRepositoryMock`). Вместо этого:
- Используйте реальную реализацию [`ProductRepositoryImpl`](specs/architecture/classes/Repository.md:455)
- Внедряйте mock-классы DataSource (`ProductApiMock`, `ProductDataStoreMock`)
- Это позволяет тестировать реальную логику оркестрации данных
- Mock-классы DataSource должны генерировать фейковые данные

## Примеры из кодовой базы

### Хороший пример: ProductRepository

```kotlin
// Интерфейс
public interface ProductRepository {
    public fun getProducts(withCache: Boolean = true): Flow<List<Product>>
    public fun getProduct(id: String, withCache: Boolean = true): Flow<Product?>
    public suspend fun saveProduct(product: Product)
    public suspend fun deleteProduct(id: String)
}

// Реализация
internal class ProductRepositoryImpl(
    private val api: ProductApi,
    private val dataStore: ProductDataStore,
) : ProductRepository {

    override fun getProducts(withCache: Boolean): Flow<List<Product>> = flow {
        if (withCache) {
            emit(dataStore.products.first())
        }

        val products = api.getProducts()
        products.forEach { dataStore.saveProduct(it) }
        emit(products)
    }
}
```

**Почему хорошо:**
- Интерфейс `public`, реализация `internal`
- Использует Flow для реактивных данных
- Имеет параметр `withCache` для управления стратегией
- Обновляет кэш после загрузки из API
- Использует доменные модели (Product)

### Плохой пример: ProductRepository без кэширования

```kotlin
// Плохо: Не использует Flow для реактивных данных
public interface ProductRepository {
    public suspend fun getProducts(): List<Product>
}

// Плохо: Реализация не обновляет кэш
internal class ProductRepositoryImpl(
    private val api: ProductApi,
    private val dataStore: ProductDataStore,
) : ProductRepository {

    override suspend fun getProducts(): List<Product> {
        // Только из API, без кэширования
        return api.getProducts()
    }
}
```

**Почему плохо:**
- Не обновляет кэш после загрузки из API
- Не использует Flow для реактивных данных
- Нет офлайн-режима

**Как лучше:**

```kotlin
// Лучше: Добавить Flow и обновление кэша
public interface ProductRepository {
    public fun getProducts(withCache: Boolean = true): Flow<List<Product>>
}

internal class ProductRepositoryImpl(
    private val api: ProductApi,
    private val dataStore: ProductDataStore,
) : ProductRepository {

    override fun getProducts(withCache: Boolean): Flow<List<Product>> = flow {
        if (withCache) {
            emit(dataStore.products.first())
        }

        val products = api.getProducts()
        products.forEach { dataStore.saveProduct(it) }
        emit(products)
    }
}
```

### Пример: Repository только с API

```kotlin
// Интерфейс для репозитория, который работает только с API
public interface ProductRepository {
    public fun getProducts(): Flow<List<Product>>
    public suspend fun saveProduct(product: Product)
}

// Реализация только с API
internal class ProductRepositoryImpl(
    private val api: ProductApi,
) : ProductRepository {

    override fun getProducts(): Flow<List<Product>> = flow {
        val products = api.getProducts()
        emit(products)
    }

    override suspend fun saveProduct(product: Product) {
        // Отправка данных на сервер
        api.createProduct(product)
    }
}
```

**Почему хорошо:**
- Параметр `withCache` не нужен, так как нет DataStore
- Использует Flow для реактивных данных
- Простой и понятный интерфейс

## Правила именования

- Интерфейс: `ProductRepository`, `OrderRepository`, `UserRepository`
- Реализация: `ProductRepositoryImpl`, `OrderRepositoryImpl`, `UserRepositoryImpl`
- Исключения: `ProductRepositoryException`, `OrderRepositoryException`

## Структура файлов

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
├── ProductRepository.kt              # public interface
└── internal/
    ├── ProductRepositoryImpl.kt      # internal implementation
    ├── api/
    │   ├── ProductApi.kt             # internal interface
    │   ├── ProductApiImpl.kt         # internal implementation
    │   └── ProductDto.kt             # DTO
    └── db/
        ├── ProductDataStore.kt       # internal interface
        ├── ProductDataStoreImpl.kt   # internal implementation
        └── ProductDao.kt             # Room DAO
```

## Отличия от других компонентов

### Repository vs DataSource

**DataSource** — низкоуровневый интерфейс для работы с конкретным источником данных:
- API — для сетевых запросов
- DataStore — для локальной базы данных
- Preferences — для настроек

**Repository** — высокоуровневый интерфейс, который оркестрирует несколько DataSource:
- Объединяет API, DataStore, Preferences
- Реализует сценарии работы с данными
- Предоставляет бизнес-логику работы с данными

### Repository vs UseCase

**Repository** — работает с данными:
- Получает, сохраняет, удаляет данные
- Оркестрирует источники данных
- Не содержит бизнес-логику приложения

**UseCase** — содержит бизнес-логику:
- Использует Repository для работы с данными
- Содержит правила и валидацию
- Возвращает результат бизнес-операции

## Резюме

Repository — это класс оркестрации данных, который:

1. **Объединяет** работу с API, DataStore и Preferences
2. **Предоставляет** высокоуровневые сценарии для работы с данными
3. **Скрывает** сложность работы с разными источниками
4. **Обеспечивает** кэширование и офлайн-режим
5. **Изолирует** бизнес-логику от деталей реализации

Интерфейс Repository располагается по тем же принципам, что и Entity (public), а реализация всегда в модуле model и является internal.
