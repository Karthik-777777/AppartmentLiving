package com.simats.appartmentliving.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.window.Dialog
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.appartmentliving.ui.theme.PrimaryBlue
import androidx.compose.ui.text.font.FontFamily

data class AdminUser(
    val id: Int,
    val name: String,
    val flat: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val date: String,
    val email: String,
    val phone: String,
    val avatar: String
)

data class AdminComplaint(
    val id: Int,
    val title: String,
    val user: String,
    val flat: String,
    val category: String,
    val status: String, // "Pending", "In Progress", "Resolved"
    val priority: String, // "High", "Medium", "Low"
    val description: String,
    val phone: String,
    val image: String,
    val adminNote: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedTab by remember { mutableStateOf("Home") }
    var currentSubScreen by remember { mutableStateOf("list") } // "list", "detail"
    var selectedUserId by remember { mutableStateOf<Int?>(null) }
    var selectedComplaintId by remember { mutableStateOf<Int?>(null) }
    var showDecisionDialog by remember { mutableStateOf(false) }
    var decisionType by remember { mutableStateOf("") } // "Approve" or "Reject"
    var selectedAadhaarSide by remember { mutableStateOf("Front") }
    var rejectNoteText by remember { mutableStateOf("Your Aadhaar back side image is blurry. Please re-upload a clearer image and we'll review again.") }
    var selectedRejectReason by remember { mutableStateOf("Incomplete Aadhaar") }
    var showStatusSheet by remember { mutableStateOf(false) }
    var newSelectedStatus by remember { mutableStateOf("") }
    var resolutionNoteText by remember { mutableStateOf("Pipe joint was loose and has been re-sealed. No more leakage observed.") }
    var assignedPlumber by remember { mutableStateOf("Ramesh (Plumber)") }
    var residentResponseText by remember { mutableStateOf("Plumber dispatched. Will reach by 2 PM.") }
    var internalNoteText by remember { mutableStateOf("Check unit C-504 above too - might be related.") }

    // Phase 11 Payments State Variables
    var billingResidentName by remember { mutableStateOf("Rahul Sharma") }
    var billingResidentId by remember { mutableStateOf("RES-0042") }
    var billingResidentApt by remember { mutableStateOf("C-404") }
    val billingItemsList = remember {
        mutableStateListOf(
            Triple("Monthly Rent", 18000, true),
            Triple("Maintenance", 3500, true),
            Triple("Parking (2 slots)", 800, false),
            Triple("Water Charges", 500, false),
            Triple("Common Area Electricity", 350, false)
        )
    }
    var billingMonthName by remember { mutableStateOf("May 2026") }
    var billingDueDateVal by remember { mutableStateOf("May 28, 2026") }
    var billingNotesVal by remember { mutableStateOf("Includes 2 reserved parking slots.") }

    // Admin Settings State Variables
    var adminSettingsSubScreen by remember { mutableStateOf("list") }
    var adminName by remember { mutableStateOf("Siddharth Karthik") }
    var adminEmail by remember { mutableStateOf("siddharth@apartmentliving.com") }
    var adminPhone by remember { mutableStateOf("+91 99999 88888") }
    var adminPin by remember { mutableStateOf("1234") }
    
    var notifyNewComplaints by remember { mutableStateOf(true) }
    var notifyNewRegistrations by remember { mutableStateOf(true) }
    var adminBiometrics by remember { mutableStateOf(false) }
    
    var baseMaintenanceRate by remember { mutableStateOf("3500") }
    var parkingRate by remember { mutableStateOf("800") }
    var waterChargesRate by remember { mutableStateOf("500") }


    val usersList = remember { mutableStateListOf<AdminUser>() }
    val complaintsList = remember { mutableStateListOf<AdminComplaint>() }
    val paymentsList = remember { mutableStateListOf<com.simats.appartmentliving.data.PaymentDto>() }

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { com.simats.appartmentliving.data.ComplaintsRepository(coroutineScope) }
    var isAdminComplaintsLoading by remember { mutableStateOf(false) }
    var adminComplaintsError by remember { mutableStateOf<String?>(null) }
    var isAdminResidentsLoading by remember { mutableStateOf(false) }
    var adminResidentsError by remember { mutableStateOf<String?>(null) }
    var isAdminPaymentsLoading by remember { mutableStateOf(false) }
    var adminPaymentsError by remember { mutableStateOf<String?>(null) }
    var analyticsData by remember { mutableStateOf<com.simats.appartmentliving.data.AnalyticsResponse?>(null) }
    var isAdminAnalyticsLoading by remember { mutableStateOf(false) }
    var adminAnalyticsError by remember { mutableStateOf<String?>(null) }

    // Notice ViewModel and state
    val noticeViewModel = remember { com.simats.appartmentliving.ui.viewmodels.NoticeViewModel() }
    val noticesList by noticeViewModel.notices.collectAsState(initial = emptyList())
    val isNoticesLoading by noticeViewModel.isLoading.collectAsState(initial = false)
    val noticesError by noticeViewModel.error.collectAsState(initial = null)
    val createNoticeStatus by noticeViewModel.createStatus.collectAsState(initial = com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Idle)
    val deleteNoticeStatus by noticeViewModel.deleteStatus.collectAsState(initial = com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Idle)

    LaunchedEffect(adminSettingsSubScreen) {
        if (adminSettingsSubScreen == "manage_notices") {
            noticeViewModel.fetchNotices()
        }
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Complaints" || selectedTab == "Home" || selectedTab == "Analytics") {
            repository.syncAdminComplaints(
                localList = complaintsList,
                onStart = {
                    isAdminComplaintsLoading = true
                    adminComplaintsError = null
                },
                onComplete = { error ->
                    isAdminComplaintsLoading = false
                    if (error != null) {
                        adminComplaintsError = error.message ?: "Failed to sync complaints"
                    }
                }
            )
        }
        if (selectedTab == "Users" || selectedTab == "Home" || selectedTab == "Payments" || selectedTab == "Analytics") {
            repository.syncResidents(
                localList = usersList,
                onStart = {
                    isAdminResidentsLoading = true
                    adminResidentsError = null
                },
                onComplete = { error ->
                    isAdminResidentsLoading = false
                    if (error != null) {
                        adminResidentsError = error.message ?: "Failed to sync residents"
                    }
                }
            )
        }
        if (selectedTab == "Payments" || selectedTab == "Home" || selectedTab == "Analytics") {
            repository.syncPayments(
                residentId = null,
                localList = paymentsList,
                onStart = {
                    isAdminPaymentsLoading = true
                    adminPaymentsError = null
                },
                onComplete = { error ->
                    isAdminPaymentsLoading = false
                    if (error != null) {
                        adminPaymentsError = error.message ?: "Failed to sync payments"
                    }
                }
            )
        }
        if (selectedTab == "Analytics") {
            repository.syncAnalytics(
                onStart = {
                    isAdminAnalyticsLoading = true
                    adminAnalyticsError = null
                },
                onComplete = { data, error ->
                    isAdminAnalyticsLoading = false
                    if (error != null) {
                        adminAnalyticsError = error.message ?: "Failed to sync analytics"
                    } else {
                        analyticsData = data
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            repository.syncAdminComplaints(
                localList = complaintsList,
                onStart = {},
                onComplete = {}
            )
            repository.syncResidents(
                localList = usersList,
                onStart = {},
                onComplete = {}
            )
            repository.syncPayments(
                residentId = null,
                localList = paymentsList,
                onStart = {},
                onComplete = {}
            )
            repository.syncAnalytics(
                onStart = {},
                onComplete = { data, _ ->
                    if (data != null) {
                        analyticsData = data
                    }
                }
            )
            kotlinx.coroutines.delay(10000)
        }
    }

    // Intercept Back Press to navigate internally
    BackHandler(enabled = selectedTab != "Home" || currentSubScreen != "list" || adminSettingsSubScreen != "list") {
        if (adminSettingsSubScreen != "list") {
            adminSettingsSubScreen = "list"
        } else {
            when (currentSubScreen) {
                "approve_success", "status_updated" -> {
                    currentSubScreen = "list"
                }
                "reject_user", "aadhaar_preview", "profile_image", "change_status" -> {
                    currentSubScreen = "detail"
                }
                "bill_preview" -> {
                    currentSubScreen = "generate_bill"
                }
                "generate_bill" -> {
                    currentSubScreen = "select_resident"
                }
                "select_resident" -> {
                    currentSubScreen = "list"
                }
                "list" -> {
                    selectedTab = "Home"
                }
                else -> {
                    currentSubScreen = "list"
                }
            }
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val cardColor = if (isDarkMode) Color(0xFF1C1C1E) else Color(0xFFF1F5F9)
    val inputBgColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFE2E8F0)
    val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = backgroundColor,
        bottomBar = {
            if ((currentSubScreen == "list" || currentSubScreen == "select_resident" || currentSubScreen == "dashboard") && adminSettingsSubScreen == "list") {
                // Custom Bottom Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(cardColor)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AdminBottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = selectedTab == "Home", onClick = { selectedTab = "Home"; currentSubScreen = "list" }, modifier = Modifier.weight(1f))
                    AdminBottomNavItem(icon = Icons.Outlined.BarChart, label = "Analytics", isSelected = selectedTab == "Analytics", onClick = { selectedTab = "Analytics"; currentSubScreen = "list" }, modifier = Modifier.weight(1f))
                    AdminBottomNavItem(icon = Icons.Outlined.People, label = "Users", isSelected = selectedTab == "Users", onClick = { selectedTab = "Users"; currentSubScreen = "list" }, modifier = Modifier.weight(1f))
                    AdminBottomNavItem(icon = Icons.Outlined.Assignment, label = "Complaints", isSelected = selectedTab == "Complaints", onClick = { selectedTab = "Complaints"; currentSubScreen = "list" }, modifier = Modifier.weight(1f))
                    AdminBottomNavItem(icon = Icons.Outlined.CreditCard, label = "Payments", isSelected = selectedTab == "Payments", onClick = { selectedTab = "Payments"; currentSubScreen = "dashboard" }, modifier = Modifier.weight(1f))
                    AdminBottomNavItem(icon = Icons.Outlined.Settings, label = "Settings", isSelected = selectedTab == "Settings", onClick = { selectedTab = "Settings"; currentSubScreen = "list" }, modifier = Modifier.weight(1f))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            // Sub-screen rendering
            if (selectedTab == "Home") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile & Notification Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .background(cardColor)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar Box
                            val adminInitials = adminName.split(" ").filter { it.isNotEmpty() }.map { it.first().uppercase() }.joinToString("").take(2)
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (adminInitials.isNotEmpty()) adminInitials else "SK",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Welcome,",
                                        color = secondaryTextColor,
                                        fontSize = 12.sp
                                    )
                                    
                                    // ADMIN Shield badge
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(PrimaryBlue.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Shield,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "ADMIN",
                                            color = PrimaryBlue,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                
                                Text(
                                    text = adminName,
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Action buttons row (Refresh + Notification Bell)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Refresh Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(cardColor)
                                    .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                                    .clickable {
                                        repository.syncAdminComplaints(
                                            localList = complaintsList,
                                            onStart = {
                                                isAdminComplaintsLoading = true
                                                adminComplaintsError = null
                                            },
                                            onComplete = { error ->
                                                isAdminComplaintsLoading = false
                                                if (error != null) {
                                                    adminComplaintsError = error.message
                                                }
                                            }
                                        )
                                        repository.syncResidents(
                                            localList = usersList,
                                            onStart = {
                                                isAdminResidentsLoading = true
                                                adminResidentsError = null
                                            },
                                            onComplete = { error ->
                                                isAdminResidentsLoading = false
                                                if (error != null) {
                                                    adminResidentsError = error.message
                                                }
                                            }
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAdminComplaintsLoading || isAdminResidentsLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = PrimaryBlue
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.Refresh,
                                        contentDescription = "Refresh",
                                        tint = textColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Notification Bell
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(cardColor)
                                    .border(1.dp, textColor.copy(alpha = 0.1f), CircleShape)
                                    .clickable { /* Notification Click */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    tint = textColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                // Red indicator dot
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 4.dp)
                                )
                            }
                        }
                    }
                    
                    // 2x2 Stats Grid
                    val approvedCount = usersList.count { it.status == "Approved" }
                    val pendingCount = usersList.count { it.status == "Pending" }
                    val openComplaintsCount = complaintsList.count { it.status != "Resolved" }
                    val resolvedTodayCount = complaintsList.count { it.status == "Resolved" }
                    val highPriorityCount = complaintsList.count { it.priority == "High" && it.status != "Resolved" }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminDashboardCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.People,
                                iconColor = PrimaryBlue,
                                title = "$approvedCount",
                                subtitle = "Total Residents",
                                indicator = "+${if (approvedCount > 0) 1 else 0}",
                                isPositive = true,
                                cardColor = cardColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor
                            )
                            AdminDashboardCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.AssignmentInd,
                                iconColor = Color(0xFFF59E0B),
                                title = "$pendingCount",
                                subtitle = "Pending Approvals",
                                isPositive = true,
                                cardColor = cardColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminDashboardCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.ErrorOutline,
                                iconColor = Color(0xFFEF4444),
                                title = "$openComplaintsCount",
                                subtitle = "Open Complaints",
                                indicator = "",
                                isPositive = false,
                                cardColor = cardColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor
                            )
                            AdminDashboardCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Outlined.CheckCircle,
                                iconColor = Color(0xFF10B981),
                                title = "$resolvedTodayCount",
                                subtitle = "Resolved Today",
                                indicator = "",
                                isPositive = true,
                                cardColor = cardColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor
                            )
                        }
                    }
                    
                    // Urgent Warning Card Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "URGENT · $highPriorityCount high-priority",
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Items requiring your immediate attention.",
                                color = secondaryTextColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Urgent Complaints List
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Urgent Complaints",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // We filter for High priority complaints
                        val urgentComplaints = complaintsList.filter { it.priority == "High" && it.status != "Resolved" }
                        urgentComplaints.forEach { complaint ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardColor)
                                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedComplaintId = complaint.id
                                        selectedTab = "Complaints"
                                        currentSubScreen = "detail"
                                    }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "#AL-2024-0${complaint.id}",
                                        color = PrimaryBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AdminStatusBadge(status = complaint.status)
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = "${complaint.title} · ${complaint.flat}",
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(textColor.copy(alpha = 0.05f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(complaint.category, color = secondaryTextColor, fontSize = 11.sp)
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(complaint.priority, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Text(
                                        text = if (complaint.id == 91) "2h ago" else "Yesterday",
                                        color = secondaryTextColor,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedTab = "Users"; currentSubScreen = "list" },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = textColor
                            )
                        ) {
                            Text("Approve Users", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        
                        Button(
                            onClick = { selectedTab = "Complaints"; currentSubScreen = "list" },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Manage Complaints", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
            
            else if (selectedTab == "Payments") {
                when (currentSubScreen) {
                    "dashboard" -> {
                        AdminPaymentsDashboardScreen(
                            isDarkMode = isDarkMode,
                            paymentsList = paymentsList,
                            isLoading = isAdminPaymentsLoading,
                            errorMessage = adminPaymentsError,
                            repository = repository,
                            onBack = { selectedTab = "Home"; currentSubScreen = "list" },
                            onManualBill = { currentSubScreen = "select_resident" },
                            onRefresh = {
                                repository.syncPayments(
                                    residentId = null,
                                    localList = paymentsList,
                                    onStart = {
                                        isAdminPaymentsLoading = true
                                        adminPaymentsError = null
                                    },
                                    onComplete = { error ->
                                        isAdminPaymentsLoading = false
                                        if (error != null) {
                                            adminPaymentsError = error.message
                                        }
                                    }
                                )
                            }
                        )
                    }
                    "select_resident" -> {
                        PaymentSelectResidentScreen(
                            isDarkMode = isDarkMode,
                            usersList = usersList,
                            onBack = { currentSubScreen = "dashboard" },
                            onContinue = { name, id, apt ->
                                billingResidentName = name
                                billingResidentId = id
                                billingResidentApt = apt
                                currentSubScreen = "generate_bill"
                            }
                        )
                    }
                    "generate_bill" -> {
                        PaymentGenerateBillScreen(
                            isDarkMode = isDarkMode,
                            residentName = billingResidentName,
                            residentId = billingResidentId,
                            residentApt = billingResidentApt,
                            initialItems = billingItemsList,
                            initialMonth = billingMonthName,
                            initialDueDate = billingDueDateVal,
                            initialNotes = billingNotesVal,
                            onBack = { currentSubScreen = "select_resident" },
                            onPreview = { items, month, dueDate, notes ->
                                billingItemsList.clear()
                                billingItemsList.addAll(items)
                                billingMonthName = month
                                billingDueDateVal = dueDate
                                billingNotesVal = notes
                                currentSubScreen = "bill_preview"
                            }
                        )
                    }
                    "bill_preview" -> {
                        PaymentBillPreviewScreen(
                            isDarkMode = isDarkMode,
                            residentName = billingResidentName,
                            residentId = billingResidentId,
                            residentApt = billingResidentApt,
                            items = billingItemsList,
                            month = billingMonthName,
                            dueDate = billingDueDateVal,
                            notes = billingNotesVal,
                            onBack = { currentSubScreen = "generate_bill" },
                            onSendBill = {
                                val itemsMap = billingItemsList.filter { it.third }.associate { it.first to it.second }
                                var rent = 0
                                var maintenance = 0
                                var parkingFee = 0
                                var waterBill = 0
                                var electricityBill = 0
                                var penalty = 0
                                var otherCharges = 0

                                itemsMap.forEach { (label, amt) ->
                                    val lower = label.lowercase()
                                    when {
                                        lower.contains("rent") -> rent += amt
                                        lower.contains("maintenance") -> maintenance += amt
                                        lower.contains("parking") -> parkingFee += amt
                                        lower.contains("water") -> waterBill += amt
                                        lower.contains("electricity") || lower.contains("power") -> electricityBill += amt
                                        lower.contains("penalty") || lower.contains("fine") -> penalty += amt
                                        else -> otherCharges += amt
                                    }
                                }

                                val matchedResident = com.simats.appartmentliving.data.ComplaintsRepository.rawResidentDtos.find {
                                    kotlin.math.abs((it.id ?: "").hashCode()).toString() == billingResidentId.removePrefix("RES-")
                                }
                                val realResidentId = matchedResident?.residentId ?: billingResidentId

                                val paymentDto = com.simats.appartmentliving.data.PaymentDto(
                                    residentName = billingResidentName,
                                    residentId = realResidentId,
                                    flatNumber = billingResidentApt,
                                    month = billingMonthName,
                                    totalAmount = itemsMap.values.sum(),
                                    rent = rent,
                                    maintenance = maintenance,
                                    parkingFee = parkingFee,
                                    waterBill = waterBill,
                                    electricityBill = electricityBill,
                                    penalty = penalty,
                                    otherCharges = otherCharges,
                                    status = "Unpaid"
                                )

                                repository.generateBill(paymentDto) { error ->
                                    if (error != null) {
                                        android.widget.Toast.makeText(context, "Failed to send bill: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Bill Sent to $billingResidentName!", android.widget.Toast.LENGTH_LONG).show()
                                        repository.syncPayments(
                                            residentId = null,
                                            localList = paymentsList,
                                            onStart = {},
                                            onComplete = {}
                                        )
                                    }
                                }
                                currentSubScreen = "dashboard"
                            }
                        )
                    }
                    else -> {
                        currentSubScreen = "dashboard"
                    }
                }
            }
            
            else if (selectedTab == "Analytics") {
                when (currentSubScreen) {
                    "chart_full" -> {
                        S47ChartFull(
                            isDarkMode = isDarkMode,
                            analyticsData = analyticsData,
                            onBack = { currentSubScreen = "list" }
                        )
                    }
                    else -> {
                        S46AnalyticsFull(
                            isDarkMode = isDarkMode,
                            analyticsData = analyticsData,
                            isLoading = isAdminAnalyticsLoading,
                            errorMessage = adminAnalyticsError,
                            onRefresh = {
                                repository.syncAnalytics(
                                    onStart = {
                                        isAdminAnalyticsLoading = true
                                        adminAnalyticsError = null
                                    },
                                    onComplete = { data, error ->
                                        isAdminAnalyticsLoading = false
                                        if (error != null) {
                                            adminAnalyticsError = error.message ?: "Failed to sync analytics"
                                        } else {
                                            analyticsData = data
                                        }
                                    }
                                )
                            },
                            onExpandChart = { currentSubScreen = "chart_full" }
                        )
                    }
                }
            }
            
            else if (selectedTab == "Users") {
                var searchQuery by remember { mutableStateOf("") }
                var activeFilter by remember { mutableStateOf("Pending") }
                val filters = listOf("All", "Pending", "Approved", "Rejected")

                when (currentSubScreen) {
                    "list" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Manage Users",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Icon(
                                    imageVector = Icons.Outlined.FactCheck,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Search Bar
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search by name, email or flat...", color = secondaryTextColor) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = secondaryTextColor) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = cardColor,
                                    unfocusedContainerColor = cardColor,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Filter Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filters.forEach { filter ->
                                    val isSelected = activeFilter == filter
                                    val count = when (filter) {
                                        "All" -> usersList.size
                                        "Pending" -> usersList.count { it.status == "Pending" }
                                        "Approved" -> usersList.count { it.status == "Approved" }
                                        "Rejected" -> usersList.count { it.status == "Rejected" }
                                        else -> 0
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) PrimaryBlue else cardColor)
                                            .clickable { activeFilter = filter }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "$filter ($count)",
                                            color = if (isSelected) Color.White else secondaryTextColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // List
                            val filteredUsers = usersList.filter { user ->
                                val matchesFilter = activeFilter == "All" || user.status == activeFilter
                                val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
                                        user.flat.contains(searchQuery, ignoreCase = true) ||
                                        user.email.contains(searchQuery, ignoreCase = true)
                                matchesFilter && matchesSearch
                            }
                            
                            var isUsersSwipeRefreshing by remember { mutableStateOf(false) }

                            PullToRefreshBox(
                                isRefreshing = isUsersSwipeRefreshing,
                                onRefresh = {
                                    isUsersSwipeRefreshing = true
                                    repository.syncResidents(
                                        localList = usersList,
                                        onStart = {},
                                        onComplete = {
                                            isUsersSwipeRefreshing = false
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (isAdminResidentsLoading && usersList.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = PrimaryBlue)
                                        }
                                    } else if (adminResidentsError != null && usersList.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(adminResidentsError ?: "Failed to sync", color = Color(0xFFEF4444))
                                        }
                                    } else if (filteredUsers.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 32.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                shape = RoundedCornerShape(24.dp),
                                                colors = CardDefaults.cardColors(containerColor = cardColor),
                                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f))
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(32.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(64.dp)
                                                            .clip(CircleShape)
                                                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.FolderOpen,
                                                            contentDescription = null,
                                                            tint = PrimaryBlue,
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                    }
                                                    
                                                    Text(
                                                        text = "No residents found",
                                                        color = textColor,
                                                        fontSize = 18.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    
                                                    Text(
                                                        text = "No applications match your active filters or search terms. Try clearing your search or filters to see more results.",
                                                        color = secondaryTextColor,
                                                        fontSize = 13.sp,
                                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                        lineHeight = 18.sp
                                                    )
                                                    
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    
                                                    Button(
                                                        onClick = {
                                                            searchQuery = ""
                                                            activeFilter = "All"
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = PrimaryBlue,
                                                            contentColor = Color.White
                                                        ),
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(48.dp)
                                                    ) {
                                                        Text(
                                                            text = "Clear All Filters",
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        filteredUsers.forEach { user ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(cardColor)
                                                    .clickable {
                                                        selectedUserId = user.id
                                                        currentSubScreen = "detail"
                                                    }
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Initials Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .background(PrimaryBlue),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = user.avatar,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(user.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                    Text("Flat ${user.flat}", color = secondaryTextColor, fontSize = 13.sp)
                                                }
                                                
                                                Column(horizontalAlignment = Alignment.End) {
                                                    AdminStatusBadge(status = user.status)
                                                    if (user.status == "Pending") {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(top = 4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Outlined.Shield,
                                                                contentDescription = null,
                                                                tint = Color(0xFFF59E0B),
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Needs Review", color = Color(0xFFF59E0B), fontSize = 10.sp)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                    "detail" -> {
                        // User Detail Admin Sub-screen
                        val user = usersList.find { it.id == selectedUserId }
                        if (user != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                // Custom Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { currentSubScreen = "list" }) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                                    }
                                    Text("User Verification", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Pending Warning Stripe Banner
                                    if (user.status == "Pending") {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.1f))
                                                .border(BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ReportProblem,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "VERIFICATION PENDING",
                                                    color = Color(0xFFF59E0B),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Please review Aadhaar details and photo carefully.",
                                                    color = secondaryTextColor,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    
                                    // Profile Header
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(72.dp)
                                                .clip(CircleShape)
                                                .background(PrimaryBlue)
                                                .clickable {
                                                    currentSubScreen = "profile_image"
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(user.avatar, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        Column {
                                            Text(user.name, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            AdminStatusBadge(status = user.status)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Details Card
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(cardColor)
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Outlined.Home, contentDescription = null, tint = secondaryTextColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Flat", color = secondaryTextColor, modifier = Modifier.width(80.dp), fontSize = 14.sp)
                                            Text(user.flat, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Outlined.Phone, contentDescription = null, tint = secondaryTextColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Phone", color = secondaryTextColor, modifier = Modifier.width(80.dp), fontSize = 14.sp)
                                            Text(user.phone, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Outlined.Email, contentDescription = null, tint = secondaryTextColor, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text("Email", color = secondaryTextColor, modifier = Modifier.width(80.dp), fontSize = 14.sp)
                                            Text(user.email, color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Aadhaar Images Preview
                                    Text("Aadhaar Documents", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Simulated Aadhaar Cards
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column {
                                            Text("Front Side", color = secondaryTextColor, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(cardColor)
                                                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        selectedAadhaarSide = "Front"
                                                        currentSubScreen = "aadhaar_preview"
                                                    }
                                                    .padding(16.dp)
                                            ) {
                                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                        Text("UNIQUE IDENTIFICATION AUTHORITY OF INDIA", color = textColor.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                        Icon(imageVector = Icons.Outlined.Fingerprint, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                                                    }
                                                    Column {
                                                        Text(user.name, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                        Text("DOB: 15/08/1995 | Gender: Female", color = secondaryTextColor, fontSize = 11.sp)
                                                    }
                                                    Text("XXXX XXXX 1234", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                                }
                                            }
                                        }
                                        
                                        Column {
                                            Text("Back Side", color = secondaryTextColor, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(cardColor)
                                                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                                    .clickable {
                                                        selectedAadhaarSide = "Back"
                                                        currentSubScreen = "aadhaar_preview"
                                                    }
                                                    .padding(16.dp)
                                            ) {
                                                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Address details & barcode region", color = secondaryTextColor, fontSize = 12.sp)
                                                    Text("Address: Block B - 205, Green View Society, Chennai, Tamil Nadu - 600001", color = textColor, fontSize = 12.sp)
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(24.dp)
                                                            .background(textColor.copy(alpha = 0.1f))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(32.dp))
                                    
                                    // Decision Actions (only if Pending)
                                    if (user.status == "Pending") {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    decisionType = "Reject"
                                                    showDecisionDialog = true
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(50.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.1f))
                                            ) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = Color(0xFFEF4444))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Reject", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                            }
                                            
                                            Button(
                                                onClick = {
                                                    decisionType = "Approve"
                                                    showDecisionDialog = true
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(50.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Approve", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(40.dp))
                                }
                            }
                        }
                    }
                    "approve_success" -> {
                        val user = usersList.find { it.id == selectedUserId }
                        if (user != null) {
                            ApproveUserSuccessScreen(
                                userName = user.name,
                                userEmail = user.email,
                                flatName = user.flat,
                                cardColor = cardColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                onBackToList = {
                                    currentSubScreen = "list"
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    "reject_user" -> {
                        val user = usersList.find { it.id == selectedUserId }
                        if (user != null) {
                            RejectUserScreen(
                                user = user,
                                selectedReason = selectedRejectReason,
                                rejectNote = rejectNoteText,
                                onReasonSelect = { reason, note ->
                                    selectedRejectReason = reason
                                    rejectNoteText = note
                                },
                                onNoteChange = { rejectNoteText = it },
                                cardColor = cardColor,
                                inputBgColor = inputBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                onCancel = {
                                    currentSubScreen = "detail"
                                },
                                onConfirm = {
                                    repository.approveOrRejectResident(
                                        userId = user.id,
                                        status = "Rejected",
                                        localList = usersList,
                                        onComplete = { error ->
                                            if (error == null) {
                                                currentSubScreen = "list"
                                            }
                                        }
                                    )
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    "aadhaar_preview" -> {
                        val user = usersList.find { it.id == selectedUserId }
                        if (user != null) {
                            AadhaarPreviewScreen(
                                user = user,
                                side = selectedAadhaarSide,
                                onBack = {
                                    currentSubScreen = "detail"
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    "profile_image" -> {
                        val user = usersList.find { it.id == selectedUserId }
                        if (user != null) {
                            ProfileImageViewerScreen(
                                user = user,
                                onBack = {
                                    currentSubScreen = "detail"
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    else -> {
                        currentSubScreen = "list"
                    }
                }
            }
            
            else if (selectedTab == "Complaints") {
                var activeFilter by remember { mutableStateOf("All") }
                val filters = listOf("All", "Pending", "In Progress", "Resolved", "High Priority")

                when (currentSubScreen) {
                    "list" -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 24.dp)
                                .padding(top = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "All Complaints",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                IconButton(onClick = { /* Open filters */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Tune,
                                        contentDescription = "Filters",
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Filter Pills
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filters.forEach { filter ->
                                    val isSelected = activeFilter == filter
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (isSelected) PrimaryBlue else cardColor)
                                            .clickable { activeFilter = filter }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = filter,
                                            color = if (isSelected) Color.White else secondaryTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Sort & Active Filters
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Active Filter Pills (High Priority, Plumbing)
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("High Priority", "Plumbing").forEach { filter ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(PrimaryBlue.copy(alpha = 0.1f))
                                                .border(BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)), RoundedCornerShape(16.dp))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = filter,
                                                    color = PrimaryBlue,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Remove",
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Sort indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { /* Toggle sort */ }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.SwapVert,
                                        contentDescription = "Sort",
                                        tint = secondaryTextColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Newest",
                                        color = secondaryTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // List
                            val filteredComplaints = complaintsList.filter { complaint ->
                                when (activeFilter) {
                                    "All" -> true
                                    "High Priority" -> complaint.priority == "High"
                                    else -> complaint.status == activeFilter
                                }
                            }
                            
                            // Item Count Text
                            Text(
                                text = "Showing ${filteredComplaints.size} of ${complaintsList.size} complaints",
                                color = secondaryTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            var isSwipeRefreshing by remember { mutableStateOf(false) }

                            PullToRefreshBox(
                                isRefreshing = isSwipeRefreshing,
                                onRefresh = {
                                    isSwipeRefreshing = true
                                    repository.syncAdminComplaints(
                                        localList = complaintsList,
                                        onStart = {},
                                        onComplete = {
                                            isSwipeRefreshing = false
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (isAdminComplaintsLoading && complaintsList.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = PrimaryBlue)
                                        }
                                    } else if (adminComplaintsError != null && complaintsList.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(adminComplaintsError ?: "Failed to sync", color = Color(0xFFEF4444))
                                        }
                                    } else if (filteredComplaints.isEmpty()) {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(200.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("No complaints found", color = secondaryTextColor)
                                        }
                                    } else {
                                        filteredComplaints.forEach { complaint ->
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(cardColor)
                                                    .clickable {
                                                        selectedComplaintId = complaint.id
                                                        currentSubScreen = "detail"
                                                    }
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Text(
                                                        text = complaint.title,
                                                        color = textColor,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    AdminStatusBadge(status = complaint.status)
                                                }
                                                
                                                Spacer(modifier = Modifier.height(12.dp))
                                                
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(complaint.user, color = textColor.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                                        Text(" • ", color = secondaryTextColor, fontSize = 13.sp)
                                                        Text(complaint.flat, color = secondaryTextColor, fontSize = 13.sp)
                                                    }
                                                    
                                                    // Priority Badge
                                                    val priorityBg = when (complaint.priority) {
                                                        "High" -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                                        "Medium" -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                                                        else -> Color(0xFF10B981).copy(alpha = 0.1f)
                                                    }
                                                    val priorityColor = when (complaint.priority) {
                                                        "High" -> Color(0xFFEF4444)
                                                        "Medium" -> Color(0xFFF59E0B)
                                                        else -> Color(0xFF10B981)
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(priorityBg)
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(complaint.priority, color = priorityColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                    "detail" -> {
                        val complaint = complaintsList.find { it.id == selectedComplaintId }
                        if (complaint != null) {
                            ComplaintDetailAdminScreen(
                                complaint = complaint,
                                residentResponse = residentResponseText,
                                onResidentResponseChange = { residentResponseText = it },
                                internalNote = internalNoteText,
                                onInternalNoteChange = { internalNoteText = it },
                                onSendResponse = {
                                    repository.updateComplaintStatusAndNote(
                                        complaintId = complaint.id,
                                        status = complaint.status,
                                        adminNote = residentResponseText,
                                        localAdminList = complaintsList,
                                        onComplete = {}
                                    )
                                },
                                cardColor = cardColor,
                                inputBgColor = inputBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                onChangeStatusClick = {
                                    newSelectedStatus = complaint.status
                                    showStatusSheet = true
                                },
                                onDeleteClick = {
                                    repository.deleteComplaint(
                                        complaintId = complaint.id,
                                        localAdminList = complaintsList,
                                        onComplete = { error ->
                                            if (error == null) {
                                                currentSubScreen = "list"
                                            }
                                        }
                                    )
                                },
                                onBack = {
                                    currentSubScreen = "list"
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    "change_status" -> {
                        val complaint = complaintsList.find { it.id == selectedComplaintId }
                        if (complaint != null) {
                            ChangeStatusScreen(
                                complaintId = complaint.id,
                                currentStatus = complaint.status,
                                newStatus = newSelectedStatus,
                                resolutionNote = resolutionNoteText,
                                onResolutionNoteChange = { resolutionNoteText = it },
                                assignedPlumber = assignedPlumber,
                                cardColor = cardColor,
                                inputBgColor = inputBgColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                onCancel = {
                                    currentSubScreen = "detail"
                                },
                                onConfirm = {
                                    repository.updateComplaintStatusAndNote(
                                        complaintId = complaint.id,
                                        status = newSelectedStatus,
                                        adminNote = resolutionNoteText,
                                        localAdminList = complaintsList,
                                        onComplete = {}
                                    )
                                    currentSubScreen = "status_updated"
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    "status_updated" -> {
                        val complaint = complaintsList.find { it.id == selectedComplaintId }
                        if (complaint != null) {
                            StatusUpdatedScreen(
                                complaintId = complaint.id,
                                status = newSelectedStatus,
                                residentName = complaint.user,
                                residentFlat = complaint.flat,
                                cardColor = cardColor,
                                textColor = textColor,
                                secondaryTextColor = secondaryTextColor,
                                onViewComplaint = {
                                    currentSubScreen = "detail"
                                },
                                onBackToList = {
                                    currentSubScreen = "list"
                                }
                            )
                        } else {
                            currentSubScreen = "list"
                        }
                    }
                    else -> {
                        currentSubScreen = "list"
                    }
                }
            }
            
            else if (selectedTab == "Settings") {
                if (adminSettingsSubScreen == "list") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = "Admin Settings",
                            color = textColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Admin Profile Group
                        Text(
                            text = "ADMIN PROFILE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryTextColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        AdminSettingsMenuItem(
                            icon = Icons.Outlined.Person,
                            text = "Edit Profile details",
                            textColor = textColor,
                            containerColor = cardColor,
                            onClick = {
                                adminSettingsSubScreen = "edit_profile"
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdminSettingsMenuItem(
                            icon = Icons.Outlined.Lock,
                            text = "Change Admin Security PIN",
                            textColor = textColor,
                            containerColor = cardColor,
                            onClick = {
                                adminSettingsSubScreen = "change_pin"
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // System Preferences Group
                        Text(
                            text = "SYSTEM PREFERENCES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryTextColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        AdminSettingsToggleItem(
                            icon = Icons.Outlined.Notifications,
                            text = "New Complaint Alerts",
                            checked = notifyNewComplaints,
                            onCheckedChange = { notifyNewComplaints = it },
                            cardColor = cardColor,
                            textColor = textColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdminSettingsToggleItem(
                            icon = Icons.Outlined.People,
                            text = "Registration Requests",
                            checked = notifyNewRegistrations,
                            onCheckedChange = { notifyNewRegistrations = it },
                            cardColor = cardColor,
                            textColor = textColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdminSettingsToggleItem(
                            icon = Icons.Outlined.Fingerprint,
                            text = "Biometric Lockout",
                            checked = adminBiometrics,
                            onCheckedChange = { adminBiometrics = it },
                            cardColor = cardColor,
                            textColor = textColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdminSettingsToggleItem(
                            icon = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                            text = "Dark Mode",
                            checked = isDarkMode,
                            onCheckedChange = { onThemeToggle() },
                            cardColor = cardColor,
                            textColor = textColor
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        // Communication Group
                        Text(
                            text = "COMMUNICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryTextColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        AdminSettingsMenuItem(
                            icon = Icons.Outlined.Notifications,
                            text = "Manage Notices & Announcements",
                            textColor = textColor,
                            containerColor = cardColor,
                            onClick = {
                                adminSettingsSubScreen = "manage_notices"
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Maintenance Rules Group
                        Text(
                            text = "MAINTENANCE RULES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryTextColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        AdminSettingsMenuItem(
                            icon = Icons.Outlined.Build,
                            text = "Set Base Maintenance Rates",
                            textColor = textColor,
                            containerColor = cardColor,
                            onClick = {
                                adminSettingsSubScreen = "maintenance_rates"
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Logout Button
                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text(
                                text = "Logout",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else if (adminSettingsSubScreen == "edit_profile") {
                    var editName by remember { mutableStateOf(adminName) }
                    var editEmail by remember { mutableStateOf(adminEmail) }
                    var editPhone by remember { mutableStateOf(adminPhone) }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { adminSettingsSubScreen = "list" }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Profile",
                                color = textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Full Name", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Email Address", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Phone Number", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                adminName = editName
                                adminEmail = editEmail
                                adminPhone = editPhone
                                adminSettingsSubScreen = "list"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else if (adminSettingsSubScreen == "change_pin") {
                    var currentPinInput by remember { mutableStateOf("") }
                    var newPinInput by remember { mutableStateOf("") }
                    var confirmPinInput by remember { mutableStateOf("") }
                    var errorMessage by remember { mutableStateOf("") }
                    var successMessage by remember { mutableStateOf("") }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { adminSettingsSubScreen = "list" }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Change Security PIN",
                                color = textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        if (successMessage.isNotEmpty()) {
                            Text(
                                text = successMessage,
                                color = Color(0xFF10B981),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        Text("Current PIN", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = { if (it.length <= 4) currentPinInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("New 4-Digit PIN", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4) newPinInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Confirm New PIN", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4) confirmPinInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                if (currentPinInput != adminPin) {
                                    errorMessage = "Current PIN is incorrect."
                                    successMessage = ""
                                } else if (newPinInput.length < 4) {
                                    errorMessage = "New PIN must be 4 digits."
                                    successMessage = ""
                                } else if (newPinInput != confirmPinInput) {
                                    errorMessage = "New PIN and confirmation PIN do not match."
                                    successMessage = ""
                                } else {
                                    adminPin = newPinInput
                                    errorMessage = ""
                                    successMessage = "PIN updated successfully!"
                                    currentPinInput = ""
                                    newPinInput = ""
                                    confirmPinInput = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Update PIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else if (adminSettingsSubScreen == "maintenance_rates") {
                    var editBaseRate by remember { mutableStateOf(baseMaintenanceRate) }
                    var editParkingRate by remember { mutableStateOf(parkingRate) }
                    var editWaterRate by remember { mutableStateOf(waterChargesRate) }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { adminSettingsSubScreen = "list" }) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Set Maintenance Rates",
                                color = textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Base Monthly Maintenance (₹)", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editBaseRate,
                            onValueChange = { editBaseRate = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Parking Slot Rate (₹ / slot)", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editParkingRate,
                            onValueChange = { editParkingRate = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Water & Electricity Rate (₹)", color = secondaryTextColor, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = editWaterRate,
                            onValueChange = { editWaterRate = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.1f),
                                cursorColor = PrimaryBlue,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Button(
                            onClick = {
                                baseMaintenanceRate = editBaseRate
                                parkingRate = editParkingRate
                                waterChargesRate = editWaterRate
                                adminSettingsSubScreen = "list"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Update Rates", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                } else if (adminSettingsSubScreen == "manage_notices") {
                    var showCreateNoticeDialog by remember { mutableStateOf(false) }
                    var newNoticeTitle by remember { mutableStateOf("") }
                    var newNoticeDescription by remember { mutableStateOf("") }
                    var newNoticeCategory by remember { mutableStateOf("General") }
                    var newNoticePriority by remember { mutableStateOf("Normal") }
                    var showPriorityDropdown by remember { mutableStateOf(false) }
                    var showCategoryDropdown by remember { mutableStateOf(false) }
                    
                    var showDeleteConfirmDialog by remember { mutableStateOf<com.simats.appartmentliving.data.NoticeDto?>(null) }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = { adminSettingsSubScreen = "list" }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Manage Notices",
                                    color = textColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { noticeViewModel.fetchNotices() }) {
                                    Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Refresh", tint = textColor)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isNoticesLoading && noticesList.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryBlue)
                                }
                            } else if (noticesError != null && noticesList.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = noticesError ?: "Failed to sync", color = Color(0xFFEF4444))
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { noticeViewModel.fetchNotices() },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                        ) {
                                            Text("Try Again", color = Color.White)
                                        }
                                    }
                                }
                            } else if (noticesList.isEmpty()) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No notices posted yet", color = secondaryTextColor)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    noticesList.forEach { notice ->
                                        NoticeAdminCard(
                                            notice = notice,
                                            isDarkMode = isDarkMode,
                                            textColor = textColor,
                                            secondaryTextColor = secondaryTextColor,
                                            cardColor = cardColor,
                                            borderColor = borderColor,
                                            onDeleteClick = { showDeleteConfirmDialog = notice }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = {
                                    newNoticeTitle = ""
                                    newNoticeDescription = ""
                                    newNoticeCategory = "General"
                                    newNoticePriority = "Normal"
                                    noticeViewModel.resetCreateStatus()
                                    showCreateNoticeDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = Color.White)
                                    Text("Create Notice", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                        
                        if (showCreateNoticeDialog) {
                            AlertDialog(
                                onDismissRequest = { showCreateNoticeDialog = false },
                                title = { Text("Create New Notice", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                                text = {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Title", color = secondaryTextColor, fontSize = 12.sp)
                                        OutlinedTextField(
                                            value = newNoticeTitle,
                                            onValueChange = { newNoticeTitle = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryBlue,
                                                unfocusedBorderColor = borderColor,
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            singleLine = true
                                        )
                                        
                                        Text("Description", color = secondaryTextColor, fontSize = 12.sp)
                                        OutlinedTextField(
                                            value = newNoticeDescription,
                                            onValueChange = { newNoticeDescription = it },
                                            modifier = Modifier.fillMaxWidth().height(100.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = PrimaryBlue,
                                                unfocusedBorderColor = borderColor,
                                                focusedTextColor = textColor,
                                                unfocusedTextColor = textColor
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            maxLines = 4
                                        )
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Category", color = secondaryTextColor, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                                            .clickable { showCategoryDropdown = true }
                                                            .padding(12.dp)
                                                    ) {
                                                        Text(newNoticeCategory, color = textColor, fontSize = 14.sp)
                                                    }
                                                    DropdownMenu(
                                                        expanded = showCategoryDropdown,
                                                        onDismissRequest = { showCategoryDropdown = false }
                                                    ) {
                                                        listOf("General", "Maintenance", "Security", "Event", "Billing").forEach { cat ->
                                                            DropdownMenuItem(
                                                                text = { Text(cat) },
                                                                onClick = {
                                                                    newNoticeCategory = cat
                                                                    showCategoryDropdown = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Priority", color = secondaryTextColor, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                                                            .clickable { showPriorityDropdown = true }
                                                            .padding(12.dp)
                                                    ) {
                                                        Text(newNoticePriority, color = textColor, fontSize = 14.sp)
                                                    }
                                                    DropdownMenu(
                                                        expanded = showPriorityDropdown,
                                                        onDismissRequest = { showPriorityDropdown = false }
                                                    ) {
                                                        listOf("High", "Medium", "Normal").forEach { prio ->
                                                            DropdownMenuItem(
                                                                text = { Text(prio) },
                                                                onClick = {
                                                                    newNoticePriority = prio
                                                                    showPriorityDropdown = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        
                                        if (createNoticeStatus is com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Error) {
                                            Text(
                                                text = (createNoticeStatus as com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Error).message,
                                                color = Color(0xFFEF4444),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (newNoticeTitle.isNotBlank() && newNoticeDescription.isNotBlank()) {
                                                noticeViewModel.createNotice(
                                                    title = newNoticeTitle,
                                                    description = newNoticeDescription,
                                                    category = newNoticeCategory,
                                                    priority = newNoticePriority
                                                )
                                            }
                                        },
                                        enabled = createNoticeStatus != com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Loading,
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                    ) {
                                        if (createNoticeStatus == com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Loading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text("Post", color = Color.White)
                                        }
                                    }
                                    
                                    if (createNoticeStatus == com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Success) {
                                        LaunchedEffect(Unit) {
                                            showCreateNoticeDialog = false
                                            noticeViewModel.resetCreateStatus()
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showCreateNoticeDialog = false }) {
                                        Text("Cancel", color = secondaryTextColor)
                                    }
                                }
                            )
                        }
                        
                        showDeleteConfirmDialog?.let { noticeToDelete ->
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmDialog = null },
                                title = { Text("Delete Notice?", color = textColor, fontWeight = FontWeight.Bold) },
                                text = { Text("Are you sure you want to delete notice \"${noticeToDelete.title}\"?", color = secondaryTextColor) },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            noticeToDelete.id?.let { noticeViewModel.deleteNotice(it) }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        enabled = deleteNoticeStatus != com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Loading
                                    ) {
                                        if (deleteNoticeStatus == com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Loading) {
                                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                        } else {
                                            Text("Delete", color = Color.White)
                                        }
                                    }
                                    
                                    if (deleteNoticeStatus == com.simats.appartmentliving.ui.viewmodels.NoticeOperationResult.Success) {
                                        LaunchedEffect(Unit) {
                                            showDeleteConfirmDialog = null
                                            noticeViewModel.resetDeleteStatus()
                                        }
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirmDialog = null }) {
                                        Text("Cancel", color = secondaryTextColor)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDecisionDialog && selectedUserId != null) {
        val user = usersList.find { it.id == selectedUserId }
        if (user != null) {
            Dialog(onDismissRequest = { showDecisionDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Icon (Caution icon)
                        val iconColor = if (decisionType == "Approve") Color(0xFF10B981) else Color(0xFFEF4444)
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (decisionType == "Approve") Icons.Outlined.CheckCircle else Icons.Outlined.ReportProblem,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Title
                        Text(
                            text = if (decisionType == "Approve") "Confirm Approval" else "Confirm Rejection",
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Subtext
                        Text(
                            text = if (decisionType == "Approve") {
                                "Are you sure you want to approve this resident? They will be granted full access to apartment facilities."
                            } else {
                                "Are you sure you want to reject this registration? The resident will not be able to log in."
                            },
                            color = secondaryTextColor,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )

                        // User Preview Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(textColor.copy(alpha = 0.03f))
                                .border(BorderStroke(1.dp, textColor.copy(alpha = 0.05f)), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mini Initials Avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.avatar,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = user.name,
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Flat ${user.flat}",
                                    color = secondaryTextColor,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel button
                            OutlinedButton(
                                onClick = { showDecisionDialog = false },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }

                            // Confirm action button
                            Button(
                                onClick = {
                                    if (decisionType == "Approve") {
                                        repository.approveOrRejectResident(
                                            userId = user.id,
                                            status = "Approved",
                                            localList = usersList,
                                            onComplete = { error ->
                                                if (error == null) {
                                                    currentSubScreen = "approve_success"
                                                }
                                            }
                                        )
                                        showDecisionDialog = false
                                    } else {
                                        showDecisionDialog = false
                                        selectedRejectReason = "Incomplete Aadhaar"
                                        rejectNoteText = "Your Aadhaar back side image is blurry. Please re-upload a clearer image and we'll review again."
                                        currentSubScreen = "reject_user"
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = iconColor,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (decisionType == "Approve") "Approve" else "Reject",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        if (showStatusSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showStatusSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(cardColor)
                        .clickable(enabled = false) {}
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(textColor.copy(alpha = 0.15f))
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Change Status",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val statuses = listOf("Pending", "Awaiting", "In Progress", "Resolved", "Rejected")
                    statuses.forEach { statusOption ->
                        val isSelected = statusOption == newSelectedStatus
                        val statusDotColor = when (statusOption) {
                            "Resolved" -> Color(0xFF10B981)
                            "In Progress" -> PrimaryBlue
                            "Pending" -> Color(0xFFF59E0B)
                            "Awaiting" -> Color(0xFF6B7280)
                            else -> Color(0xFFEF4444)
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    newSelectedStatus = statusOption
                                    showStatusSheet = false
                                    currentSubScreen = "change_status"
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(statusDotColor)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = statusOption,
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AdminStatusBadge(status: String) {
    val (bg, text) = when (status) {
        "Approved", "Resolved" -> Pair(Color(0xFF10B981).copy(alpha = 0.15f), Color(0xFF10B981))
        "Rejected" -> Pair(Color(0xFFEF4444).copy(alpha = 0.15f), Color(0xFFEF4444))
        "In Progress" -> Pair(PrimaryBlue.copy(alpha = 0.15f), PrimaryBlue)
        else -> Pair(Color(0xFFF59E0B).copy(alpha = 0.15f), Color(0xFFF59E0B))
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(status, color = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminDashboardCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    indicator: String? = null,
    isPositive: Boolean = true,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (indicator != null) {
                val trendColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
                val trendBg = trendColor.copy(alpha = 0.1f)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(trendBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                            contentDescription = null,
                            tint = trendColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = indicator,
                            color = trendColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            color = textColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = subtitle,
            color = secondaryTextColor,
            fontSize = 12.sp
        )
    }
}

@Composable
fun AdminQuickActionCard(
    icon: ImageVector,
    iconColor: Color = PrimaryBlue,
    title: String,
    subtitle: String,
    badge: String? = null,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = secondaryTextColor,
                fontSize = 12.sp
            )
        }
        
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF59E0B))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = badge,
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AdminBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = PrimaryBlue
    val unselectedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
    val color = if (isSelected) selectedColor else unselectedColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
            )
            Spacer(modifier = Modifier.height(4.dp))
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = color,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun LegendItem(
    color: Color,
    label: String,
    count: Int,
    textColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(text = count.toString(), color = secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ApproveUserSuccessScreen(
    userName: String,
    userEmail: String,
    flatName: String,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onBackToList: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp, bottom = 40.dp)
    ) {
        // Success ambient blur background
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large circular success check mark
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "User Approved",
                color = textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "$userName has been approved and added to Greenview Apartments, Block ${flatName.take(1)}.",
                color = secondaryTextColor,
                fontSize = 15.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome email sent card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome email sent",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userEmail,
                        color = secondaryTextColor,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Push notification sent card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Push notification sent",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Apartment Living app",
                        color = secondaryTextColor,
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        
        Button(
            onClick = onBackToList,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Return to List", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun RejectUserScreen(
    user: AdminUser,
    selectedReason: String,
    rejectNote: String,
    onReasonSelect: (String, String) -> Unit,
    onNoteChange: (String) -> Unit,
    cardColor: Color,
    inputBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val reasons = listOf(
        "Incomplete Aadhaar" to "Your Aadhaar back side image is blurry. Please re-upload a clearer image and we'll review again.",
        "Invalid apartment" to "The selected flat is currently not available or unregistered. Please correct your registration details.",
        "Duplicate account" to "This apartment number is already claimed by an active resident. Please check and re-apply.",
        "Photo unclear" to "The profile picture uploaded is dark or blurry. Please upload a clear photo showing your face.",
        "Other" to ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                IconButton(onClick = onCancel, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                }
                Text("Reject Application", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            // Applicant card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user.avatar, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column {
                    Text(user.name, color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Apt ${user.flat}", color = secondaryTextColor, fontSize = 13.sp)
                }
            }
            
            // Warning Alert Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEF4444).copy(alpha = 0.1f))
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "The applicant will receive your reason and can re-apply with corrected details.",
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            
            // Quick Reasons
            Column {
                Text(
                    text = "Quick reason",
                    color = secondaryTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reasons.take(3).forEach { (reason, defaultMsg) ->
                            val isSelected = selectedReason == reason
                            val chipBg = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.2f) else cardColor
                            val chipBorder = if (isSelected) Color(0xFFEF4444) else textColor.copy(alpha = 0.08f)
                            val chipTextColor = if (isSelected) Color(0xFFEF4444) else textColor
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                                    .clickable { onReasonSelect(reason, defaultMsg) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = reason,
                                    color = chipTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        reasons.drop(3).forEach { (reason, defaultMsg) ->
                            val isSelected = selectedReason == reason
                            val chipBg = if (isSelected) Color(0xFFEF4444).copy(alpha = 0.2f) else cardColor
                            val chipBorder = if (isSelected) Color(0xFFEF4444) else textColor.copy(alpha = 0.08f)
                            val chipTextColor = if (isSelected) Color(0xFFEF4444) else textColor
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                                    .clickable { onReasonSelect(reason, defaultMsg) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = reason,
                                    color = chipTextColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Custom Reason Textbox
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text("Rejection note ", color = secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("*", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(inputBgColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        BasicTextField(
                            value = rejectNote,
                            onValueChange = {
                                if (it.length <= 250) {
                                    onNoteChange(it)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = textColor,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${rejectNote.length}/250",
                            color = secondaryTextColor,
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
            
            // Preview Notification Card (Glassmorphic)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor.copy(alpha = 0.8f))
                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "PREVIEW",
                    color = secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                val previewText = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = textColor)) {
                        append("Apartment Living: ")
                    }
                    append("Your application was not approved. Reason: $rejectNote")
                }
                Text(
                    text = previewText,
                    color = textColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        
        // Sticky Footer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Confirm Rejection", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AadhaarPreviewScreen(
    user: AdminUser,
    side: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top overlay bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Aadhaar $side",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { /* download click */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = "Download",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // Aadhaar document card wrapper
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A))
                    )
                )
        ) {
            if (side == "Front") {
                // Front Aadhaar layout simulation
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("UNIQUE IDENTIFICATION AUTHORITY OF INDIA", color = Color.DarkGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Icon(imageVector = Icons.Outlined.Fingerprint, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Photo placeholder
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.LightGray)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(user.name, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("DOB: 15/08/1995", color = Color.Gray, fontSize = 9.sp)
                            Text("Gender: Female", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                    
                    Text(
                        text = "XXXX XXXX 1234",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            } else {
                // Back Aadhaar layout simulation
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Text("Address details & barcode region", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        text = "Address: Block B - 205, Green View Society, Chennai, Tamil Nadu - 600001",
                        color = Color.Black,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.align(Alignment.CenterStart).padding(vertical = 12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .background(Color.DarkGray.copy(alpha = 0.2f))
                            .align(Alignment.BottomCenter)
                    )
                }
            }
            
            // Diagonal Translucent watermark
            Canvas(modifier = Modifier.fillMaxSize()) {
                val paint = Paint().apply {
                    color = PrimaryBlue.copy(alpha = 0.18f).toArgb()
                    textSize = 32.sp.toPx()
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.save()
                drawContext.canvas.nativeCanvas.rotate(-30f, size.width / 2f, size.height / 2f)
                drawContext.canvas.nativeCanvas.drawText("VERIFIED COPY", size.width / 2f, size.height / 2f + 10.dp.toPx(), paint)
                drawContext.canvas.nativeCanvas.restore()
            }
        }
        
        // Zoom footer indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ZoomIn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Pinch to zoom",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun ProfileImageViewerScreen(
    user: AdminUser,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top overlay bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.8f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { /* share click */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { /* download click */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        
        // Centered large circular initials avatar
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(256.dp)
                .clip(CircleShape)
                .background(PrimaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.avatar,
                color = Color.White,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Bottom details overlay with name and block info
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(bottom = 60.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = user.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Apartment ${user.flat} · Greenview Block ${user.flat.take(1)}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ComplaintDetailAdminScreen(
    complaint: AdminComplaint,
    residentResponse: String,
    onResidentResponseChange: (String) -> Unit,
    internalNote: String,
    onInternalNoteChange: (String) -> Unit,
    onSendResponse: () -> Unit,
    cardColor: Color,
    inputBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onChangeStatusClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                    Text("Complaint #00${complaint.id}", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
            }
            
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Build,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(complaint.category, color = secondaryTextColor, fontSize = 12.sp)
                        }
                        
                        AdminStatusBadge(status = complaint.status)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(complaint.title, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = complaint.user.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(complaint.user, color = textColor, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("·", color = secondaryTextColor, fontSize = 13.sp)
                        Text(complaint.flat, color = secondaryTextColor, fontSize = 13.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = complaint.description,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Assignee Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ASSIGNED TO", color = secondaryTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Change",
                            color = PrimaryBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { /* Assignee Selector */ }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RM", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Column {
                            Text("Ramesh (Plumber)", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Assigned 1h ago", color = secondaryTextColor, fontSize = 11.sp)
                        }
                    }
                }
            }
            
            // Respond Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("RESPOND TO RESIDENT", color = secondaryTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(inputBgColor)
                            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = residentResponse,
                            onValueChange = onResidentResponseChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = 14.sp)
                        )
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onSendResponse,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(imageVector = Icons.Outlined.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Internal Notes Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF59E0B).copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Text("INTERNAL NOTES · NOT VISIBLE TO RESIDENT", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        BasicTextField(
                            value = internalNote,
                            onValueChange = onInternalNoteChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = 12.sp)
                        )
                    }
                }
            }
        }
        
        // Bottom sticky button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            Button(
                onClick = onChangeStatusClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(imageVector = Icons.Outlined.Sync, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Change Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ChangeStatusScreen(
    complaintId: Int,
    currentStatus: String,
    newStatus: String,
    resolutionNote: String,
    onResolutionNoteChange: (String) -> Unit,
    assignedPlumber: String,
    cardColor: Color,
    inputBgColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel, modifier = Modifier.offset(x = (-12).dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                }
                Text("Update Status", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            // Status transition card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STATUS CHANGE", color = secondaryTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AdminStatusBadge(status = currentStatus)
                            Text("Current", color = secondaryTextColor, fontSize = 11.sp)
                        }
                        
                        Icon(
                            imageVector = Icons.Outlined.TrendingUp, // standard indicator arrow
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            AdminStatusBadge(status = newStatus)
                            Text("New", color = PrimaryBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Required note
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row {
                    Text("Resolution note ", color = secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("*", color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(inputBgColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = resolutionNote,
                        onValueChange = onResolutionNoteChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = textColor, fontSize = 14.sp)
                    )
                }
            }
            
            // Assignee selector (Static dropdown representation)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Assignee", color = secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(inputBgColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable { /* click select */ }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RM", color = textColor, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Text(assignedPlumber, color = textColor, fontSize = 14.sp)
                    }
                    Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null, tint = secondaryTextColor)
                }
            }
            
            // Date Picker (Static representation)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Resolved on", color = secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(inputBgColor)
                        .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable { /* click select */ }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        Text("May 21, 2026 · 2:45 PM", color = textColor, fontSize = 14.sp)
                    }
                    Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null, tint = secondaryTextColor)
                }
            }
            
            // Notification Preview (Resident Will See)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor.copy(alpha = 0.8f))
                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "RESIDENT WILL SEE",
                    color = secondaryTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "✓ Your complaint #AL-2024-00$complaintId has been marked as $newStatus.",
                    color = textColor,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
        }
        
        // Footer Buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Confirm Status Change", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusUpdatedScreen(
    complaintId: Int,
    status: String,
    residentName: String,
    residentFlat: String,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    onViewComplaint: () -> Unit,
    onBackToList: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp, bottom = 40.dp)
    ) {
        // Success ambient blur background
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Status badge pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                    Text(status, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Status Updated",
                color = textColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Complaint #AL-2024-00$complaintId has been marked as $status.",
                color = secondaryTextColor,
                fontSize = 15.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.widthIn(max = 280.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Notification sent card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor)
                    .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Resident will be notified",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$residentName · $residentFlat",
                        color = secondaryTextColor,
                        fontSize = 11.sp
                    )
                }
            }
        }
        
        // Action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onViewComplaint,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("View Complaint", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            OutlinedButton(
                onClick = onBackToList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
            ) {
                Text("Back to List", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ==========================================
// PHASE 10 ADVANCED: ADMIN ANALYTICS SCREENS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun S46AnalyticsFull(
    isDarkMode: Boolean,
    analyticsData: com.simats.appartmentliving.data.AnalyticsResponse?,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onExpandChart: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = textColor)
            }
        }

        if (isLoading && analyticsData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (errorMessage != null && analyticsData == null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = errorMessage, color = Color(0xFFEF4444), fontSize = 16.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Button(onClick = onRefresh, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                        Text("Retry", color = Color.White)
                    }
                }
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Period chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Today", "Week", "Month", "Custom").forEachIndexed { index, period ->
                            val isSelected = index == 1 // "Week" selected
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) PrimaryBlue else cardColor)
                                    .border(1.dp, if (isSelected) PrimaryBlue else dividerColor, RoundedCornerShape(16.dp))
                                    .clickable { /* simulated */ }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = period,
                                    color = if (isSelected) Color.White else secondaryTextColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // 4 KPI Grid
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Total Complaints KPI
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(textColor.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.Build, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "${analyticsData?.complaints?.total ?: 0}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text("Total Complaints", fontSize = 13.sp, color = secondaryTextColor)
                                }
                            }

                            // Resolved Complaints KPI
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(textColor.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "${analyticsData?.complaints?.resolved ?: 0}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text("Resolved", fontSize = 13.sp, color = secondaryTextColor)
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Avg Resolution Time KPI
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(textColor.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.Schedule, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    val avgHours = analyticsData?.complaints?.averageResolutionTime ?: 0.0
                                    val timeLabel = if (avgHours >= 24.0) {
                                        String.format(java.util.Locale.US, "%.1fd", avgHours / 24.0)
                                    } else {
                                        String.format(java.util.Locale.US, "%.1fh", avgHours)
                                    }
                                    Text(
                                        text = timeLabel,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text("Avg. Resolution", fontSize = 13.sp, color = secondaryTextColor)
                                }
                            }

                            // Active Residents KPI
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(textColor.copy(alpha = 0.05f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Outlined.People, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "${analyticsData?.residents?.approved ?: 0}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text("Active Residents", fontSize = 13.sp, color = secondaryTextColor)
                                }
                            }
                        }
                    }

                    // Stacked Bar Chart Card (Daily Complaints last 7 days)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerColor, RoundedCornerShape(16.dp))
                            .clickable { onExpandChart() },
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Daily Complaints", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("By category (last 7 days)", fontSize = 12.sp, color = secondaryTextColor)
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            val dailyStats = analyticsData?.complaints?.dailyStats ?: emptyList()
                            val days = dailyStats.map { it.day }
                            val stackedData = dailyStats.map { Triple(it.plumbing.toFloat(), it.electrical.toFloat(), it.lift.toFloat()) }
                            val maxVal = (dailyStats.maxOfOrNull { it.plumbing + it.electrical + it.lift }?.toFloat() ?: 9f).coerceAtLeast(1f)

                            Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val barCount = dailyStats.size
                                if (barCount > 0) {
                                    val barWidth = 20.dp.toPx()
                                    val spacing = (canvasWidth - (barWidth * barCount)) / (barCount + 1)

                                    // Draw Grid lines
                                    val gridLines = 4
                                    for (i in 0..gridLines) {
                                        val y = (canvasHeight - 20.dp.toPx()) * (i.toFloat() / gridLines)
                                        drawLine(
                                            color = textColor.copy(alpha = 0.05f),
                                            start = androidx.compose.ui.geometry.Offset(0f, y),
                                            end = androidx.compose.ui.geometry.Offset(canvasWidth, y),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }

                                    // Draw bars
                                    stackedData.forEachIndexed { index, (plumb, elec, lift) ->
                                        val x = spacing + index * (barWidth + spacing)
                                        var currentY = canvasHeight - 20.dp.toPx()
                                        
                                        // Plumbing (Blue)
                                        val plumbH = (plumb / maxVal) * (canvasHeight - 30.dp.toPx())
                                        if (plumbH > 0) {
                                            drawRect(
                                                color = Color(0xFF3B82F6),
                                                topLeft = androidx.compose.ui.geometry.Offset(x, currentY - plumbH),
                                                size = androidx.compose.ui.geometry.Size(barWidth, plumbH)
                                            )
                                            currentY -= plumbH
                                        }

                                        // Electrical (Orange)
                                        val elecH = (elec / maxVal) * (canvasHeight - 30.dp.toPx())
                                        if (elecH > 0) {
                                            drawRect(
                                                color = Color(0xFFF59E0B),
                                                topLeft = androidx.compose.ui.geometry.Offset(x, currentY - elecH),
                                                size = androidx.compose.ui.geometry.Size(barWidth, elecH)
                                            )
                                            currentY -= elecH
                                        }

                                        // Lift (Green)
                                        val liftH = (lift / maxVal) * (canvasHeight - 30.dp.toPx())
                                        if (liftH > 0) {
                                            drawRoundRect(
                                                color = Color(0xFF22C55E),
                                                topLeft = androidx.compose.ui.geometry.Offset(x, currentY - liftH),
                                                size = androidx.compose.ui.geometry.Size(barWidth, liftH),
                                                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                                            )
                                        }

                                        // Day Label under bar
                                        val textPaint = android.graphics.Paint().apply {
                                            color = secondaryTextColor.toArgb()
                                            textSize = 26f
                                            textAlign = android.graphics.Paint.Align.CENTER
                                        }
                                        drawContext.canvas.nativeCanvas.drawText(
                                            days[index],
                                            x + barWidth / 2,
                                            canvasHeight - 2.dp.toPx(),
                                            textPaint
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Legend Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    Pair(Color(0xFF3B82F6), "Plumbing"),
                                    Pair(Color(0xFFF59E0B), "Electrical"),
                                    Pair(Color(0xFF22C55E), "Lift")
                                ).forEachIndexed { index, (color, label) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(label, fontSize = 11.sp, color = secondaryTextColor)
                                    }
                                }
                            }
                        }
                    }

                    // Resolution Time Distribution Histogram Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resolution Time", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("Hours to resolve (distribution)", fontSize = 12.sp, color = secondaryTextColor)
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            val histogram = analyticsData?.complaints?.resolutionHistogram ?: emptyList()
                            val maxVal = (histogram.maxOfOrNull { it.count }?.toFloat() ?: 18f).coerceAtLeast(1f)

                            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val barCount = histogram.size
                                if (barCount > 0) {
                                    val spacing = 4.dp.toPx()
                                    val totalSpacing = spacing * (barCount + 1)
                                    val barWidth = (canvasWidth - totalSpacing) / barCount

                                    histogram.forEachIndexed { index, value ->
                                        val x = spacing + index * (barWidth + spacing)
                                        val countVal = value.count.toFloat()
                                        val h = (countVal / maxVal) * canvasHeight
                                        val alpha = 0.3f + (countVal / maxVal) * 0.7f
                                        drawRoundRect(
                                            color = PrimaryBlue.copy(alpha = alpha),
                                            topLeft = androidx.compose.ui.geometry.Offset(x, canvasHeight - h),
                                            size = androidx.compose.ui.geometry.Size(barWidth, h),
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                histogram.forEach { bin ->
                                    Text(bin.range, fontSize = 10.sp, color = secondaryTextColor)
                                }
                            }
                        }
                    }

                    // Top Categories Ranking Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Top Categories", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val totalCatComplaints = analyticsData?.complaints?.byCategory?.sumOf { it.count } ?: 0
                            val categoriesRank = analyticsData?.complaints?.byCategory?.take(4) ?: emptyList()

                            if (categoriesRank.isEmpty()) {
                                Text("No complaints recorded", color = secondaryTextColor, fontSize = 13.sp, modifier = Modifier.padding(vertical = 12.dp))
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    categoriesRank.forEachIndexed { index, catInfo ->
                                        val percent = if (totalCatComplaints > 0) catInfo.count.toFloat() / totalCatComplaints else 0f
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryBlue.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = (index + 1).toString(),
                                                    color = PrimaryBlue,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(catInfo.category, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
                                                    Text("${catInfo.count}", fontSize = 13.sp, color = secondaryTextColor)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(textColor.copy(alpha = 0.05f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(percent)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(PrimaryBlue)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun S47ChartFull(
    isDarkMode: Boolean,
    analyticsData: com.simats.appartmentliving.data.AnalyticsResponse?,
    onBack: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)

    var selectedChartType by remember { mutableStateOf("Bar") } // "Bar", "Line", "Donut"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
            Text(
                text = "Advanced Stats",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Chart type toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardColor)
                    .border(1.dp, dividerColor, RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Bar Chart Toggle
                val barSel = selectedChartType == "Bar"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (barSel) PrimaryBlue else Color.Transparent)
                        .clickable { selectedChartType = "Bar" }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.BarChart, contentDescription = null, tint = if (barSel) Color.White else secondaryTextColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complaints", color = if (barSel) Color.White else secondaryTextColor, fontSize = 12.sp, fontWeight = if (barSel) FontWeight.Bold else FontWeight.Normal)
                }

                // Line Chart Toggle
                val lineSel = selectedChartType == "Line"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (lineSel) PrimaryBlue else Color.Transparent)
                        .clickable { selectedChartType = "Line" }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.ShowChart, contentDescription = null, tint = if (lineSel) Color.White else secondaryTextColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Residents", color = if (lineSel) Color.White else secondaryTextColor, fontSize = 12.sp, fontWeight = if (lineSel) FontWeight.Bold else FontWeight.Normal)
                }

                // Donut Chart Toggle
                val donutSel = selectedChartType == "Donut"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (donutSel) PrimaryBlue else Color.Transparent)
                        .clickable { selectedChartType = "Donut" }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Outlined.PieChart, contentDescription = null, tint = if (donutSel) Color.White else secondaryTextColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payments", color = if (donutSel) Color.White else secondaryTextColor, fontSize = 12.sp, fontWeight = if (donutSel) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        when (selectedChartType) {
            "Bar" -> {
                // Complaints by Category
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Complaints by Category", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Distribution of total complaints", fontSize = 12.sp, color = secondaryTextColor)

                        Spacer(modifier = Modifier.height(28.dp))

                        val categories = analyticsData?.complaints?.byCategory ?: emptyList()
                        val maxCount = (categories.maxOfOrNull { it.count }?.toFloat() ?: 24f).coerceAtLeast(1f)

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            if (categories.isEmpty()) {
                                Text("No complaints recorded", color = secondaryTextColor, fontSize = 14.sp)
                            } else {
                                categories.forEach { (cat, count) ->
                                    val fraction = count.toFloat() / maxCount
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = cat,
                                            color = secondaryTextColor,
                                            fontSize = 13.sp,
                                            modifier = Modifier.width(90.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(16.dp)
                                                .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                                .background(textColor.copy(alpha = 0.05f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                                                    .background(PrimaryBlue)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = count.toString(),
                                            color = textColor,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "Line" -> {
                // Resident Monthly Registrations
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Resident Registrations", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text("Monthly registrations (last 6 months)", fontSize = 12.sp, color = secondaryTextColor)

                        Spacer(modifier = Modifier.height(28.dp))

                        val registrations = analyticsData?.residents?.monthlyRegistrations ?: emptyList()
                        val maxCount = (registrations.maxOfOrNull { it.count }?.toFloat() ?: 5f).coerceAtLeast(1f)

                        if (registrations.isEmpty()) {
                            Text("No registration data available", color = secondaryTextColor, fontSize = 14.sp)
                        } else {
                            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val pointsCount = registrations.size
                                val spacing = canvasWidth / (pointsCount - 1).coerceAtLeast(1)
                                
                                val points = registrations.mapIndexed { index, reg ->
                                    val x = index * spacing
                                    val y = canvasHeight - 24.dp.toPx() - (reg.count.toFloat() / maxCount) * (canvasHeight - 48.dp.toPx())
                                    androidx.compose.ui.geometry.Offset(x, y)
                                }

                                // Draw line path
                                for (i in 0 until points.size - 1) {
                                    drawLine(
                                        color = PrimaryBlue,
                                        start = points[i],
                                        end = points[i + 1],
                                        strokeWidth = 3.dp.toPx()
                                    )
                                }

                                // Draw circles and labels
                                points.forEachIndexed { index, point ->
                                    drawCircle(
                                        color = PrimaryBlue,
                                        radius = 5.dp.toPx(),
                                        center = point
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = point
                                    )

                                    // Count text above point
                                    val countTextPaint = android.graphics.Paint().apply {
                                        color = textColor.toArgb()
                                        textSize = 24f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isFakeBoldText = true
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(
                                        registrations[index].count.toString(),
                                        point.x,
                                        point.y - 8.dp.toPx(),
                                        countTextPaint
                                    )

                                    // Month label under chart
                                    val textPaint = android.graphics.Paint().apply {
                                        color = secondaryTextColor.toArgb()
                                        textSize = 22f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    drawContext.canvas.nativeCanvas.drawText(
                                        registrations[index].month.split(" ").firstOrNull() ?: "",
                                        point.x,
                                        canvasHeight - 2.dp.toPx(),
                                        textPaint
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "Donut" -> {
                // Payment Collections Donut & List
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Collections & Revenue", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val totalRev = analyticsData?.payments?.totalRevenue ?: 0.0
                        val collectionRate = analyticsData?.payments?.collectionRate ?: 0.0
                        val paidCount = analyticsData?.payments?.paidBills ?: 0
                        val unpaidCount = analyticsData?.payments?.unpaidBills ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Donut Canvas
                            Canvas(modifier = Modifier.size(100.dp)) {
                                val total = (paidCount + unpaidCount).toFloat()
                                val paidSweep = if (total > 0f) 360f * (paidCount.toFloat() / total) else 360f
                                val unpaidSweep = if (total > 0f) 360f * (unpaidCount.toFloat() / total) else 0f

                                drawArc(
                                    color = Color(0xFF10B981), // Paid (Green)
                                    startAngle = -90f,
                                    sweepAngle = paidSweep,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx())
                                )
                                if (unpaidSweep > 0f) {
                                    drawArc(
                                        color = Color(0xFFEF4444), // Unpaid (Red)
                                        startAngle = -90f + paidSweep,
                                        sweepAngle = unpaidSweep,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 16.dp.toPx())
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = String.format(java.util.Locale.US, "₹%.0f", totalRev),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "Collection Rate: %.1f%%", collectionRate),
                                    fontSize = 13.sp,
                                    color = secondaryTextColor
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$paidCount Paid", fontSize = 11.sp, color = secondaryTextColor)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("$unpaidCount Unpaid", fontSize = 11.sp, color = secondaryTextColor)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Monthly Collections (last 6 months)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val collections = analyticsData?.payments?.monthlyCollections ?: emptyList()
                            if (collections.isEmpty()) {
                                Text("No collection records available", color = secondaryTextColor, fontSize = 12.sp)
                            } else {
                                collections.forEach { collection ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(textColor.copy(alpha = 0.02f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(collection.month, fontSize = 12.sp, color = textColor)
                                        Text(
                                            text = String.format(java.util.Locale.US, "₹%.0f", collection.amount),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// PHASE 11: ADMIN BILL GENERATION
// ==========================================

@Composable
fun AdminSettingsMenuItem(
    icon: ImageVector,
    text: String,
    textColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    showChevron: Boolean = true
) {
    val secondaryTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (showChevron) {
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = secondaryTextColor)
        }
    }
}

@Composable
fun AdminSettingsToggleItem(
    icon: ImageVector,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    cardColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = textColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryBlue
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectResidentScreen(
    isDarkMode: Boolean,
    usersList: List<AdminUser>,
    onBack: () -> Unit,
    onContinue: (String, String, String) -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)
    val inputBg = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)

    val approvedUsers = usersList.filter { it.status == "Approved" }
    val residents = approvedUsers.map { Triple(it.name, "RES-${it.id}", it.flat) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedBlockFilter by remember { mutableStateOf("All Blocks") }
    var selectedId by remember { mutableStateOf("") }

    LaunchedEffect(residents) {
        if (selectedId.isEmpty() && residents.isNotEmpty()) {
            selectedId = residents.first().second
        }
    }

    val filteredResidents = residents.filter { (name, id, apt) ->
        val matchesSearch = name.contains(searchQuery, ignoreCase = true) || 
                            id.contains(searchQuery, ignoreCase = true) || 
                            apt.contains(searchQuery, ignoreCase = true)
        val matchesBlock = selectedBlockFilter == "All Blocks" || apt.contains(selectedBlockFilter.takeLast(1))
        matchesSearch && matchesBlock
    }

    val activeSelected = residents.find { it.second == selectedId }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Text(
                    text = "Generate Bill",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Step Indicator
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Step 1 of 3", fontSize = 12.sp, color = secondaryTextColor)
                    Text("Choose resident to bill", fontSize = 12.sp, color = secondaryTextColor)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                placeholder = { Text("Search by name, apt no, or ID", color = secondaryTextColor) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = secondaryTextColor)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Block filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("All Blocks", "Block C", "Block B", "Block A").forEach { block ->
                    val isSel = selectedBlockFilter == block
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) PrimaryBlue else cardColor)
                            .border(1.dp, if (isSel) Color.Transparent else dividerColor, RoundedCornerShape(20.dp))
                            .clickable { selectedBlockFilter = block }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = block,
                            color = if (isSel) Color.White else secondaryTextColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Resident List
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredResidents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No residents found matching criteria", color = secondaryTextColor, fontSize = 14.sp)
                    }
                } else {
                    filteredResidents.forEach { (name, id, apt) ->
                        val isSel = selectedId == id
                        val initials = name.split(" ").map { it.take(1) }.joinToString("")
                        val isThreeBhk = apt.contains("3")
                        val flatType = if (isThreeBhk) "3 BHK" else "2 BHK"
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSel) PrimaryBlue.copy(alpha = 0.08f) else cardColor)
                                .border(
                                    1.dp,
                                    if (isSel) PrimaryBlue else dividerColor,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedId = id }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(id, fontSize = 12.sp, color = secondaryTextColor, fontFamily = FontFamily.Monospace)
                                    Text("·", fontSize = 12.sp, color = secondaryTextColor)
                                    Text("Apt $apt", fontSize = 12.sp, color = secondaryTextColor)
                                    Text("·", fontSize = 12.sp, color = secondaryTextColor)
                                    Text(flatType, fontSize = 12.sp, color = secondaryTextColor)
                                }
                            }
                            
                            if (isSel) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            } else {
                                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = secondaryTextColor)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Sticky footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(backgroundColor.copy(alpha = 0.95f))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel",
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBack() }
                )
                
                Button(
                    onClick = {
                        if (activeSelected != null) {
                            onContinue(activeSelected.first, activeSelected.second, activeSelected.third)
                        }
                    },
                    enabled = activeSelected != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = if (activeSelected != null) "Continue with ${activeSelected.first.split(" ").first()} →" else "Continue",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGenerateBillScreen(
    isDarkMode: Boolean,
    residentName: String,
    residentId: String,
    residentApt: String,
    initialItems: List<Triple<String, Int, Boolean>>,
    initialMonth: String,
    initialDueDate: String,
    initialNotes: String,
    onBack: () -> Unit,
    onPreview: (List<Triple<String, Int, Boolean>>, String, String, String) -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)
    val inputBg = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)

    var billingMonth by remember { mutableStateOf(initialMonth) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
    var notes by remember { mutableStateOf(initialNotes) }

    val itemsState = remember {
        mutableStateListOf<Triple<String, Int, Boolean>>().apply {
            addAll(initialItems)
        }
    }

    val totalAmount = itemsState.sumOf { it.second }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Text(
                    text = "Bill Details",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Step Indicator
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Step 2 of 3", fontSize = 12.sp, color = secondaryTextColor)
                    Text("Bill Items", fontSize = 12.sp, color = secondaryTextColor)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.66f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selected resident header card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.03f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BILLING TO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text(
                                "Change",
                                fontSize = 12.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { onBack() }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    residentName.split(" ").map { it.take(1) }.joinToString(""),
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(residentName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Text(residentId, fontSize = 11.sp, color = secondaryTextColor, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Flat", fontSize = 10.sp, color = secondaryTextColor)
                                Text(residentApt, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                            Column {
                                Text("Floor", fontSize = 10.sp, color = secondaryTextColor)
                                Text("4th", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                            Column {
                                Text("Type", fontSize = 10.sp, color = secondaryTextColor)
                                Text("2 BHK", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }
                    }
                }

                // Billing period inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Billing month", fontSize = 12.sp, color = secondaryTextColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(inputBg)
                                .clickable { /* change month */ }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.DateRange, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(billingMonth, fontSize = 13.sp, color = textColor)
                                }
                                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = secondaryTextColor, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Due date", fontSize = 12.sp, color = secondaryTextColor)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(inputBg)
                                .clickable { /* change due date */ }
                                .padding(12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.DateRange, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(dueDate, fontSize = 13.sp, color = textColor)
                                }
                                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = secondaryTextColor, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // Line items charge list
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Charges", fontSize = 13.sp, color = secondaryTextColor, fontWeight = FontWeight.SemiBold)
                        Text(
                            "+ Add custom",
                            fontSize = 13.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                itemsState.add(Triple("Custom Charge", 100, false))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column {
                            itemsState.forEachIndexed { index, (label, amount, required) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(label, fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
                                        if (required) {
                                            Text("Required", fontSize = 10.sp, color = secondaryTextColor)
                                        }
                                    }

                                    // Amount Input Box
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(inputBg)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("₹", fontSize = 13.sp, color = secondaryTextColor)
                                        Spacer(modifier = Modifier.width(2.dp))
                                        
                                        BasicTextField(
                                            value = amount.toString(),
                                            onValueChange = { newVal ->
                                                val clean = newVal.filter { it.isDigit() }
                                                val intVal = clean.toIntOrNull() ?: 0
                                                itemsState[index] = Triple(label, intVal, required)
                                            },
                                            textStyle = androidx.compose.ui.text.TextStyle(
                                                color = textColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                                            ),
                                            modifier = Modifier.width(60.dp),
                                            singleLine = true,
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                            )
                                        )
                                    }

                                    if (!required) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { itemsState.removeAt(index) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                if (index != itemsState.lastIndex) {
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                                }
                            }
                        }
                    }
                }

                // Notes input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Notes (optional)", fontSize = 12.sp, color = secondaryTextColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardColor,
                            unfocusedContainerColor = cardColor,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = dividerColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        placeholder = { Text("Add any additional information...", color = secondaryTextColor) },
                        maxLines = 2
                    )
                }

                // Total Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL AMOUNT", fontSize = 10.sp, color = secondaryTextColor, fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%,d", totalAmount)}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${itemsState.size} items", fontSize = 12.sp, color = secondaryTextColor)
                            Text("Due May 28", fontSize = 11.sp, color = secondaryTextColor)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Footer buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(backgroundColor.copy(alpha = 0.95f))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back",
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBack() }
                )
                
                Button(
                    onClick = {
                        onPreview(itemsState.toList(), billingMonth, dueDate, notes)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Preview Bill", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PaymentBillPreviewScreen(
    isDarkMode: Boolean,
    residentName: String,
    residentId: String,
    residentApt: String,
    items: List<Triple<String, Int, Boolean>>,
    month: String,
    dueDate: String,
    notes: String,
    onBack: () -> Unit,
    onSendBill: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)
    val total = items.sumOf { it.second }

    var notifyResident by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Text(
                    text = "Preview & Send",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Step Indicator
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Step 3 of 3", fontSize = 12.sp, color = secondaryTextColor)
                    Text("Review & Send", fontSize = 12.sp, color = secondaryTextColor)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(PrimaryBlue)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Receipt card (Greenview branded)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        // Branded Blue strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PrimaryBlue)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Greenview Apartments · Block C", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("#BILL-2026-0512", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }

                        // Receipt Details
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Bill".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 0.5.sp)
                            Text(month, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E0E0E))

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Billed to".uppercase(), fontSize = 9.sp, color = Color.Gray)
                                    Text(residentName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E0E0E))
                                    Text("$residentApt · RES-0042", fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Due by".uppercase(), fontSize = 9.sp, color = Color.Gray)
                                    Text(dueDate, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E0E0E))
                                    Text("7 days", fontSize = 12.sp, color = Color.DarkGray)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Breakdown charges
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items.forEach { (label, amt) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(label, fontSize = 13.sp, color = Color.DarkGray)
                                        Text("₹${String.format("%,d", amt)}", fontSize = 13.sp, color = Color(0xFF0E0E0E), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color(0xFF0E0E0E)))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Due", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E0E0E))
                                Text("₹${String.format("%,d", total)}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E0E0E))
                            }
                        }

                        // Branded QR Scan Code Section at bottom
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC))
                                .border(BorderStroke(1.dp, Color(0xFFE2E8F0)))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            QRCodeComponent(sizeDp = 80.dp, color = Color(0xFF0E0E0E))
                            Column {
                                Text("Scan to Pay".uppercase(), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text("greenview@upi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0E0E0E))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("PhonePe: +91 98765 12345", fontSize = 11.sp, color = Color.DarkGray)
                                Text("UPI also via GPay, Paytm", fontSize = 11.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }

                // Notify Resident Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Send, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notify resident", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("Push notification + email to ${residentName.split(" ").first()}", fontSize = 12.sp, color = secondaryTextColor)
                        }
                        Switch(
                            checked = notifyResident,
                            onCheckedChange = { notifyResident = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryBlue
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Sticky footer Send button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(backgroundColor.copy(alpha = 0.95f))
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Back",
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onBack() }
                )
                
                Button(
                    onClick = onSendBill,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Send Bill to Resident", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsDashboardScreen(
    isDarkMode: Boolean,
    paymentsList: List<com.simats.appartmentliving.data.PaymentDto>,
    isLoading: Boolean,
    errorMessage: String?,
    repository: com.simats.appartmentliving.data.ComplaintsRepository,
    onBack: () -> Unit,
    onManualBill: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)

    var searchQuery by remember { mutableStateOf("") }
    var selectedMonthFilter by remember { mutableStateOf("All Months") }
    var selectedYearFilter by remember { mutableStateOf("All Years") }
    var activeTabFilter by remember { mutableStateOf("All") } // "All", "Paid", "Pending"

    var showGenerateDialog by remember { mutableStateOf(false) }
    var selectedGenMonth by remember { mutableStateOf("May") }
    var selectedGenYear by remember { mutableStateOf("2026") }
    var isGenerating by remember { mutableStateOf(false) }

    // Metrics Calculations
    val totalRevenue = paymentsList.filter { it.status.lowercase() == "paid" }.sumOf { it.amount ?: it.totalAmount }
    val pendingAmount = paymentsList.filter { it.status.lowercase() != "paid" }.sumOf { it.amount ?: it.totalAmount }
    val collectionRate = if (totalRevenue + pendingAmount > 0) {
        (totalRevenue.toDouble() / (totalRevenue + pendingAmount)) * 100
    } else {
        0.0
    }

    if (showGenerateDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { if (!isGenerating) showGenerateDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Auto-Generate Bills", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("This will generate monthly maintenance bills automatically for all approved residents who do not have a bill for this month/year. Base rates: 1BHK=2500, 2BHK=3500, 3BHK=5000.", color = secondaryTextColor, fontSize = 13.sp)

                    // Month dropdown simulator
                    var showMonthMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedGenMonth,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Billing Month") },
                            modifier = Modifier.fillMaxWidth().clickable { showMonthMenu = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.15f)
                            ),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = textColor) }
                        )
                        DropdownMenu(expanded = showMonthMenu, onDismissRequest = { showMonthMenu = false }) {
                            listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December").forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m) },
                                    onClick = { selectedGenMonth = m; showMonthMenu = false }
                                )
                            }
                        }
                    }

                    // Year dropdown simulator
                    var showYearMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedGenYear,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Billing Year") },
                            modifier = Modifier.fillMaxWidth().clickable { showYearMenu = true },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = textColor.copy(alpha = 0.15f)
                            ),
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = textColor) }
                        )
                        DropdownMenu(expanded = showYearMenu, onDismissRequest = { showYearMenu = false }) {
                            listOf("2025", "2026", "2027", "2028").forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y) },
                                    onClick = { selectedGenYear = y; showYearMenu = false }
                                )
                            }
                        }
                    }

                    if (isGenerating) {
                        Box(modifier = Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(24.dp))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showGenerateDialog = false },
                            enabled = !isGenerating,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = textColor)
                        }

                        Button(
                            onClick = {
                                isGenerating = true
                                repository.generateMonthlyBills(selectedGenMonth, selectedGenYear) { err, count ->
                                    isGenerating = false
                                    showGenerateDialog = false
                                    if (err != null) {
                                        android.widget.Toast.makeText(context, "Error: ${err.message}", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Successfully generated $count bills!", android.widget.Toast.LENGTH_LONG).show()
                                        onRefresh()
                                    }
                                }
                            },
                            enabled = !isGenerating,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Generate", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                }
                Text(
                    text = "Payments Dashboard",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = textColor)
                }
            }

            // Scrollable Content
            var isSwipeRefreshing by remember { mutableStateOf(false) }
            PullToRefreshBox(
                isRefreshing = isSwipeRefreshing,
                onRefresh = {
                    isSwipeRefreshing = true
                    repository.syncPayments(
                        residentId = null,
                        localList = paymentsList as SnapshotStateList<com.simats.appartmentliving.data.PaymentDto>,
                        onStart = {},
                        onComplete = {
                            isSwipeRefreshing = false
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Financial Metrics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminFinanceCard(
                            modifier = Modifier.weight(1f),
                            title = "₹${String.format("%,d", totalRevenue)}",
                            subtitle = "Total Revenue",
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            iconColor = Color(0xFF10B981)
                        )
                        AdminFinanceCard(
                            modifier = Modifier.weight(1f),
                            title = "₹${String.format("%,d", pendingAmount)}",
                            subtitle = "Pending Amount",
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            iconColor = Color(0xFFF59E0B)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminFinanceCard(
                            modifier = Modifier.weight(1f),
                            title = String.format("%.1f%%", collectionRate),
                            subtitle = "Collection Rate",
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryTextColor = secondaryTextColor,
                            iconColor = PrimaryBlue
                        )
                    }

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showGenerateDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Auto-Generate", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onManualBill,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Outlined.Add, null, tint = textColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Individual Bill", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Search and Filters Section
                    Text("Resident Bills", color = textColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or flat number...", color = secondaryTextColor) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = secondaryTextColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = textColor.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Dropdowns Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var showMonthDropdown by remember { mutableStateOf(false) }
                        var showYearDropdown by remember { mutableStateOf(false) }

                        // Month Filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showMonthDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
                            ) {
                                Text(selectedMonthFilter, color = textColor, maxLines = 1)
                                Icon(Icons.Default.ArrowDropDown, null, tint = textColor)
                            }
                            DropdownMenu(expanded = showMonthDropdown, onDismissRequest = { showMonthDropdown = false }) {
                                (listOf("All Months", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")).forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m) },
                                        onClick = { selectedMonthFilter = m; showMonthDropdown = false }
                                    )
                                }
                            }
                        }

                        // Year Filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showYearDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, textColor.copy(alpha = 0.15f))
                            ) {
                                Text(selectedYearFilter, color = textColor, maxLines = 1)
                                Icon(Icons.Default.ArrowDropDown, null, tint = textColor)
                            }
                            DropdownMenu(expanded = showYearDropdown, onDismissRequest = { showYearDropdown = false }) {
                                (listOf("All Years", "2025", "2026", "2027", "2028")).forEach { y ->
                                    DropdownMenuItem(
                                        text = { Text(y) },
                                        onClick = { selectedYearFilter = y; showYearDropdown = false }
                                    )
                                }
                            }
                        }
                    }

                    // Tab filter (All, Paid, Pending)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Paid", "Pending").forEach { filter ->
                            val isSelected = activeTabFilter == filter
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue else cardColor)
                                    .border(1.dp, if (isSelected) Color.Transparent else textColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { activeTabFilter = filter }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(filter, color = if (isSelected) Color.White else textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Loading / Error
                    if (isLoading && paymentsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryBlue)
                        }
                    } else if (errorMessage != null && paymentsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                            Text(errorMessage, color = Color(0xFFEF4444))
                        }
                    } else {
                        // Filtered payments list
                        val filteredList = paymentsList.filter { bill ->
                            val matchesSearch = bill.residentName.contains(searchQuery, ignoreCase = true) ||
                                                bill.flatNumber.contains(searchQuery, ignoreCase = true)
                            val matchesMonth = selectedMonthFilter == "All Months" || bill.month.contains(selectedMonthFilter, ignoreCase = true)
                            val matchesYear = selectedYearFilter == "All Years" || (bill.year ?: "").contains(selectedYearFilter, ignoreCase = true)
                            val matchesStatus = activeTabFilter == "All" ||
                                                (activeTabFilter == "Paid" && bill.status.lowercase() == "paid") ||
                                                (activeTabFilter == "Pending" && bill.status.lowercase() != "paid")
                            matchesSearch && matchesMonth && matchesYear && matchesStatus
                        }

                        if (filteredList.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                                Text("No payments found", color = secondaryTextColor, fontSize = 14.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                filteredList.forEach { bill ->
                                    val isPaid = bill.status.lowercase() == "paid"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(cardColor)
                                            .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(bill.residentName, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text("Flat ${bill.flatNumber} · ${bill.month} ${bill.year ?: ""}", color = secondaryTextColor, fontSize = 11.sp)
                                        }

                                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("₹${String.format("%,d", bill.amount ?: bill.totalAmount)}", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isPaid) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (isPaid) "Paid" else "Pending",
                                                    color = if (isPaid) Color(0xFF22C55E) else Color(0xFFF59E0B),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun AdminFinanceCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    cardColor: Color,
    textColor: Color,
    secondaryTextColor: Color,
    iconColor: Color
) {
    Card(
        modifier = modifier.border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(subtitle, color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Box(
                    modifier = Modifier.size(6.dp).clip(CircleShape).background(iconColor)
                )
            }
            Text(title, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun NoticeAdminCard(
    notice: com.simats.appartmentliving.data.NoticeDto,
    isDarkMode: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color,
    borderColor: Color,
    onDeleteClick: () -> Unit
) {
    val priorityColor = when (notice.priority.lowercase()) {
        "high" -> Color(0xFFEF4444)
        "medium" -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = notice.category,
                            color = PrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = notice.priority,
                            color = priorityColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = notice.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = notice.description,
                fontSize = 13.sp,
                color = secondaryTextColor
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "By ${notice.createdBy}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor.copy(alpha = 0.8f)
                )
                
                val displayDate = if (notice.createdAt != null && notice.createdAt.length >= 10) {
                    notice.createdAt.substring(0, 10)
                } else {
                    "Today"
                }
                
                Text(
                    text = displayDate,
                    fontSize = 11.sp,
                    color = secondaryTextColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
