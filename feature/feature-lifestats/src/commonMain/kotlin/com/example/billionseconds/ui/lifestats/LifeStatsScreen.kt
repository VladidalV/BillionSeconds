package com.example.billionseconds.ui.lifestats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppConstants.bottomPadding

@Composable
fun LifeStatsScreen(
    uiState: LifeStatsUiState,
    onAction: (LifeStatsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onAction(LifeStatsAction.ScreenStarted)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundScreen)
    ) {
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.purpleAccent)
                }
            }

            uiState.error == LifeStatsError.NoBirthData -> {
                NoBirthDataPlaceholder()
            }

            else -> {
                LifeStatsContent(uiState = uiState)
            }
        }
    }
}

// ── Main Content ──────────────────────────────────────────────────────────────

@Composable
private fun LifeStatsContent(uiState: LifeStatsUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient glows
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width - 60.dp.toPx(), 160.dp.toPx()),
                    radius = 210.dp.toPx()
                ),
                radius = 210.dp.toPx(),
                center = Offset(size.width - 60.dp.toPx(), 160.dp.toPx())
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(60.dp.toPx(), size.height - 180.dp.toPx()),
                    radius = 190.dp.toPx()
                ),
                radius = 190.dp.toPx(),
                center = Offset(60.dp.toPx(), size.height - 180.dp.toPx())
            )
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 96.dp, bottom = bottomPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Age hero
            AgeHeroCard(
                ageLabel = uiState.ageLabel,
                isApproximate = uiState.isUnknownBirthTime
            )

            // Exact stats
            if (uiState.exactStats.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                StatsSectionHeader(title = "ТОЧНЫЕ ДАННЫЕ")
                Spacer(Modifier.height(10.dp))
                StatsList(items = uiState.exactStats, isApproximateSection = false)
            }

            // Approximate stats
            if (uiState.approximateStats.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                StatsSectionHeader(title = "ПРИБЛИЗИТЕЛЬНО")
                Spacer(Modifier.height(10.dp))
                if (uiState.isUnknownBirthTime) {
                    ApproximateDisclaimer(
                        text = "Время рождения не указано. Все расчёты выполнены с полудня дня рождения."
                    )
                    Spacer(Modifier.height(10.dp))
                }
                StatsList(items = uiState.approximateStats, isApproximateSection = true)
            }
        }

        // Frosted glass top bar
        StatsTopBar()
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.StatsTopBar() {
    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .fillMaxWidth()
            .height(64.dp)
            .background(AppColors.headerBackground)
            .padding(horizontal = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "✦",
            color = AppColors.purpleAccent,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Life Data",
            color = AppColors.purpleAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.36).sp
        )
    }
}

// ── Age Hero Card ─────────────────────────────────────────────────────────────

@Composable
private fun AgeHeroCard(ageLabel: String, isApproximate: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.cardDark)
            .border(1.dp, AppColors.cardBorder, RoundedCornerShape(20.dp))
    ) {
        // Top gradient sheen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.purpleAccent.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "TEMPORAL AGE",
                color = AppColors.textLabel,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(Modifier.height(4.dp))

            val displayAge = if (isApproximate) "≈ $ageLabel" else ageLabel
            Text(
                text = displayAge,
                style = TextStyle(
                    brush = Brush.linearGradient(
                        listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)
                    ),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.2).sp,
                    textAlign = TextAlign.Center
                ),
                textAlign = TextAlign.Center
            )

            if (isApproximate) {
                Text(
                    text = "время рождения не указано",
                    color = AppColors.textSubtle,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun StatsSectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AppColors.purpleAccent.copy(alpha = 0.6f))
        )
        Text(
            text = title,
            color = AppColors.textLabel,
            fontSize = 11.sp,
            letterSpacing = 2.4.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

// ── Approximate Disclaimer ────────────────────────────────────────────────────

@Composable
private fun ApproximateDisclaimer(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.dangerBackground)
            .border(1.dp, AppColors.textDanger.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = "◎", color = AppColors.textDanger, fontSize = 13.sp)
        Text(
            text = text,
            color = AppColors.textDanger,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

// ── Stats List ────────────────────────────────────────────────────────────────

@Composable
private fun StatsList(items: List<StatItem>, isApproximateSection: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            StatCard(item = item, isApproximateSection = isApproximateSection)
        }
    }
}

// ── Stat Card ─────────────────────────────────────────────────────────────────

@Composable
private fun StatCard(item: StatItem, isApproximateSection: Boolean) {
    val valueColor = if (item.isApproximate || isApproximateSection)
        AppColors.blueAccent
    else
        AppColors.purpleAccent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.cardDark)
            .border(1.dp, AppColors.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    color = AppColors.textBody,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                )
                item.disclaimer?.let { disclaimer ->
                    Text(
                        text = disclaimer,
                        color = AppColors.textSubtle,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = item.value,
                color = valueColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp,
                textAlign = TextAlign.End
            )
        }
    }
}

// ── No Birth Data ─────────────────────────────────────────────────────────────

@Composable
private fun NoBirthDataPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "◈", color = AppColors.purpleAccent, fontSize = 48.sp)

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Нет данных",
                color = AppColors.textHeading,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Пройдите онбординг, чтобы\nувидеть статистику.",
                color = AppColors.textBody,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}
