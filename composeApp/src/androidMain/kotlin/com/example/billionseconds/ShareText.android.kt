package com.example.billionseconds

import android.content.Intent
import com.example.billionseconds.data.AppContext

actual fun shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, null).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    AppContext.get().startActivity(chooser)
}
