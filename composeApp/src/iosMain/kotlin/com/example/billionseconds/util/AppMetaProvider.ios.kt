package com.example.billionseconds.util

import platform.Foundation.NSBundle

actual object AppMetaProvider {
    actual fun getVersion(): String {
        val info = NSBundle.mainBundle.infoDictionary
        val version = info?.get("CFBundleShortVersionString") as? String ?: "1.0"
        val build = info?.get("CFBundleVersion") as? String ?: "1"
        return "$version ($build)"
    }
}
