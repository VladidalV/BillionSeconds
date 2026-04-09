package com.example.billionseconds

import kotlinx.browser.window

actual fun exitApp() {
    window.close()
}
