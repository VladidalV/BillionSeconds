package com.example.billionseconds.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.mvi.ActiveProfileSummary
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.mvi.ProfileSubScreen
import com.example.billionseconds.mvi.ProfileUiState
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun ProfileRootContent(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 80.dp,    // below top bar
                bottom = bottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            // Hero: avatar + name + subtitle
            item {
                ProfileHeroSection(
                    summary = uiState.activeProfileSummary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Premium status card
            item {
                PremiumCard(
                    onManageClick = { onIntent(AppIntent.PremiumClicked) },
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            // Settings groups
            item {
                ProfileSettingsGroups(uiState = uiState, onIntent = onIntent)
            }
        }

        // Frosted top bar
        ProfileTopBar()
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.ProfileTopBar() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.headerBackground)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "✦",
                color = AppColors.purpleAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Billion Seconds",
                color = AppColors.purpleAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.36).sp
            )
        }
        // Search / settings icon placeholder
        Text(
            text = "⊙",
            color = AppColors.textLabel,
            fontSize = 18.sp
        )
    }
}

// ── Hero Section ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroSection(
    summary: ActiveProfileSummary?,
    modifier: Modifier = Modifier
) {
    val name = summary?.name ?: "Your Profile"
    val subtitle = summary?.billionDateText

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Avatar with gradient border
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center
        ) {
            // Gradient ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.purpleAccent, AppColors.blueAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Dark border + initials circle
                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(CircleShape)
                        .background(AppColors.backgroundScreen)
                        .border(4.dp, AppColors.backgroundScreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(AppColors.cardMid),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.initials(),
                            color = AppColors.purpleAccent,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Name
        Text(
            text = name,
            color = AppColors.textHeading,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1.5).sp,
            textAlign = TextAlign.Center
        )

        // Subtitle
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = AppColors.textBody,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.35.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun String.initials(): String =
    split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

// ── Premium Card ──────────────────────────────────────────────────────────────

@Composable
private fun PremiumCard(
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(AppColors.cardMid, AppColors.cardDeep),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header row: badge + star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "STELLAR TIER" badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.purpleAccent.copy(alpha = 0.2f))
                        .border(1.dp, AppColors.purpleAccent.copy(alpha = 0.2f), CircleShape)
                        .padding(horizontal = 13.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "STELLAR TIER",
                        color = AppColors.purpleAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                }
                Text(text = "★", color = AppColors.purpleAccent, fontSize = 20.sp)
            }

            Spacer(Modifier.height(2.dp))

            Text(
                text = "Premium Member",
                color = AppColors.textHeading,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp
            )

            Text(
                text = "Unlimited Time Capsules and enhanced temporal analytics active until Dec 2025.",
                color = AppColors.textBody,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(10.dp))

            // Manage Subscription button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                        )
                    )
                    .clickable { onManageClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Manage Subscription",
                    color = AppColors.premiumButtonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.35).sp
                )
            }
        }
    }
}

// ── Settings Groups ───────────────────────────────────────────────────────────

@Composable
private fun ProfileSettingsGroups(
    uiState: ProfileUiState,
    onIntent: (AppIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        // Group: Temporal Tools
        ProfileGroup(title = "Temporal Tools") {
            ProfileNavRow(
                iconEmoji = "⏳",
                iconTint = AppColors.purpleAccent.copy(alpha = 0.1f),
                title = "Time Capsule",
                subtitle = "4 active memories in orbit",
                onClick = { onIntent(AppIntent.TimeCapsuleClicked) }
            )
            ProfileGroupDivider()
            ProfileNavRow(
                iconEmoji = "~",
                iconTint = AppColors.blueAccent.copy(alpha = 0.1f),
                title = "Life Statistics",
                subtitle = "View your temporal breakdown",
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.AboutApp)) }
            )
        }

        // Group: System
        ProfileGroup(title = "System") {
            ProfileToggleRow(
                iconEmoji = "🔔",
                iconTint = AppColors.textBody.copy(alpha = 0.1f),
                title = "Notifications",
                checked = uiState.settings.notificationsEnabled,
                onToggle = { onIntent(AppIntent.NotificationsToggled) }
            )
            ProfileGroupDivider()
            ProfileNavRow(
                iconEmoji = "🔒",
                iconTint = AppColors.textBody.copy(alpha = 0.1f),
                title = "Privacy & Security",
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.DataManagement)) }
            )
            ProfileGroupDivider()
            ProfileNavRow(
                iconEmoji = "◑",
                iconTint = AppColors.textBody.copy(alpha = 0.1f),
                title = "Appearance",
                trailingLabel = "Dark Orbit",
                onClick = { onIntent(AppIntent.ProfileSubScreenSelected(ProfileSubScreen.AppSettings)) }
            )
        }

        // Danger zone
        DangerZoneButton(
            onClick = { onIntent(AppIntent.ClearAllDataClicked) }
        )

        Spacer(Modifier.height(8.dp))
    }
}

// ── Group Container ───────────────────────────────────────────────────────────

@Composable
private fun ProfileGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        Text(
            text = title.uppercase(),
            color = AppColors.textLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(AppColors.cardDark),
            content = content
        )
    }
}

@Composable
private fun ProfileGroupDivider() {
    HorizontalDivider(
        color = AppColors.ringDecoration,
        thickness = 1.dp
    )
}

// ── Row Items ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileNavRow(
    iconEmoji: String,
    iconTint: Color,
    title: String,
    subtitle: String? = null,
    trailingLabel: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon square
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconTint),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconEmoji, fontSize = 16.sp)
        }

        // Text block
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AppColors.textHeading,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = AppColors.textBody,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Trailing
        if (trailingLabel != null) {
            Text(
                text = trailingLabel,
                color = AppColors.textLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Text(
            text = "›",
            color = AppColors.textLabel,
            fontSize = 18.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun ProfileToggleRow(
    iconEmoji: String,
    iconTint: Color,
    title: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon square
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconTint),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconEmoji, fontSize = 16.sp)
        }

        // Title
        Text(
            text = title,
            color = AppColors.textHeading,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )

        // Custom styled switch
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedTrackColor   = AppColors.purpleAccent.copy(alpha = 0.35f),
                checkedThumbColor   = AppColors.purpleAccent,
                checkedBorderColor  = Color.Transparent,
                uncheckedTrackColor = AppColors.stepInactive,
                uncheckedThumbColor = AppColors.purpleAccent.copy(alpha = 0.7f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

// ── Danger Zone ───────────────────────────────────────────────────────────────

@Composable
private fun DangerZoneButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AppColors.dangerBackground)
            .clickable { onClick() }
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "⎋", color = AppColors.textDanger, fontSize = 14.sp)
            Text(
                text = "Logout of Nebula",
                color = AppColors.textDanger,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.35).sp
            )
        }
    }
}

// ── Legacy helpers (used by sub-screens) ──────────────────────────────────────

@Composable
internal fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = AppColors.textLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.8.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
internal fun NavigationRow(
    emoji: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = AppColors.textHeading, fontSize = 14.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = AppColors.textBody, fontSize = 12.sp)
            }
        }
        Text(text = "›", color = AppColors.textLabel, fontSize = 18.sp)
    }
}

@Composable
internal fun StubNavigationRow(
    emoji: String,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp, color = AppColors.textBody.copy(alpha = 0.4f))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AppColors.textHeading.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = AppColors.textBody.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
internal fun ToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (enabled) AppColors.textHeading else AppColors.textHeading.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = if (enabled) AppColors.textBody else AppColors.textBody.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle() },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedTrackColor    = AppColors.purpleAccent.copy(alpha = 0.35f),
                checkedThumbColor    = AppColors.purpleAccent,
                checkedBorderColor   = Color.Transparent,
                uncheckedTrackColor  = AppColors.stepInactive,
                uncheckedThumbColor  = AppColors.purpleAccent.copy(alpha = 0.7f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
