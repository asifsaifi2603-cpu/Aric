package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.NeonPurple
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AricOrbVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    isProcessing: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isProcessing) 2500 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    // Dynamic scale driven by speech audio level
    val audioScale = remember { Animatable(1f) }
    LaunchedEffect(rmsLevel, isListening, isSpeaking) {
        val target = if (isListening) {
            1f + (rmsLevel / 10f).coerceIn(0f, 0.5f)
        } else if (isSpeaking) {
            1.15f
        } else {
            1f
        }
        audioScale.animateTo(target, tween(100))
    }

    val primaryColor = when {
        isListening -> CyanPrimary
        isSpeaking -> NeonPurple
        isProcessing -> NeonIndigo
        else -> CyanPrimary
    }

    val secondaryColor = when {
        isListening -> Color(0xFF38BDF8)
        isSpeaking -> Color(0xFFC084FC)
        isProcessing -> Color(0xFF6366F1)
        else -> Color(0xFF0284C7)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.72f * breathingScale * audioScale.value

            // 1. Outermost glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (isListening) 0.35f else 0.18f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.4f
                ),
                radius = baseRadius * 1.4f,
                center = center
            )

            // 2. Animated orbital particle nodes
            val numNodes = if (isProcessing) 8 else 6
            for (i in 0 until numNodes) {
                val angleRad = Math.toRadians((rotationAngle + (i * 360f / numNodes)).toDouble())
                val orbitRadius = baseRadius * 1.15f
                val nodeX = center.x + (orbitRadius * cos(angleRad)).toFloat()
                val nodeY = center.y + (orbitRadius * sin(angleRad)).toFloat()

                drawCircle(
                    color = primaryColor.copy(alpha = 0.8f),
                    radius = if (isListening) 5f else 3.5f,
                    center = Offset(nodeX, nodeY)
                )
            }

            // 3. Orbital circuit ring
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.2f),
                        primaryColor.copy(alpha = 0.8f)
                    ),
                    center = center
                ),
                radius = baseRadius * 1.15f,
                center = center,
                style = Stroke(width = if (isListening) 3.5f else 2f)
            )

            // 4. Secondary counter-rotating inner orbital ring
            drawCircle(
                color = secondaryColor.copy(alpha = 0.45f),
                radius = baseRadius * 0.95f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // 5. Core holographic sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.85f),
                        secondaryColor.copy(alpha = 0.6f),
                        Color(0xFF030712)
                    ),
                    center = Offset(center.x - baseRadius * 0.2f, center.y - baseRadius * 0.2f),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // 6. Neural core highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.7f),
                radius = baseRadius * 0.25f,
                center = Offset(center.x - baseRadius * 0.25f, center.y - baseRadius * 0.25f)
            )
        }
    }
}
