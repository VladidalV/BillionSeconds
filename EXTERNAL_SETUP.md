# External Setup Checklist

Всё, что нужно настроить во внешних сервисах/консолях перед релизом.

---

## Google Sign-In (Android)

**Статус:** код готов, нужен реальный Client ID

### Шаги:
1. Зайти в [Google Cloud Console](https://console.cloud.google.com/)
2. Создать проект (или выбрать существующий)
3. Включить **Google Sign-In API**: APIs & Services → Enable APIs → "Google Identity"
4. Создать OAuth consent screen (External, заполнить app name / email)
5. Создать **Web application** OAuth 2.0 Client ID:
   - APIs & Services → Credentials → Create Credentials → OAuth Client ID
   - Тип: **Web application**
   - Authorized redirect URIs: не нужны для мобильных
6. Скопировать Client ID вида `XXXXXXXX.apps.googleusercontent.com`
7. Вставить в файл:
   ```
   composeApp/src/androidMain/kotlin/com/example/billionseconds/GoogleSignInConfig.kt
   ```
   Заменить `YOUR_WEB_CLIENT_ID.apps.googleusercontent.com` на реальный ID

> ⚠️ Нужен именно **Web** client ID, не Android client ID.

---

## Google Sign-In (iOS) — Шаг 3

**Статус:** не реализовано (stub возвращает Cancelled)

### Шаги:
1. В том же Google Cloud Console создать **iOS** OAuth 2.0 Client ID:
   - Тип: **iOS**
   - Bundle ID: `com.example.billionseconds` (уточнить в Xcode)
2. Скачать `GoogleService-Info.plist`
3. Добавить в Xcode проект (iosApp target)
4. Добавить URL scheme в `Info.plist`:
   - Key: `URL types` → Item 0 → URL Schemes → Item 0
   - Value: reversed client ID из `GoogleService-Info.plist` (поле `REVERSED_CLIENT_ID`)
5. Реализовать `actual fun onLaunchGoogleSignIn` в `AuthSignIn.ios.kt` через `GIDSignIn`

---

## Apple Sign-In (iOS) — Шаг 3

**Статус:** ✅ Kotlin код реализован (`AuthSignIn.ios.kt`), нужно включить capability в Xcode/Apple Developer

### Шаги:
1. Открыть [Apple Developer Console](https://developer.apple.com/account/)
2. Certificates, Identifiers & Profiles → Identifiers → выбрать App ID приложения
3. Включить capability **Sign In with Apple** → Save
4. В Xcode: выбрать target `iosApp` → Signing & Capabilities → "+ Capability" → **Sign In with Apple**
5. Пересобрать iOS таргет — `ASAuthorizationController` заработает автоматически

---

## iOS Keychain (TokenStorage) — Шаг 4

**Статус:** ✅ Kotlin код реализован (`IosTokenStorage.kt`), нужно включить capability в Xcode

### Шаги:
1. В Xcode: Signing & Capabilities → добавить **Keychain Sharing**
2. Keychain Group: `$(AppIdentifierPrefix)com.example.billionseconds`
3. ~~Реализовать `IosTokenStorage` через `Security` framework~~ — **ГОТОВО**
   - Файл: `core/core-network/src/iosMain/.../token/IosTokenStorage.kt`
   - Токены хранятся в Keychain с `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly`
   - Автоматическая миграция из `NSUserDefaults` при первом чтении

---

## Push Notifications (будущее)

**Статус:** не реализовано

### Шаги:
1. Apple Developer Console → Certificates → создать APNs key (.p8)
2. Firebase Console → Project Settings → Cloud Messaging → загрузить APNs key
3. Google Cloud Console → Firebase Cloud Messaging API включить
4. В Xcode: добавить capability **Push Notifications** + **Background Modes → Remote notifications**
