package com.example.billionseconds.network.token

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointed
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.interpretObjCPointer
import kotlinx.cinterop.interpretObjCPointerOrNull
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.objcPtr
import kotlinx.cinterop.rawValue
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecItemUpdate
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.darwin.NSObject

// CFStringRef и CFBooleanRef — toll-free bridged с NSString/NSNumber, но в системе типов
// Kotlin/Native это разные иерархии: ObjCObject vs CPointer. Helpers ниже соединяют их
// через interpretObjCPointer/interpretCPointer (безопасно, т.к. layout идентичен).

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosTokenStorage : TokenStorage {

    companion object {
        private const val SERVICE           = "com.example.billionseconds.tokens"
        private const val KEY_ACCESS_TOKEN  = "bs_access_token"
        private const val KEY_REFRESH_TOKEN = "bs_refresh_token"
        private const val KEY_USER_ID       = "bs_user_id"
        private const val KEY_DEVICE_ID     = "bs_device_id"
    }

    // ------- TokenStorage interface -------

    override fun getAccessToken(): String?   = keychainGet(KEY_ACCESS_TOKEN)  ?: migrateFromDefaults(KEY_ACCESS_TOKEN)
    override fun setAccessToken(v: String?)  = if (v != null) keychainSet(KEY_ACCESS_TOKEN, v)  else keychainDelete(KEY_ACCESS_TOKEN)

    override fun getRefreshToken(): String?  = keychainGet(KEY_REFRESH_TOKEN) ?: migrateFromDefaults(KEY_REFRESH_TOKEN)
    override fun setRefreshToken(v: String?) = if (v != null) keychainSet(KEY_REFRESH_TOKEN, v) else keychainDelete(KEY_REFRESH_TOKEN)

    override fun getUserId(): String?        = keychainGet(KEY_USER_ID)       ?: migrateFromDefaults(KEY_USER_ID)
    override fun setUserId(v: String?)       = if (v != null) keychainSet(KEY_USER_ID, v)       else keychainDelete(KEY_USER_ID)

    override fun getDeviceId(): String?      = keychainGet(KEY_DEVICE_ID)     ?: migrateFromDefaults(KEY_DEVICE_ID)
    override fun setDeviceId(id: String)     = keychainSet(KEY_DEVICE_ID, id)

    override fun clear() {
        val deviceId = getDeviceId()
        keychainDelete(KEY_ACCESS_TOKEN)
        keychainDelete(KEY_REFRESH_TOKEN)
        keychainDelete(KEY_USER_ID)
        deviceId?.let { keychainSet(KEY_DEVICE_ID, it) }
    }

    // ------- Keychain CRUD -------

    private fun keychainGet(key: String): String? = memScoped {
        val query = baseQuery(key).also { dict ->
            dict.setObject(kCFBooleanTrue.asNSObject()!!, forKey = kSecReturnData.asNSString()!!)
            dict.setObject(kSecMatchLimitOne.asNSObject()!!, forKey = kSecMatchLimit.asNSString()!!)
        }
        // alloc<CPointerVar<CPointed>>: конкретный тип, без `out`-variance.
        // result.ptr не компилируется (reified T не выводится для параметрических типов),
        // поэтому берём rawPtr аллоцированного слота и reinterpret-им через interpretCPointer.
        val result = alloc<CPointerVar<CPointed>>()
        @Suppress("UNCHECKED_CAST")
        val resultRef = interpretCPointer<CPointerVar<CPointed>>(result.rawPtr)!!
            as CValuesRef<CPointerVarOf<CPointer<out CPointed>>>
        if (SecItemCopyMatching(query.toCFDict(), resultRef) != errSecSuccess) return@memScoped null

        val rawPtr = result.value?.rawValue ?: return@memScoped null
        val data: NSData = interpretObjCPointerOrNull(rawPtr) ?: return@memScoped null
        NSString.create(data, NSUTF8StringEncoding)?.description
    }

    private fun keychainSet(key: String, value: String) {
        val data = value.toNSData() ?: return

        val updateAttrs = NSMutableDictionary().also {
            it.setObject(data, forKey = kSecValueData.asNSString()!!)
        }
        val status = SecItemUpdate(baseQuery(key).toCFDict(), updateAttrs.toCFDict())

        if (status == errSecItemNotFound) {
            SecItemAdd(
                baseQuery(key).also { dict ->
                    dict.setObject(data, forKey = kSecValueData.asNSString()!!)
                    // kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly — токены доступны
                    // после первой разблокировки, в т.ч. для фонового sync. Не уходят в iCloud.
                    dict.setObject(
                        kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly.asNSObject()!!,
                        forKey = kSecAttrAccessible.asNSString()!!
                    )
                }.toCFDict(),
                null
            )
        }
    }

    private fun keychainDelete(key: String) {
        SecItemDelete(baseQuery(key).toCFDict())
    }

    private fun baseQuery(key: String) = NSMutableDictionary().also { dict ->
        dict.setObject(kSecClassGenericPassword.asNSObject()!!, forKey = kSecClass.asNSString()!!)
        dict.setObject(SERVICE,  forKey = kSecAttrService.asNSString()!!)
        dict.setObject(key,      forKey = kSecAttrAccount.asNSString()!!)
    }

    // ------- NSUserDefaults → Keychain migration -------
    // При первом чтении каждого ключа переносим значение из NSUserDefaults в Keychain
    // и удаляем небезопасную копию. Выполняется прозрачно для вызывающего кода.
    private fun migrateFromDefaults(key: String): String? {
        val value = NSUserDefaults.standardUserDefaults.stringForKey(key) ?: return null
        keychainSet(key, value)
        NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
        return value
    }

    // ------- Toll-free bridge helpers -------

    // CFStringRef/CFBooleanRef → NSString (ключи словаря; NSString реализует NSCopying)
    private fun <T : CPointed> CPointer<T>?.asNSString(): NSString? =
        this?.rawValue?.let { interpretObjCPointer(it) }

    // CFStringRef/CFBooleanRef → NSObject (значения словаря)
    private fun <T : CPointed> CPointer<T>?.asNSObject(): NSObject? =
        this?.rawValue?.let { interpretObjCPointer(it) }

    // NSMutableDictionary → CFDictionaryRef через toll-free bridge (NSDict IS CFDict в runtime).
    // __CFDictionary не экспортируется из platform.CoreFoundation, поэтому идём через CPointed:
    // interpretCPointer<CPointed> даёт CPointer<CPointed>, затем @Suppress cast до CFDictionaryRef.
    // Безопасно: оба типа — один и тот же адрес памяти (toll-free bridge).
    @Suppress("UNCHECKED_CAST")
    private fun NSMutableDictionary.toCFDict(): CFDictionaryRef =
        interpretCPointer<CPointed>(objcPtr())!! as CFDictionaryRef

    // String → NSData (UTF-8). Использует usePinned — безопаснее, чем cast через NSString.
    private fun String.toNSData(): NSData? {
        val bytes = encodeToByteArray()
        if (bytes.isEmpty()) return null
        return bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
    }
}

actual fun createTokenStorage(): TokenStorage = IosTokenStorage()
