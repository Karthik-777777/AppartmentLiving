package com.simats.appartmentliving.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val illustrationType: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onNavigateToLogin: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPageData(
            title = "Stay updated with\nannouncements",
            description = "Get instant notifications about society\nmeetings, maintenance schedules,\nand important events.",
            illustrationType = "bell"
        ),
        OnboardingPageData(
            title = "Manage security and\nvisitors",
            description = "Pre-approve your guests, track visitor\nentries, and ensure security for your\nfamily and home.",
            illustrationType = "shield"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0E))
    ) {
        // Skip Button (Top Right)
        Text(
            text = "Skip",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 56.dp, end = 24.dp)
                .clickable { onNavigateToLogin() }
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Horizontal Pager for Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { position ->
                val page = pages[position]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustration Circle Container
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .background(Color(0xFF16161A).copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Floating card 1 (Bottom Left)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.BottomStart)
                                .offset(x = 36.dp, y = (-48).dp)
                                .background(Color(0xFF0C0C0E).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        )

                        // Floating card 2 (Top Right)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = (-36).dp, y = 48.dp)
                                .background(Color(0xFF0C0C0E).copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        )

                        // Center Icon
                        OnboardingIcon(
                            type = page.illustrationType,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFF3B82F6)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Title
                    Text(
                        text = page.title,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = page.description,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Bottom Actions (Indicator + Next button)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(2) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 6.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF3B82F6) else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Next Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < 1) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onNavigateToLogin()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (pagerState.currentPage == 1) "Get Started" else "Next",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingIcon(
    type: String,
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF3B82F6)
) {
    val path = remember(type) {
        Path().apply {
            when (type) {
                "warning" -> {
                    // Chat bubble with rounded corners
                    moveTo(5f, 3f)
                    lineTo(19f, 3f)
                    quadraticTo(21f, 3f, 21f, 5f)
                    lineTo(21f, 13f)
                    quadraticTo(21f, 15f, 19f, 15f)
                    lineTo(8f, 15f)
                    lineTo(4f, 19f)
                    lineTo(4f, 15f)
                    quadraticTo(3f, 15f, 3f, 14f)
                    lineTo(3f, 5f)
                    quadraticTo(3f, 3f, 5f, 3f)
                    close()
                    
                    // Exclamation Mark
                    moveTo(12f, 7f)
                    lineTo(12f, 11f)
                    moveTo(12f, 13.8f)
                    lineTo(12f, 14f)
                }
                "bell" -> {
                    // Bell dome outline
                    moveTo(6f, 8f)
                    quadraticTo(6f, 3f, 12f, 3f)
                    quadraticTo(18f, 3f, 18f, 8f)
                    quadraticTo(18f, 15f, 21f, 17f)
                    lineTo(3f, 17f)
                    quadraticTo(6f, 15f, 6f, 8f)
                    
                    // Bell clapper
                    moveTo(10.3f, 19f)
                    quadraticTo(12f, 22f, 13.7f, 19f)
                }
                "shield" -> {
                    // Shield outline
                    moveTo(12f, 2f)
                    lineTo(20f, 5f)
                    lineTo(20f, 12f)
                    quadraticTo(20f, 18f, 12f, 22f)
                    quadraticTo(4f, 18f, 4f, 12f)
                    lineTo(4f, 5f)
                    close()
                }
            }
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
