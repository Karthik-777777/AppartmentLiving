package com.simats.appartmentliving.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        launch {
            rotation.animateTo(
                targetValue = -8f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
        }
        delay(2200)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0E)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Tilted container box
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .rotate(rotation.value),
                contentAlignment = Alignment.Center
            ) {
                // Outer rotated gradient border
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF1D4ED8),
                                    Color(0xFF3B82F6).copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                )

                // Inner dark card (centered)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF18181C), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Counter-rotated building icon to keep it straight (rotation + -rotation = 0)
                    Building2Icon(
                        modifier = Modifier
                            .size(36.dp)
                            .rotate(-rotation.value),
                        tint = Color(0xFF3B82F6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Apartment Living",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your apartment, managed smarter.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun Building2Icon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF3B82F6)
) {
    val path = remember {
        Path().apply {
            // Windows
            moveTo(10f, 12f)
            lineTo(14f, 12f)
            moveTo(10f, 8f)
            lineTo(14f, 8f)
            
            // Door
            moveTo(14f, 21f)
            lineTo(14f, 18f)
            quadraticTo(14f, 16f, 12f, 16f)
            quadraticTo(10f, 16f, 10f, 18f)
            lineTo(10f, 21f)
            
            // Outer Wings
            moveTo(6f, 10f)
            lineTo(4f, 10f)
            quadraticTo(2f, 10f, 2f, 12f)
            lineTo(2f, 19f)
            quadraticTo(2f, 21f, 4f, 21f)
            lineTo(20f, 21f)
            quadraticTo(22f, 21f, 22f, 19f)
            lineTo(22f, 9f)
            quadraticTo(22f, 7f, 20f, 7f)
            lineTo(18f, 7f)
            
            // Main Central Tower
            moveTo(6f, 21f)
            lineTo(6f, 5f)
            quadraticTo(6f, 3f, 8f, 3f)
            lineTo(16f, 3f)
            quadraticTo(18f, 3f, 18f, 5f)
            lineTo(18f, 21f)
        }
    }

    Canvas(modifier = modifier) {
        scale(
            scaleX = size.width / 24f,
            scaleY = size.height / 24f,
            pivot = Offset.Zero
        ) {
            drawPath(
                path = path,
                color = tint,
                style = Stroke(
                    width = 2f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}
