package com.example.billionseconds

import android.content.Intent
import android.net.Uri
import com.example.billionseconds.data.AppContext

actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AppContext.get().startActivity(intent)
}
