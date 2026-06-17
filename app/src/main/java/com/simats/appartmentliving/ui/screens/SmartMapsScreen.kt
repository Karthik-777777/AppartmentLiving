package com.simats.appartmentliving.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.appartmentliving.ui.theme.PrimaryBlue
import com.simats.appartmentliving.ui.viewmodels.MapsViewModel
import com.simats.appartmentliving.ui.viewmodels.PlaceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMapsScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    val viewModel = remember { MapsViewModel() }
    val places by viewModel.places.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedCategory by remember { mutableStateOf("all") }
    val context = LocalContext.current

    // Back press handler
    BackHandler(onBack = onBack)

    val backgroundColor = if (isDarkMode) Color(0xFF0C0C0E) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val cardColor = if (isDarkMode) Color(0xFF18181C) else Color.White
    val borderColor = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0)
    val chipUnselectedBg = if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9)

    // Animated Live Indicator Pulse
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.2f at 0
                1f at 500
                0.2f at 1000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Maps",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = alpha))
                            )
                            Text(
                                text = "Live Nearby Services",
                                fontSize = 11.sp,
                                color = secondaryTextColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            // Description subtext
            Text(
                text = "Live hospitals, medical pharmacies, supermarkets and emergency services located near the society.",
                fontSize = 14.sp,
                color = secondaryTextColor,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Horizontal Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Chip Helper Composable inline
                val categories = listOf(
                    Triple("all", "All", PrimaryBlue),
                    Triple("hospital", "Hospitals", Color(0xFFEF4444)),
                    Triple("pharmacy", "Pharmacies", Color(0xFF22C55E)),
                    Triple("supermarket", "Supermarkets", Color(0xFF3B82F6))
                )

                categories.forEach { (catId, label, colorVal) ->
                    val isSelected = selectedCategory == catId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colorVal.copy(alpha = 0.1f) else chipUnselectedBg)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) colorVal else borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedCategory = catId }
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) colorVal else textColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Places List
            if (isLoading && places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else {
                val filteredPlaces = places.filter {
                    selectedCategory == "all" || it.type == selectedCategory
                }

                if (filteredPlaces.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No places found in this category.",
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredPlaces) { place ->
                            PlaceCard(
                                place = place,
                                cardColor = cardColor,
                                borderColor = borderColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                isDarkMode = isDarkMode,
                                onDirectionsClick = {
                                    val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${place.lat},${place.lng}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (ex: Exception) {
                                        // Open in web browser fallback
                                        val webIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        context.startActivity(webIntent)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceCard(
    place: PlaceItem,
    cardColor: Color,
    borderColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    isDarkMode: Boolean,
    onDirectionsClick: () -> Unit
) {
    val typeIcon = when (place.type) {
        "hospital" -> "🏥"
        "pharmacy" -> "💊"
        else -> "🛒"
    }

    val typeLabel = when (place.type) {
        "hospital" -> "Hospital"
        "pharmacy" -> "Pharmacy"
        else -> "Supermarket"
    }

    val typeColor = when (place.type) {
        "hospital" -> Color(0xFFEF4444)
        "pharmacy" -> Color(0xFF22C55E)
        else -> Color(0xFF3B82F6)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Place Name
                    Text(
                        text = place.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Category Badge Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$typeIcon $typeLabel",
                            fontSize = 12.sp,
                            color = typeColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "•",
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = String.format("%.1f", place.rating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = borderColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    // Geodesic distance
                    Text(
                        text = "📍 ${String.format("%.1f", place.distance)} km away",
                        fontSize = 13.sp,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    // Open status
                    Text(
                        text = if (place.isOpen) "● Open Now" else "● Closed",
                        fontSize = 12.sp,
                        color = if (place.isOpen) Color(0xFF22C55E) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Directions Button
                Button(
                    onClick = onDirectionsClick,
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Directions",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
