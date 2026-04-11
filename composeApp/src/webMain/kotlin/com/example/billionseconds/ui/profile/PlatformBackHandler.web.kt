package com.example.billionseconds.ui.profile

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Web не имеет нативной кнопки Back, браузер управляет историей сам
}
