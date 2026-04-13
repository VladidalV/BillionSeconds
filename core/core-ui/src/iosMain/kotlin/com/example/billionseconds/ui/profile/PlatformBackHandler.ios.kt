package com.example.billionseconds.ui.profile

import androidx.compose.runtime.Composable

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS использует системный свайп-жест, дополнительная обработка не нужна
}
