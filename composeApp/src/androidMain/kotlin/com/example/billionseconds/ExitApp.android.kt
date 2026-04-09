package com.example.billionseconds

import com.example.billionseconds.data.AppContext
import android.app.Activity

actual fun exitApp() {
    (AppContext.get() as? Activity)?.finish()
        ?: kotlin.system.exitProcess(0)
}
