package com.example.billionseconds.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {

    // ── Backgrounds ───────────────────────────────────────────────────────────
    val backgroundDark  = Color(0xFF070D1F)
    val cardDark        = Color(0xFF151B2D)
    val cardMid         = Color(0xFF23293C)
    val overlayCard     = Color(0xFF2E3447).copy(alpha = 0.4f)

    // ── Accents ───────────────────────────────────────────────────────────────
    val purpleAccent    = Color(0xFFD0BCFF)
    val blueAccent      = Color(0xFFADC6FF)

    // ── Text ──────────────────────────────────────────────────────────────────
    val textHeading     = Color(0xFFDCE1FB)
    val textBody        = Color(0xFFCBC3D7)
    val textSubtle      = Color(0xFFCBC3D7).copy(alpha = 0.6f)

    // ── Dividers / Borders ────────────────────────────────────────────────────
    val divider         = Color(0xFF494454).copy(alpha = 0.3f)
    val cardBorder      = Color.White.copy(alpha = 0.03f)
    val avatarBorder    = Color(0xFF0C1324)

    // ── Button ────────────────────────────────────────────────────────────────
    val buttonGradientStart = Color(0xFFD0BCFF)
    val buttonGradientEnd   = Color(0xFFA078FF)
    val buttonText          = Color(0xFF3C0091)

    // ── Avatar placeholders ───────────────────────────────────────────────────
    val avatarDark          = Color(0xFF191F31)
    val avatarPurple        = Color(0xFFD0BCFF).copy(alpha = 0.2f)
    val avatarBlue          = Color(0xFFADC6FF).copy(alpha = 0.2f)

    // ── Glow / Decorative ─────────────────────────────────────────────────────
    val glowPurple      = Color(0xFFD0BCFF).copy(alpha = 0.12f)
    val glowBlue        = Color(0xFFADC6FF).copy(alpha = 0.10f)
}