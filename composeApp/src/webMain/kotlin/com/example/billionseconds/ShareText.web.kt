package com.example.billionseconds

import kotlinx.browser.window

actual fun shareText(text: String) {
    // Fallback: copy to clipboard via navigator
    window.navigator.clipboard.writeText(text)
}
