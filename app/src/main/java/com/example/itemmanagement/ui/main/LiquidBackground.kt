package com.example.itemmanagement.ui.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.itemmanagement.ui.theme.*
import kotlin.math.sin

@Composable
fun LiquidBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_bg")
    
    // Animate a phase value from 0 to 2PI for smooth sine wave movements
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Base background color
        drawRect(color = BackgroundLight)
        
        // Dynamic floating halos
        drawHalo(
            color = AccentTeal.copy(alpha = 0.15f),
            center = Offset(
                x = width * 0.2f + sin(phase) * 100f,
                y = height * 0.3f + sin(phase * 0.8f) * 100f
            ),
            radius = width * 0.6f
        )
        
        drawHalo(
            color = AccentPurple.copy(alpha = 0.1f),
            center = Offset(
                x = width * 0.8f + sin(phase * 1.2f) * 120f,
                y = height * 0.6f + sin(phase * 0.9f) * 120f
            ),
            radius = width * 0.7f
        )
        
        drawHalo(
            color = PrimaryBlue.copy(alpha = 0.08f),
            center = Offset(
                x = width * 0.5f + sin(phase * 0.7f) * 80f,
                y = height * 0.9f + sin(phase * 1.1f) * 80f
            ),
            radius = width * 0.8f
        )
    }
}

private fun DrawScope.drawHalo(color: Color, center: Offset, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
