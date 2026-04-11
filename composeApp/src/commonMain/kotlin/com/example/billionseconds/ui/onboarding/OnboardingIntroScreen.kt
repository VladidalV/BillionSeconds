package com.example.billionseconds.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billionseconds.mvi.AppIntent
import com.example.billionseconds.ui.theme.AppColors
import com.example.billionseconds.ui.theme.AppColors.backgroundDark
import kotlin.random.Random

@Composable
fun OnboardingIntroScreen(
    onIntent: (AppIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundDark)
    ) {
        // Decorative ambient glow elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Purple glow — top-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowPurple, Color.Transparent),
                    center = Offset(size.width - 39.dp.toPx() + 250.dp.toPx(), 116.dp.toPx() + 250.dp.toPx()),
                    radius  = 250.dp.toPx()
                ),
                radius = 250.dp.toPx(),
                center = Offset(size.width - 39.dp.toPx() + 250.dp.toPx(), 116.dp.toPx() + 250.dp.toPx())
            )
            // Blue glow — bottom-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.glowBlue, Color.Transparent),
                    center = Offset(-39.dp.toPx() + 150.dp.toPx(), size.height - 232.dp.toPx()),
                    radius  = 150.dp.toPx()
                ),
                radius = 150.dp.toPx(),
                center = Offset(-39.dp.toPx() + 150.dp.toPx(), size.height - 232.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 64.dp, start = 24.dp, end = 24.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            HeroSection()
            Spacer(Modifier.height(80.dp))
            BentoSection()
            Spacer(Modifier.height(64.dp))
            CtaSection(onIntent)
        }
    }
}

@Composable
private fun HeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Eyebrow label
        Text(
            text = "A TEMPORAL ARTIFACT",
            color = AppColors.purpleAccent.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 3.6.sp
        )

        Spacer(Modifier.height(16.dp))

        // Main heading — 1,000,000,000 in accent purple
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = AppColors.textHeading)) {
                    append("Every human\nlives\n")
                }
                withStyle(SpanStyle(color = AppColors.purpleAccent)) {
                    append("1,000,000,000\n")
                }
                withStyle(SpanStyle(color = AppColors.textHeading)) {
                    append("seconds.")
                }
            },
            fontSize = 60.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 54.sp,
            letterSpacing = (-2.4).sp
        )

        Spacer(Modifier.height(32.dp))

        // Horizontal divider + subtitle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(1.dp)
                    .background(AppColors.divider)
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = "It happens only once.",
                color = AppColors.textBody,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun BentoSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CosmosCard()
        MilestoneInfoCard()
        SocialProofCard()
    }
}

@Composable
private fun CosmosCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.cardDark)
    ) {
        // Procedural star field
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rng = Random(seed = 42)
            val colors = listOf(
                Color.White.copy(alpha = 0.75f),
                AppColors.purpleAccent.copy(alpha = 0.55f),
                AppColors.blueAccent.copy(alpha = 0.45f)
            )
            repeat(110) { i ->
                drawCircle(
                    color  = colors[i % colors.size],
                    radius = rng.nextFloat() * 1.5f + 0.3f,
                    center = Offset(rng.nextFloat() * size.width, rng.nextFloat() * size.height)
                )
            }
            // Subtle nebula glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppColors.purpleAccent.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(size.width * 0.65f, size.height * 0.35f),
                    radius = size.minDimension * 0.45f
                ),
                radius = size.minDimension * 0.45f,
                center = Offset(size.width * 0.65f, size.height * 0.35f)
            )
        }

        // Bottom vignette fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, AppColors.backgroundDark.copy(alpha = 0.6f))
                    )
                )
        )

        // Text overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Track the Unseen",
                color = AppColors.textHeading,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 28.sp
            )
            Text(
                text = "Visualize the relentless flow of your singular existence across the cosmos.",
                color = AppColors.textBody,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun MilestoneInfoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.cardMid)
            .border(1.dp, AppColors.cardBorder, RoundedCornerShape(12.dp))
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Gradient progress bar (31.7% ≈ billionth-second milestone)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(AppColors.backgroundDark)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.317f)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)))
            )
        }

        Text(
            text = "Most people pass their billionth second around age 31.7. Where are you in your journey?",
            color = AppColors.textBody,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun SocialProofCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.overlayCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Stacked avatar circles with overlap
        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.avatarDark)
                    .border(2.dp, AppColors.avatarBorder, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.avatarPurple)
                    .border(2.dp, AppColors.avatarBorder, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AppColors.avatarBlue)
                    .border(2.dp, AppColors.avatarBorder, CircleShape)
            )
        }

        Text(
            text = "JOINED THE VOID",
            color = AppColors.textBody,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun CtaSection(onIntent: (AppIntent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Gradient CTA button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Brush.linearGradient(listOf(AppColors.buttonGradientStart, AppColors.buttonGradientEnd)))
                .clickable { onIntent(AppIntent.StartClicked) }
                .padding(vertical = 22.dp, horizontal = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Begin Journey",
                color = AppColors.buttonText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.45).sp
            )
        }

        Text(
            text = "By beginning, you acknowledge the finite nature of time and the beauty of the present.",
            color = AppColors.textSubtle,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 3.dp)
        )
    }
}