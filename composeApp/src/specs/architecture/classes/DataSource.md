# Источники данных (DataSource)

## Обзор

В архитектуре приложения используются три основных класса источников данных:

1. **API** — для работы с сетевыми запросами (Ktor)
2. **DataStore** — для работы с локальной базой данных (Room)
3. **Preferences** — для работы с настройками (PreferenceManager)

Все три класса следуют единым принципам:
- Интерфейсы возвращают доменные модели (Entity)
- Интерфейсы принимают примитивные данные или собственные data-классы в качестве аргументов
- Реализации являются `internal` и скрыты от внешних модулей
- Для тестов создаются mock-реализации с фейковыми данными

---

## API

### Определение

**API** — это интерфейс для работы с сетевыми запросами через Ktor HTTP клиент. API интерфейсы используются для маппинга данных из внешних сервисов.

### Назначение

API решает следующие задачи:
- Выполняет HTTP-запросы к внешним сервисам
- Маппит DTO (Data Transfer Objects) в доменные модели
- Изолирует сетевую логику от бизнес-логики
- Обеспечивает типобезопасность при работе с сетью

### Размещение в FSD

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
├── internal/
│   └── api/
│       ├── MyFeatureApi.kt          # internal interface
│       ├── MyFeatureApiImpl.kt      # internal implementation
│       └── MyFeatureDto.kt          # DTO для маппинга
```

### Структура API

#### Интерфейс API

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/api/MyFeatureApi.kt
package com.xfivetech.myFeature.internal.api

import com.xfivetech.myFeature.Product
import com.xfivetech.myFeature.ProductSearchParameters

internal interface MyFeatureApi {
    suspend fun getProduct(id: String): Product
    suspend fun searchProducts(parameters: ProductSearchParameters): List<Product>
    suspend fun createProduct(product: ProductCreateRequest): Product
}
```

#### Реализация API

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/api/MyFeatureApiImpl.kt
package com.xfivetech.myFeature.internal.api

import com.xfivetech.myFeature.Product
import com.xfivetech.myFeature.ProductSearchParameters
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

internal class MyFeatureApiImpl(
    private val networkClient: HttpClient,
    baseUrlProvider: () -> String,
) : MyFeatureApi {
    private val baseUrl = baseUrlProvider()

    override suspend fun getProduct(id: String): Product {
        val response: ProductDto = networkClient.get("$baseUrl/products/$id").body()
        return response.toEntity()
    }

    override suspend fun searchProducts(
        parameters: ProductSearchParameters,
    ): List<Product> {
        val response: List<ProductDto> = networkClient.get("$baseUrl/products") {
            url {
                parameters.append("query", parameters.query)
                parameters.append("category", parameters.category)
            }
        }.body()
        return response.map { it.toEntity() }
    }

    override suspend fun createProduct(
        product: ProductCreateRequest,
    ): Product {
        val response: ProductDto = networkClient.post("$baseUrl/products") {
            setBody(product.toDto())
        }.body()
        return response.toEntity()
    }
}
```

#### DTO для маппинга

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/api/MyFeatureDto.kt
package com.xfivetech.myFeature.internal.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProductDto(
    @SerialName("product_id")
    val id: String,
    @SerialName("product_name")
    val name: String,
    @SerialName("price_rub")
    val price: Double,
    @SerialName("image_url")
    val imageUrl: String?,
)

internal fun ProductDto.toEntity(): Product = Product(
    id = id,
    name = name,
    price = Money.fromRubles(price),
    imageUrl = imageUrl,
)

internal fun Product.toDto(): ProductDto = ProductDto(
    id = id,
    name = name,
    price = price.toRubles(),
    imageUrl = imageUrl,
)
```

### Mock-реализация для тестов

```kotlin
// tc-shared/myFeature/model/src/commonTest/kotlin/com/xfivetech/myFeature/internal/api/MyFeatureApiMock.kt
package com.xfivetech.myFeature.internal.api

import com.xfivetech.myFeature.Product
import com.xfivetech.myFeature.ProductSearchParameters

internal class MyFeatureApiMock : MyFeatureApi {
    override suspend fun getProduct(id: String): Product = Product.mock()

    override suspend fun searchProducts(
        parameters: ProductSearchParameters,
    ): List<Product> = Product.mocks()

    override suspend fun createProduct(
        product: ProductCreateRequest,
    ): Product = Product.mock()
}
```

### Правила создания API

1. **Интерфейс должен быть `internal`** — API не должен быть доступен извне модуля
2. **Возвращать доменные модели (Entity)** — не DTO
3. **Принимать примитивы или data-классы** — не DTO
4. **Использовать suspend-функции** — все сетевые операции асинхронны
5. **Создавать DTO для маппинга** — отделять сетевое представление от доменного
6. **Добавлять mock-реализацию** — для тестов и Preview

---

## DataStore

### Определение

**DataStore** — это интерфейс для работы с локальной базой данных (Room). DataStore интерфейсы используются для хранения и получения данных из локального хранилища.

### Назначение

DataStore решает следующие задачи:
- Хранит данные локально в базе данных
- Предоставляет реактивный доступ к данным через Flow
- Кэширует данные для офлайн-режима
- Изолирует работу с базой данных от бизнес-логики

### Размещение в FSD

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
├── internal/
│   └── db/
│       ├── MyFeatureDataStore.kt      # internal interface
│       ├── MyFeatureDataStoreImpl.kt  # internal implementation
│       └── MyFeatureDao.kt            # Room DAO
```

### Структура DataStore

#### Интерфейс DataStore

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/db/MyFeatureDataStore.kt
package com.xfivetech.myFeature.internal.db

import com.xfivetech.myFeature.Product
import kotlinx.coroutines.flow.Flow

internal interface MyFeatureDataStore {
    val products: Flow<List<Product>>
    suspend fun getProduct(id: String): Product?
    suspend fun saveProduct(product: Product)
    suspend fun deleteProduct(id: String)
    suspend fun clear()
}
```

#### Реализация DataStore

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/db/MyFeatureDataStoreImpl.kt
package com.xfivetech.myFeature.internal.db

import com.xfivetech.myFeature.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class MyFeatureDataStoreImpl(
    private val dao: MyFeatureDao,
) : MyFeatureDataStore {
    override val products: Flow<List<Product>>
        get() = dao.getProductsFlow().map { entities ->
            entities.map { it.toEntity() }
        }

    override suspend fun getProduct(id: String): Product? {
        return dao.getProductById(id)?.toEntity()
    }

    override suspend fun saveProduct(product: Product) {
        dao.insert(ProductDatabaseEntity(product))
    }

    override suspend fun deleteProduct(id: String) {
        dao.deleteById(id)
    }

    override suspend fun clear() {
        dao.deleteAll()
    }
}
```

#### Room DAO

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/db/MyFeatureDao.kt
package com.xfivetech.myFeature.internal.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface MyFeatureDao {
    @Query("SELECT * FROM products")
    fun getProductsFlow(): Flow<List<ProductDatabaseEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductDatabaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductDatabaseEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
```

#### Database Entity

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/db/ProductDatabaseEntity.kt
package com.xfivetech.myFeature.internal.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.xfivetech.myFeature.Product

@Entity(tableName = "products")
internal data class ProductDatabaseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String?,
)

internal fun ProductDatabaseEntity.toEntity(): Product = Product(
    id = id,
    name = name,
    price = Money.fromRubles(price),
    imageUrl = imageUrl,
)

internal fun Product.toDatabaseEntity(): ProductDatabaseEntity = ProductDatabaseEntity(
    id = id,
    name = name,
    price = price.toRubles(),
    imageUrl = imageUrl,
)
```

### Mock-реализация для тестов

```kotlin
// tc-shared/myFeature/model/src/commonTest/kotlin/com/xfivetech/myFeature/internal/db/MyFeatureDataStoreMock.kt
package com.xfivetech.myFeature.internal.db

import com.xfivetech.myFeature.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MyFeatureDataStoreMock : MyFeatureDataStore {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    override val products: Flow<List<Product>> = _products.asStateFlow()

    override suspend fun getProduct(id: String): Product? {
        return _products.value.find { it.id == id }
    }

    override suspend fun saveProduct(product: Product) {
        _products.update { current ->
            current.toMutableList().apply {
                val index = indexOfFirst { it.id == product.id }
                if (index >= 0) {
                    this[index] = product
                } else {
                    add(product)
                }
            }
        }
    }

    override suspend fun deleteProduct(id: String) {
        _products.update { it.filter { product -> product.id != id } }
    }

    override suspend fun clear() {
        _products.update { emptyList() }
    }
}
```

### Правила создания DataStore

1. **Интерфейс должен быть `internal`** — DataStore не должен быть доступен извне модуля
2. **Возвращать доменные модели (Entity)** — не DatabaseEntity
3. **Использовать Flow для реактивных данных** — подписчики получают обновления
4. **Создавать DatabaseEntity для Room** — отделять базовое представление от доменного
5. **Добавлять mock-реализацию** — для тестов и Preview

---

## Preferences

### Определение

**Preferences** — это интерфейс для работы с настройками через PreferenceManager. Preferences интерфейсы используются для хранения простых значений (флаги, токены, настройки пользователя).

### Назначение

Preferences решает следующие задачи:
- Хранит простые значения (Boolean, String, Int, etc.)
- Предоставляет реактивный доступ к настройкам через Flow
- Сохраняет настройки между запусками приложения
- Изолирует работу с настройками от бизнес-логики

### Размещение в FSD

```
tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/
├── internal/
│   └── preferences/
│       ├── MyFeaturePreferences.kt      # internal interface
│       └── MyFeaturePreferencesImpl.kt  # internal implementation
```

### Структура Preferences

#### Интерфейс Preferences

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/preferences/MyFeaturePreferences.kt
package com.xfivetech.myFeature.internal.preferences

import kotlinx.coroutines.flow.Flow

internal interface MyFeaturePreferences {
    suspend fun setUserId(userId: String)
    suspend fun getUserId(): String?
    fun trackUserId(): Flow<String?>

    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun isNotificationsEnabled(): Boolean
    fun trackNotificationsEnabled(): Flow<Boolean?>

    suspend fun clear()
}
```

#### Реализация Preferences

```kotlin
// tc-shared/myFeature/model/src/kotlin/com/xfivetech/myFeature/internal/preferences/MyFeaturePreferencesImpl.kt
package com.xfivetech.myFeature.internal.preferences

import com.fivetech.preferences.PreferencesManager
import kotlinx.coroutines.flow.Flow

internal class MyFeaturePreferencesImpl(
    private val prefs: PreferencesManager,
) : MyFeaturePreferences {

    override suspend fun setUserId(userId: String) {
        prefs.setString(KEY_USER_ID, userId)
    }

    override suspend fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID)
    }

    override fun trackUserId(): Flow<String?> {
        return prefs.trackString(KEY_USER_ID)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.setBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
    }

    override suspend fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    override fun trackNotificationsEnabled(): Flow<Boolean?> {
        return prefs.trackBoolean(KEY_NOTIFICATIONS_ENABLED)
    }

    override suspend fun clear() {
        prefs.remove(KEY_USER_ID)
        prefs.remove(KEY_NOTIFICATIONS_ENABLED)
    }

    private companion object {
        const val KEY_USER_ID = "key_user_id"
        const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"
    }
}
```

### Mock-реализация для тестов

> **Примечание:** Для тестов не обязательно создавать отдельный Mock-класс для Preferences. Достаточно использовать `PreferencesImpl`, передавая `PreferencesManagerMock()` в качестве реализации `PreferencesManager`.

```kotlin
// В тестах можно использовать PreferencesImpl с PreferencesManagerMock
import com.fivetech.preferences.PreferencesManagerMock

val preferences = MyFeaturePreferencesImpl(
    prefs = PreferencesManagerMock(),
)
```

### Правила создания Preferences

1. **Интерфейс должен быть `internal`** — Preferences не должен быть доступен извне модуля
2. **Использовать пары методов** — `setXxx()` / `getXxx()` и `trackXxx()`
3. **Использовать Flow для отслеживания изменений** — подписчики получают обновления
4. **Хранить ключи в companion object** — для избежания опечаток
5. **Добавлять метод clear()** — для очистки настроек
6. **Для тестов использовать PreferencesImpl с PreferencesManagerMock** — не нужно создавать отдельный Mock-класс для Preferences, достаточно передать `PreferencesManagerMock()` в `PreferencesImpl`
7. **Регистрировать через `single` в Koin DI** — Preferences оборачивают PreferenceManager, который должен быть singleton

```kotlin
// ✅ Правильно
module {
    single<ProfilePreferences> { ProfilePreferencesImpl(get()) }
}

// ❌ Неправильно
module {
    factory<ProfilePreferences> { ProfilePreferencesImpl(get()) }
}
```

---

## Общие правила для всех источников данных

### 1. Возвращаемые типы

Все интерфейсы должны возвращать **доменные модели (Entity)**, а не DTO или DatabaseEntity:

```kotlin
// ✅ Правильно
internal interface MyFeatureApi {
    suspend fun getProduct(id: String): Product  // Entity
}

// ❌ Неправильно
internal interface MyFeatureApi {
    suspend fun getProduct(id: String): ProductDto  // DTO
}
```

### 2. Аргументы методов

Интерфейсы должны принимать **примитивные данные** или **собственные data-классы**:

```kotlin
// ✅ Правильно
internal interface MyFeatureApi {
    suspend fun searchProducts(
        query: String,                    // примитив
        category: String,                 // примитив
        parameters: ProductSearchParameters  // data-класс
    ): List<Product>
}

// ❌ Неправильно
internal interface MyFeatureApi {
    suspend fun searchProducts(
        request: SearchRequestDto  // DTO
    ): List<Product>
}
```

### 3. Внутренние классы

Все реализации должны быть `internal`:

```kotlin
// ✅ Правильно
internal class MyFeatureApiImpl(...) : MyFeatureApi

// ❌ Неправильно
public class MyFeatureApiImpl(...) : MyFeatureApi
```

### 4. Mock-реализации

Для каждого интерфейса должна быть mock-реализация в тестовой папке:

```kotlin
// tc-shared/model/src/commonTest/kotlin/com/xfivetech/myFeature/internal/api/MyFeatureApiMock.kt
internal class MyFeatureApiMock : MyFeatureApi {
    override suspend fun getProduct(id: String): Product = Product.mock()
}
```

Mock-реализации должны использовать функции из Entity для создания фейковых данных:

```kotlin
// ✅ Правильно
override suspend fun getProduct(id: String): Product = Product.mock()

// ❌ Неправильно
override suspend fun getProduct(id: String): Product = Product(
    id = "1",
    name = "Test",
    price = Money.fromRubles(100.0),
    imageUrl = null,
)
```

### 5. Размещение файлов

```
tc-shared/model/src/kotlin/com/xfivetech/myFeature/
├── internal/
│   ├── api/
│   │   ├── MyFeatureApi.kt          # интерфейс
│   │   ├── MyFeatureApiImpl.kt      # реализация
│   │   └── MyFeatureDto.kt          # DTO
│   ├── db/
│   │   ├── MyFeatureDataStore.kt    # интерфейс
│   │   ├── MyFeatureDataStoreImpl.kt  # реализация
│   │   └── MyFeatureDao.kt          # Room DAO
│   └── preferences/
│       ├── MyFeaturePreferences.kt  # интерфейс
│       └── MyFeaturePreferencesImpl.kt  # реализация
```

### 6. Использование в Repository

Источники данных используются в Repository для объединения данных из разных источников:

```kotlin
internal class MyFeatureRepositoryImpl(
    private val api: MyFeatureApi,
    private val dataStore: MyFeatureDataStore,
    private val preferences: MyFeaturePreferences,
) : MyFeatureRepository {

    override suspend fun getProduct(id: String): Product {
        // Сначала пробуем получить из кэша
        val cached = dataStore.getProduct(id)
        if (cached != null) {
            return cached
        }

        // Если нет в кэше, загружаем из API
        val product = api.getProduct(id)

        // Сохраняем в кэш
        dataStore.saveProduct(product)

        return product
    }
}
```

---

## Примеры из кодовой базы

### Хорошие примеры

#### 1. ProfileApi (API)

```kotlin
// tc-shared/profile/model/src/commonMain/kotlin/com/xfivetech/superapp/profile/internal/api/ProfileApi.kt
internal interface ProfileApi {
    suspend fun getUserInfo(): ProfileModel
    suspend fun deleteProfile(tc: TC)
}
```

**Почему хорошо:**
- Интерфейс `internal`
- Возвращает Entity
- Принимает примитивы и enum

#### 2. AppModeDataStore (DataStore)

```kotlin
// tc-shared/core/appSelector/impl/src/commonMain/kotlin/com/xfivetech/appSelector/appmode/AppModeDataStore.kt
internal interface AppModeDataStore {
    val modes: Flow<List<AppModeEntity>>
    suspend fun applyAndSelect(tc: TC, mode: AppMode)
    suspend fun clear()
}
```

**Почему хорошо:**
- Интерфейс `internal`
- Возвращает Entity
- Использует Flow для реактивных данных
- Имеет mock-реализацию в companion object

#### 3. AuthServicePreferences (Preferences)

```kotlin
// tc-shared/auth-tcx/src/commonMain/kotlin/com/xfivetech/multiplatform/authTcx/preferences/AuthServicePreferences.kt
public interface AuthServicePreferences {
    public suspend fun setSuccessRegisterDeviceId(isSuccessRegister: Boolean)
    public suspend fun isSuccessRegisterDeviceId(): Boolean
    public fun trackRegisterDeviceId(): Flow<Boolean?>
}
```

**Почему хорошо:**
- Использует пары методов: `setXxx()` / `isXxx()` и `trackXxx()`
- Возвращает примитивные типы
- Использует Flow для отслеживания изменений

---

## Сравнение источников данных

| Характеристика | API | DataStore | Preferences |
|----------------|-----|-----------|-------------|
| **Назначение** | Сетевые запросы | Локальная база данных | Настройки |
| **Технология** | Ktor | Room | PreferenceManager |
| **Возвращаемые типы** | Entity (через DTO) | Entity | Примитивы |
| **Аргументы** | Примитивы, data-классы | Примитивы, Entity | Примитивы |
| **Асинхронность** | suspend-функции | suspend-функции + Flow | suspend-функции + Flow |
| **Кэширование** | Нет | Да | Да |
| **Офлайн-режим** | Нет | Да | Да |

---

## Заключение

Три класса источников данных (API, DataStore, Preferences) обеспечивают унифицированный подход к работе с данными в приложении:

1. **API** — для получения данных из внешних сервисов
2. **DataStore** — для локального хранения и кэширования
3. **Preferences** — для хранения настроек и флагов

Все они следуют единым принципам:
- Интерфейсы возвращают доменные модели
- Реализации скрыты (`internal`)
- Mock-реализации для тестов обязательны
- Используют Flow для реактивных данных

Это позволяет создавать тестируемый, поддерживаемый и масштабируемый код.
