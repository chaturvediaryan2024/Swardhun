package com.aryan.calculator.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aryan.calculator.ui.theme.AccentLime
import com.aryan.calculator.ui.theme.AccentLimeDark
import com.aryan.calculator.ui.theme.BgDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    var start by remember { mutableStateOf(false) }

    val badgeScale by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "badgeScale"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = tween(400, delayMillis = 150),
        label = "textAlpha"
    )

    // Pulsing glow behind the badge
    val glow = rememberInfiniteTransition(label = "glow")
    val glowScale by glow.animateFloat(
        initialValue = 1f, targetValue = 1.35f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowScale"
    )

    LaunchedEffect(Unit) {
        start = true
        delay(1400)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1A1440), BgDark, Color(0xFF05070E)))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                // Glow
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(badgeScale * glowScale)
                        .alpha(0.35f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(AccentLime.copy(alpha = 0.6f), Color.Transparent))
                        )
                )
                // Badge
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(badgeScale)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AccentLime, AccentLimeDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.MusicNote, null, tint = Color.Black, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "Musify",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                ),
                color = Color.White,
                modifier = Modifier.alpha(textAlpha)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Feel the Music",
                style = MaterialTheme.typography.bodyMedium,
                color = AccentLime.copy(alpha = 0.9f),
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(28.dp))

            // Animated equalizer bars (unique premium touch)
            WaveformBars(active = start, modifier = Modifier.alpha(textAlpha))
        }

        // Credits
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 44.dp)
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Developed by Aaru", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.45f))
            Spacer(Modifier.height(3.dp))
            Text("ver 1.0 • 2026", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.28f))
        }
    }
}

@Composable
private fun WaveformBars(active: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bars")
    val heights = List(7) { i ->
        transition.animateFloat(
            initialValue = 10f,
            targetValue = 40f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 420 + i * 90, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$i"
        )
    }
    Row(
        modifier = modifier.height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(if (active) h.value.dp else 10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.verticalGradient(listOf(AccentLime, AccentLimeDark)))
            )
        }
    }
}
