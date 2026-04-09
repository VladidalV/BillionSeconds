package com.example.billionseconds

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

actual fun shareText(text: String) {
    val controller = UIActivityViewController(listOf(text), null)
    UIApplication.sharedApplication.keyWindow?.rootViewController
        ?.presentViewController(controller, animated = true, completion = null)
}
