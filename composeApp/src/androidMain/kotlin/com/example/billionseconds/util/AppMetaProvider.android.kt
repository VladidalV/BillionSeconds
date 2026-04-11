package com.example.billionseconds.util

import com.example.billionseconds.data.AppContext

actual object AppMetaProvider {
    actual fun getVersion(): String {
        return try {
            val ctx = AppContext.get()
            val pInfo = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            val versionName = pInfo.versionName ?: "1.0"
            val versionCode = pInfo.longVersionCode
            "$versionName ($versionCode)"
        } catch (e: Exception) {
            "1.0"
        }
    }
}
