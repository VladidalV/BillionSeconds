package com.example.billionseconds

import androidx.activity.ComponentActivity
import java.lang.ref.WeakReference

object ActivityHolder {
    private var ref: WeakReference<ComponentActivity>? = null

    fun set(activity: ComponentActivity) {
        ref = WeakReference(activity)
    }

    fun clear() {
        ref = null
    }

    fun get(): ComponentActivity? = ref?.get()
}
