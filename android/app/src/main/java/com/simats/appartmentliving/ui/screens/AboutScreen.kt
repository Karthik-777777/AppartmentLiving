package com.simats.appartmentliving.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.appartmentliving.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    isDarkMode: Boolean,
    onBackClick: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.8f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "About Appartment Living", 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Apartment living brings together comfort, community, and convenience in a shared residential environment. It offers residents a modern lifestyle where facilities, security, and services are managed efficiently for everyone’s benefit.",
                color = secondaryTextColor,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            SectionTitle("Shared Amenities", textColor)
            SectionBody("Access to facilities like gyms, parks, clubhouses, swimming pools, and more.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("24/7 Security", textColor)
            SectionBody("Gated access, CCTV monitoring, and visitor tracking for safety.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Maintenance Services", textColor)
            SectionBody("Regular cleaning, repairs, and upkeep handled by management.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("Community Living", textColor)
            SectionBody("Opportunities to interact with neighbors and participate in events.", secondaryTextColor)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "📱 Why Use SocietyDesk for Apartment Living?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "SocietyDesk simplifies everyday living by digitizing apartment management:",
                color = secondaryTextColor,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("🛠️ Raise Complaints Easily", textColor)
            SectionBody("Report maintenance issues anytime.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("📢 Stay Updated", textColor)
            SectionBody("Receive announcements and notices instantly.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("💰 Track Payments", textColor)
            SectionBody("View and pay maintenance bills online.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("🚪 Visitor Management", textColor)
            SectionBody("Approve and monitor guests securely.", secondaryTextColor)

            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("📊 Transparency", textColor)
            SectionBody("Access records, reports, and updates anytime.", secondaryTextColor)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "🤝 Benefits of Apartment Living",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            BulletPoint("Better security and safety", secondaryTextColor)
            BulletPoint("Cost-effective shared resources", secondaryTextColor)
            BulletPoint("Strong community bonding", secondaryTextColor)
            BulletPoint("Organized maintenance and services", secondaryTextColor)
            BulletPoint("Convenient urban lifestyle", secondaryTextColor)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "⚠️ Responsibilities of Residents",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Spacer(modifier = Modifier.height(16.dp))
            BulletPoint("Follow community rules and guidelines", secondaryTextColor)
            BulletPoint("Pay maintenance fees on time", secondaryTextColor)
            BulletPoint("Respect neighbors and shared spaces", secondaryTextColor)
            BulletPoint("Report issues responsibly", secondaryTextColor)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Apartment living is more than just a place to stay—it’s a connected lifestyle. With SocietyDesk, managing daily activities becomes simple, transparent, and efficient, enhancing the overall living experience.",
                color = secondaryTextColor,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SectionTitle(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
fun SectionBody(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = text,
        fontSize = 16.sp,
        color = color,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun BulletPoint(text: String, color: androidx.compose.ui.graphics.Color) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text("•", color = color, fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
        Text(text = text, color = color, fontSize = 16.sp)
    }
}
