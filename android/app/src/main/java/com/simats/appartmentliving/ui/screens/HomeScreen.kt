package com.simats.appartmentliving.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import com.simats.appartmentliving.ui.theme.PrimaryBlue
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// Data Models
data class TimelineStep(
    val label: String,
    val date: String,
    val status: String // "done", "active", "pending"
)

data class AdminComment(
    val name: String,
    val initials: String,
    val comment: String,
    val timeAgo: String
)

data class Complaint(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val status: String, // "Pending", "In Progress", "Resolved"
    val priority: String, // "Low", "Medium", "High"
    val description: String,
    val timeline: List<TimelineStep>,
    val adminComment: AdminComment? = null,
    val photoAttached: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isDarkMode: Boolean,
    onLogout: () -> Unit,
    onThemeToggle: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFFFFFFF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)
    val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0)

    var selectedTab by remember { mutableStateOf("Home") }
    var complaintsSubScreen by remember { mutableStateOf("list") } // "list", "new", "detail"
    var selectedComplaintId by remember { mutableStateOf<String?>(null) }
    var uiStatePreview by remember { mutableStateOf<String?>(null) } // "loading", "error", "offline", "notifications", "toasts"
    var paymentsTabSubScreen by remember { mutableStateOf("bill") }

    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { com.simats.appartmentliving.data.AuthManager(context) }
    val user = remember { authManager.getUser() }

    var residentName by remember { mutableStateOf((user?.residentName ?: "Resident").capitalizeWords()) }
    var selectedAvatarBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var residentEmail by remember { mutableStateOf(user?.email ?: "") }
    var residentPhone by remember { mutableStateOf(user?.phone ?: "") }
    var residentFlat by remember {
        mutableStateOf(
            if (!user?.block.isNullOrEmpty()) "${user.block}-${user.flatNumber}" else (user?.flatNumber ?: "")
        )
    }
    var residentFlatType by remember { mutableStateOf(user?.flatType ?: "2 BHK") }
    val currentResidentId = user?.residentId ?: user?.id ?: ""
    var avatarInitials by remember {
        mutableStateOf(
            if (!user?.residentName.isNullOrEmpty()) {
                user.residentName.split(" ")
                    .filter { it.isNotEmpty() }
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .take(2)
                    .joinToString("")
                    .uppercase()
            } else {
                "RE"
            }
        )
    }
    var profileSubScreen by remember { mutableStateOf("view") } // "view", "edit", "change_password"
    var profileSubScreenBackTo by remember { mutableStateOf("Profile") }
    var showLogoutConfirm by remember { mutableStateOf(false) }


    // Settings Toggle States
    var pushNotifications by remember { mutableStateOf(true) }
    var emailAlerts by remember { mutableStateOf(true) }
    var biometricLogin by remember { mutableStateOf(false) }

    // Dynamic Complaints List State
    val complaintsList = remember { mutableStateListOf<Complaint>() }

    val coroutineScope = rememberCoroutineScope()
    val repository = remember { com.simats.appartmentliving.data.ComplaintsRepository(coroutineScope) }
    var isComplaintsLoading by remember { mutableStateOf(false) }
    var complaintsError by remember { mutableStateOf<String?>(null) }

    // Notices state and ViewModel
    val noticeViewModel = remember { com.simats.appartmentliving.ui.viewmodels.NoticeViewModel() }
    val latestNoticesState by noticeViewModel.latestNotices.collectAsState(initial = emptyList())
    val noticesState by noticeViewModel.notices.collectAsState(initial = emptyList())
    val isNoticesLoading by noticeViewModel.isLoading.collectAsState(initial = false)
    val noticesError by noticeViewModel.error.collectAsState(initial = null)

    LaunchedEffect(selectedTab, currentResidentId) {
        if ((selectedTab == "Complaints" || selectedTab == "Home") && currentResidentId.isNotEmpty()) {
            repository.syncResidentComplaints(
                residentId = currentResidentId,
                localList = complaintsList,
                onStart = {
                    isComplaintsLoading = true
                    complaintsError = null
                },
                onComplete = { error ->
                    isComplaintsLoading = false
                    if (error != null) {
                        complaintsError = error.message ?: "Failed to sync complaints"
                    }
                }
            )
        }
        if (selectedTab == "Home" && currentResidentId.isNotEmpty()) {
            noticeViewModel.fetchLatestNotices()
        }
    }

    LaunchedEffect(uiStatePreview) {
        if (uiStatePreview == "notifications_list") {
            noticeViewModel.fetchNotices()
        }
    }

    val paymentsList = remember { mutableStateListOf<com.simats.appartmentliving.data.PaymentDto>() }
    var isPaymentsLoading by remember { mutableStateOf(false) }
    var paymentsError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedTab, currentResidentId) {
        if ((selectedTab == "Payments" || selectedTab == "Home") && currentResidentId.isNotEmpty()) {
            repository.syncPayments(
                residentId = currentResidentId,
                localList = paymentsList,
                onStart = {
                    isPaymentsLoading = true
                    paymentsError = null
                },
                onComplete = { error ->
                    isPaymentsLoading = false
                    if (error != null) {
                        paymentsError = error.message ?: "Failed to sync bills"
                    }
                }
            )
        }
    }

    LaunchedEffect(currentResidentId) {
        if (currentResidentId.isNotEmpty()) {
            while (true) {
                repository.syncResidentComplaints(
                    residentId = currentResidentId,
                    localList = complaintsList,
                    onStart = {},
                    onComplete = {}
                )
                repository.syncPayments(
                    residentId = currentResidentId,
                    localList = paymentsList,
                    onStart = {},
                    onComplete = {}
                )
                delay(10000)
            }
        }
    }

    val activeBill = paymentsList.firstOrNull { it.status != "Paid" } ?: paymentsList.firstOrNull()

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            if (complaintsSubScreen == "list" && profileSubScreen == "view") {
                // Custom Bottom Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(cardColor)
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(icon = Icons.Default.Home, label = "Home", isSelected = selectedTab == "Home", onClick = { selectedTab = "Home" }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.Outlined.Assignment, label = "Complaints", isSelected = selectedTab == "Complaints", onClick = { selectedTab = "Complaints" }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.Outlined.Person, label = "Profile", isSelected = selectedTab == "Profile", onClick = { selectedTab = "Profile" }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.Outlined.CreditCard, label = "Payments", isSelected = selectedTab == "Payments", onClick = { selectedTab = "Payments"; paymentsTabSubScreen = "bill" }, modifier = Modifier.weight(1f))
                    BottomNavItem(icon = Icons.Outlined.Settings, label = "Settings", isSelected = selectedTab == "Settings", onClick = { selectedTab = "Settings" }, modifier = Modifier.weight(1f))
                }
            }
        },
        floatingActionButton = {
            if (complaintsSubScreen == "list" && profileSubScreen == "view" && (selectedTab == "Home" || selectedTab == "Complaints")) {
                FloatingActionButton(
                    onClick = {
                        selectedTab = "Complaints"
                        complaintsSubScreen = "new"
                    },
                    containerColor = PrimaryBlue,
                    shape = CircleShape,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(64.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Complaint", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { paddingValues ->
        if (uiStatePreview != null) {
            BackHandler {
                if (uiStatePreview == "payment_method_sheet") {
                    uiStatePreview = "resident_bill"
                } else if (uiStatePreview == "payment_success") {
                    uiStatePreview = "resident_bill"
                } else {
                    uiStatePreview = null
                }
            }
        } else if (selectedTab == "Payments") {
            BackHandler {
                if (paymentsTabSubScreen == "method_sheet") {
                    paymentsTabSubScreen = "bill"
                } else if (paymentsTabSubScreen == "success") {
                    paymentsTabSubScreen = "bill"
                    selectedTab = "Home"
                } else {
                    selectedTab = "Home"
                }
            }
        } else if (profileSubScreen != "view") {
            BackHandler {
                selectedTab = profileSubScreenBackTo
                profileSubScreen = "view"
            }
        } else if (complaintsSubScreen != "list") {
            BackHandler {
                complaintsSubScreen = "list"
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiStatePreview != null) {
                when (uiStatePreview) {
                    "loading" -> SkeletonLoadingScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null })
                    "error" -> SomethingWentWrongScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null }, onTryAgain = { uiStatePreview = null })
                    "offline" -> YouAreOfflineScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null }, onRetryNow = { uiStatePreview = null })
                    "notifications" -> NotificationsEmptyScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null })
                    "notifications_list" -> NotificationCenterScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null }, noticeViewModel = noticeViewModel)
                    "maintenance" -> MaintenancePaymentScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null })
                    "design_summary" -> DesignSummaryScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null })
                    "toasts" -> ToastVariantsScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null })
                    "maps_screen" -> SmartMapsScreen(isDarkMode = isDarkMode, onBack = { uiStatePreview = null })
                    "resident_bill" -> PaymentResidentBillScreen(
                        isDarkMode = isDarkMode,
                        onBack = { uiStatePreview = null },
                        onPay = { uiStatePreview = "payment_method_sheet" },
                        activeBill = activeBill
                    )
                    "payment_method_sheet" -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            PaymentResidentBillScreen(
                                isDarkMode = isDarkMode,
                                onBack = { uiStatePreview = null },
                                onPay = {},
                                activeBill = activeBill
                            )
                            PaymentMethodSheetScreen(
                                isDarkMode = isDarkMode,
                                onBack = { uiStatePreview = "resident_bill" },
                                onSelectApp = { appName ->
                                    if (activeBill != null) {
                                        repository.payBill(
                                            billId = activeBill.id ?: "",
                                            paymentApp = appName,
                                            transactionId = "TXN-${(100000..999999).random()}",
                                            onComplete = { error ->
                                                if (error == null) {
                                                    repository.syncPayments(
                                                        residentId = currentResidentId,
                                                        localList = paymentsList,
                                                        onStart = { isPaymentsLoading = true },
                                                        onComplete = { isPaymentsLoading = false }
                                                    )
                                                    uiStatePreview = "payment_success"
                                                } else {
                                                    android.widget.Toast.makeText(context, "Payment failed: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        )
                                    } else {
                                        uiStatePreview = "payment_success"
                                    }
                                },
                                activeBill = activeBill
                            )
                        }
                    }
                    "payment_success" -> PaymentSuccessScreen(
                        isDarkMode = isDarkMode,
                        onDone = { uiStatePreview = null }
                    )
                }
            } else if (complaintsSubScreen == "new") {
                NewComplaintScreen(
                    isDarkMode = isDarkMode,
                    onBack = { complaintsSubScreen = "list" },
                    onSubmit = { newComplaint ->
                        repository.addResidentComplaint(
                            title = newComplaint.title,
                            description = newComplaint.description,
                            category = newComplaint.category,
                            priority = newComplaint.priority,
                            photoAttached = newComplaint.photoAttached,
                            residentName = residentName,
                            residentId = currentResidentId,
                            flatNumber = residentFlat,
                            localList = complaintsList,
                            onComplete = {}
                        )
                        complaintsSubScreen = "list"
                    }
                )
            } else if (complaintsSubScreen == "detail") {
                val complaint = complaintsList.find { it.id == selectedComplaintId }
                if (complaint != null) {
                    ComplaintDetailScreen(
                        complaint = complaint,
                        isDarkMode = isDarkMode,
                        onBack = { complaintsSubScreen = "list" }
                    )
                } else {
                    complaintsSubScreen = "list"
                }
            } else if (profileSubScreen == "edit") {
                EditProfileScreen(
                    isDarkMode = isDarkMode,
                    initialName = residentName,
                    initialEmail = residentEmail,
                    initialPhone = residentPhone,
                    initialFlat = residentFlat,
                    initialAvatarBitmap = selectedAvatarBitmap,
                    onDiscard = {
                        selectedTab = profileSubScreenBackTo
                        profileSubScreen = "view"
                    },
                    onSave = { name, email, phone, flat, avatarBitmap ->
                        residentName = name.capitalizeWords()
                        residentEmail = email
                        residentPhone = phone
                        residentFlat = flat
                        selectedAvatarBitmap = avatarBitmap
                        avatarInitials = getInitials(name)
                        selectedTab = profileSubScreenBackTo
                        profileSubScreen = "view"
                    }
                )
            } else if (profileSubScreen == "change_password") {
                ChangePasswordSubScreen(
                    isDarkMode = isDarkMode,
                    onBack = {
                        selectedTab = profileSubScreenBackTo
                        profileSubScreen = "view"
                    },
                    onPasswordUpdated = {
                        selectedTab = profileSubScreenBackTo
                        profileSubScreen = "view"
                    }
                )
            } else {
                if (selectedTab == "Payments") {
                    when (paymentsTabSubScreen) {
                        "bill" -> {
                            PaymentResidentBillScreen(
                                isDarkMode = isDarkMode,
                                onBack = { selectedTab = "Home" },
                                onPay = { paymentsTabSubScreen = "method_sheet" },
                                activeBill = activeBill,
                                allBills = paymentsList
                            )
                        }
                        "method_sheet" -> {
                            Box(modifier = Modifier.fillMaxSize()) {
                                PaymentResidentBillScreen(
                                    isDarkMode = isDarkMode,
                                    onBack = { selectedTab = "Home" },
                                    onPay = {},
                                    activeBill = activeBill,
                                    allBills = paymentsList
                                )
                                PaymentMethodSheetScreen(
                                    isDarkMode = isDarkMode,
                                    onBack = { paymentsTabSubScreen = "bill" },
                                    onSelectApp = { appName ->
                                        if (activeBill != null) {
                                            repository.payBill(
                                                billId = activeBill.id ?: "",
                                                paymentApp = appName,
                                                transactionId = "TXN-${(100000..999999).random()}",
                                                onComplete = { error ->
                                                    if (error == null) {
                                                        repository.syncPayments(
                                                            residentId = currentResidentId,
                                                            localList = paymentsList,
                                                            onStart = { isPaymentsLoading = true },
                                                            onComplete = { isPaymentsLoading = false }
                                                        )
                                                        paymentsTabSubScreen = "success"
                                                    } else {
                                                        android.widget.Toast.makeText(context, "Payment failed: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            )
                                        } else {
                                            paymentsTabSubScreen = "success"
                                        }
                                    },
                                    activeBill = activeBill
                                )
                            }
                        }
                        "success" -> {
                            PaymentSuccessScreen(
                                isDarkMode = isDarkMode,
                                onDone = {
                                    paymentsTabSubScreen = "bill"
                                    selectedTab = "Home"
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        var isSwipeRefreshing by remember { mutableStateOf(false) }
                        
                        PullToRefreshBox(
                            isRefreshing = isSwipeRefreshing,
                            onRefresh = {
                                isSwipeRefreshing = true
                                if (selectedTab == "Complaints" || selectedTab == "Home") {
                                    repository.syncResidentComplaints(
                                        residentId = currentResidentId,
                                        localList = complaintsList,
                                        onStart = {},
                                        onComplete = {
                                            isSwipeRefreshing = false
                                        }
                                    )
                                } else if (selectedTab == "Payments" && currentResidentId.isNotEmpty()) {
                                    repository.syncPayments(
                                        residentId = currentResidentId,
                                        localList = paymentsList,
                                        onStart = {},
                                        onComplete = {
                                            isSwipeRefreshing = false
                                        }
                                    )
                                } else {
                                    isSwipeRefreshing = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 24.dp, bottom = 48.dp)
                            ) {
                                if (selectedTab == "Home") {
                            // Top Profile Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(cardColor)
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                         // Avatar
                                         Box(
                                             modifier = Modifier
                                                 .size(56.dp)
                                                 .clip(CircleShape)
                                                 .border(2.dp, PrimaryBlue, CircleShape)
                                                 .background(inputBackgroundColor),
                                             contentAlignment = Alignment.Center
                                         ) {
                                             if (selectedAvatarBitmap != null) {
                                                 Image(
                                                     bitmap = selectedAvatarBitmap!!.asImageBitmap(),
                                                     contentDescription = null,
                                                     modifier = Modifier.fillMaxSize(),
                                                     contentScale = ContentScale.Crop
                                                 )
                                             } else {
                                                 Icon(Icons.Default.Person, contentDescription = null, tint = secondaryTextColor)
                                             }
                                         }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        Column {
                                            Text(text = "Welcome back,", color = secondaryTextColor, fontSize = 14.sp)
                                            Text(text = residentName.capitalizeWords(), color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Notification Bell
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(inputBackgroundColor)
                                            .clickable { uiStatePreview = "notifications_list" },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = textColor)
                                        
                                        // Red dot
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF4444))
                                                .align(Alignment.TopEnd)
                                                .offset(x = (-10).dp, y = 12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Active Complaints
                                val activeCount = complaintsList.count { it.status != "Resolved" }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(cardColor)
                                        .padding(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Info, contentDescription = null, tint = PrimaryBlue)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = activeCount.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(text = "Active Complaints", fontSize = 14.sp, color = secondaryTextColor)
                                }

                                // Resolved
                                val resolvedCount = complaintsList.count { it.status == "Resolved" }
                                val greenColor = Color(0xFF22C55E)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(cardColor)
                                        .padding(20.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(greenColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = greenColor)
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(text = resolvedCount.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(text = "Resolved", fontSize = 14.sp, color = secondaryTextColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Latest Notices Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Latest Notices",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "View All",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.clickable { uiStatePreview = "notifications_list" }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Latest notices rendering
                            if (isNoticesLoading && latestNoticesState.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(24.dp))
                                }
                            } else if (noticesError != null && latestNoticesState.isEmpty()) {
                                Text(
                                    text = noticesError ?: "Failed to load notices",
                                    color = Color(0xFFEF4444),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else if (latestNoticesState.isEmpty()) {
                                Text(
                                    text = "No recent notices",
                                    color = secondaryTextColor,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                latestNoticesState.take(3).forEach { notice ->
                                    NoticeDashboardItem(
                                        notice = notice,
                                        isDarkMode = isDarkMode,
                                        textColor = textColor,
                                        secondaryTextColor = secondaryTextColor,
                                        cardColor = cardColor,
                                        borderColor = borderColor
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Recent Complaints Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Complaints",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "View All",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.clickable { selectedTab = "Complaints"; complaintsSubScreen = "list" }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Recent dynamic complaints
                            complaintsList.take(2).forEach { complaint ->
                                val categoryIcon = when (complaint.category) {
                                    "Plumbing" -> Icons.Outlined.WaterDrop
                                    "Electrical" -> Icons.Outlined.Bolt
                                    "Security" -> Icons.Outlined.Shield
                                    else -> Icons.Outlined.Assignment
                                }
                                val statusColor = when (complaint.status) {
                                    "In Progress" -> PrimaryBlue
                                    "Pending" -> Color(0xFFF59E0B)
                                    "Resolved" -> Color(0xFF22C55E)
                                    else -> secondaryTextColor
                                }
                                ComplaintItem(
                                    icon = categoryIcon,
                                    title = complaint.title,
                                    category = complaint.category,
                                    date = complaint.date,
                                    status = complaint.status,
                                    statusColor = statusColor,
                                    isDarkMode = isDarkMode,
                                    modifier = Modifier.clickable {
                                        selectedComplaintId = complaint.id
                                        complaintsSubScreen = "detail"
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Smart Maps Section
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Smart Maps",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "View Map",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryBlue,
                                    modifier = Modifier.clickable { uiStatePreview = "maps_screen" }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Maps Preview Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(cardColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(24.dp))
                                    .clickable { uiStatePreview = "maps_screen" }
                                    .padding(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Map,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Nearby Essentials",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Locate hospitals, pharmacies, and supermarkets near the society.",
                                            fontSize = 13.sp,
                                            color = secondaryTextColor,
                                            lineHeight = 18.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = secondaryTextColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                        } else if (selectedTab == "Complaints") {
                            var searchQuery by remember { mutableStateOf("") }
                            var selectedFilter by remember { mutableStateOf("All") }

                            Text(
                                text = "My Complaints",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Search Bar
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = inputBackgroundColor,
                                    unfocusedContainerColor = inputBackgroundColor,
                                    disabledContainerColor = inputBackgroundColor,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor
                                ),
                                placeholder = { Text("Search complaints...", color = secondaryTextColor) },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Search, contentDescription = null, tint = secondaryTextColor)
                                },
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Filters
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("All", "Pending", "In Progress", "Resolved", "High Priority").forEach { filter ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(if (selectedFilter == filter) PrimaryBlue else inputBackgroundColor)
                                            .clickable { selectedFilter = filter }
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = filter,
                                            color = if (selectedFilter == filter) Color.White else secondaryTextColor,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedFilter == filter) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Filtered List
                            val filteredList = complaintsList.filter { complaint ->
                                val matchesSearch = complaint.title.contains(searchQuery, ignoreCase = true) ||
                                        complaint.category.contains(searchQuery, ignoreCase = true) ||
                                        complaint.id.contains(searchQuery, ignoreCase = true)
                                val matchesFilter = when (selectedFilter) {
                                    "All" -> true
                                    "High Priority" -> complaint.priority == "High"
                                    else -> complaint.status == selectedFilter
                                }
                                matchesSearch && matchesFilter
                            }

                            if (isComplaintsLoading && complaintsList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryBlue)
                                }
                            } else if (complaintsError != null && complaintsList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(complaintsError ?: "Failed to sync", color = Color(0xFFEF4444))
                                }
                            } else if (filteredList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No complaints found", color = secondaryTextColor)
                                }
                            } else {
                                filteredList.forEach { complaint ->
                                    val categoryIcon = when (complaint.category) {
                                        "Plumbing" -> Icons.Outlined.WaterDrop
                                        "Electrical" -> Icons.Outlined.Bolt
                                        "Security" -> Icons.Outlined.Shield
                                        else -> Icons.Outlined.Assignment
                                    }
                                    val statusColor = when (complaint.status) {
                                        "In Progress" -> PrimaryBlue
                                        "Pending" -> Color(0xFFF59E0B)
                                        "Resolved" -> Color(0xFF22C55E)
                                        else -> secondaryTextColor
                                    }
                                    ComplaintItem(
                                        icon = categoryIcon,
                                        title = complaint.title,
                                        category = complaint.category,
                                        date = complaint.date,
                                        status = complaint.status,
                                        statusColor = statusColor,
                                        isDarkMode = isDarkMode,
                                        modifier = Modifier.clickable {
                                            selectedComplaintId = complaint.id
                                            complaintsSubScreen = "detail"
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }

                        } else if (selectedTab == "Profile") {
                            // Top Bar Row (Header + Edit Icon)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Profile",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                IconButton(
                                    onClick = {
                                        profileSubScreenBackTo = "Profile"
                                        profileSubScreen = "edit"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = textColor
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Avatar & Info
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar with verified badge
                                Box(modifier = Modifier.size(100.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, PrimaryBlue, CircleShape)
                                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedAvatarBitmap != null) {
                                            Image(
                                                bitmap = selectedAvatarBitmap!!.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = avatarInitials,
                                                fontSize = 36.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )
                                        }
                                    }
                                    
                                    // Verified badge
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .align(Alignment.BottomEnd)
                                            .offset(x = (-4).dp, y = (-4).dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981)) // Green
                                            .border(2.dp, backgroundColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(text = residentName.capitalizeWords(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = textColor)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Apartment $residentFlat · $residentFlatType", fontSize = 14.sp, color = secondaryTextColor)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Member since Jan 2024", fontSize = 12.sp, color = textColor.copy(alpha = 0.4f))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Stats Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Raised Card
                                val totalCount = complaintsList.size
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(cardColor)
                                        .padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.Assignment, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = totalCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(text = "Complaints Raised", fontSize = 12.sp, color = secondaryTextColor)
                                }
                                
                                // Resolved Card
                                val resolvedCount = complaintsList.count { it.status == "Resolved" }
                                val greenColor = Color(0xFF10B981)
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(cardColor)
                                        .padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(greenColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = greenColor, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(text = resolvedCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = greenColor)
                                    Text(text = "Resolved", fontSize = 12.sp, color = secondaryTextColor)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Verified Documents Card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(cardColor)
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "VERIFIED DOCUMENTS",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = secondaryTextColor,
                                        letterSpacing = 0.5.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(inputBackgroundColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Assignment,
                                            contentDescription = null,
                                            tint = PrimaryBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Aadhaar Card", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                                        Text(text = "Verified on Jan 15, 2024", fontSize = 12.sp, color = secondaryTextColor)
                                    }
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Recent Activity Preview List
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(cardColor)
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "RECENT ACTIVITY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryTextColor,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Activity 1
                                val latestComplaintId = complaintsList.firstOrNull()?.id ?: "#AL-2026-0091"
                                ActivityItem(
                                    title = "Raised complaint",
                                    subtitle = latestComplaintId,
                                    time = "2h ago",
                                    icon = Icons.Outlined.Assignment,
                                    iconTint = PrimaryBlue,
                                    backgroundColor = inputBackgroundColor
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Activity 2
                                ActivityItem(
                                    title = "Complaint resolved",
                                    subtitle = "#AL-2026-0031",
                                    time = "May 18",
                                    icon = Icons.Outlined.CheckCircle,
                                    iconTint = Color(0xFF10B981),
                                    backgroundColor = inputBackgroundColor
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Activity 3
                                ActivityItem(
                                    title = "Updated profile",
                                    subtitle = "Phone: $residentPhone",
                                    time = "May 10",
                                    icon = Icons.Outlined.Person,
                                    iconTint = Color(0xFFF59E0B),
                                    backgroundColor = inputBackgroundColor
                                )
                            }
                        } else if (selectedTab == "Settings") {
                            Text(
                                text = "Settings",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Account Group
                            Text(
                                text = "ACCOUNT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryTextColor,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            ProfileMenuItem(
                                icon = Icons.Outlined.Person,
                                text = "Edit Profile",
                                textColor = textColor,
                                containerColor = cardColor,
                                onClick = {
                                    profileSubScreenBackTo = "Settings"
                                    profileSubScreen = "edit"
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileMenuItem(
                                icon = Icons.Outlined.Lock,
                                text = "Change Password",
                                textColor = textColor,
                                containerColor = cardColor,
                                onClick = {
                                    profileSubScreenBackTo = "Settings"
                                    profileSubScreen = "change_password"
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Notifications Group
                            Text(
                                text = "PREFERENCES & SECURITY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryTextColor,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Push notifications toggle
                            SettingsToggleItem(
                                icon = Icons.Outlined.Notifications,
                                text = "Push Notifications",
                                checked = pushNotifications,
                                onCheckedChange = { pushNotifications = it },
                                cardColor = cardColor,
                                textColor = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Email alerts toggle
                            SettingsToggleItem(
                                icon = Icons.Outlined.Email,
                                text = "Email Alerts",
                                checked = emailAlerts,
                                onCheckedChange = { emailAlerts = it },
                                cardColor = cardColor,
                                textColor = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Biometric login toggle
                            SettingsToggleItem(
                                icon = Icons.Outlined.Fingerprint,
                                text = "Biometric Login",
                                checked = biometricLogin,
                                onCheckedChange = { biometricLogin = it },
                                cardColor = cardColor,
                                textColor = textColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Dark Mode Toggle
                            SettingsToggleItem(
                                icon = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                                text = "Dark Mode",
                                checked = isDarkMode,
                                onCheckedChange = { onThemeToggle() },
                                cardColor = cardColor,
                                textColor = textColor
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Support Group
                            Text(
                                text = "SUPPORT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = secondaryTextColor,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            ProfileMenuItem(
                                icon = Icons.Default.Star,
                                text = "Rate this App",
                                textColor = textColor,
                                containerColor = cardColor,
                                onClick = { /* simulated action */ }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileMenuItem(
                                icon = Icons.Outlined.Warning,
                                text = "Report a Bug",
                                textColor = textColor,
                                containerColor = cardColor,
                                onClick = { /* simulated action */ }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            ProfileMenuItem(
                                icon = Icons.Outlined.Info,
                                text = "About Appartment Living",
                                textColor = textColor,
                                containerColor = cardColor,
                                onClick = onAboutClick
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // Log Out Button
                            val redColor = Color(0xFFEF4444)
                            ProfileMenuItem(
                                icon = Icons.Default.ExitToApp,
                                text = "Log Out",
                                textColor = redColor,
                                containerColor = redColor.copy(alpha = 0.1f),
                                onClick = { showLogoutConfirm = true },
                                showChevron = false
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // App Version Centered Footer
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Apartment Living v2.4.1",
                                    fontSize = 12.sp,
                                    color = secondaryTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }


            // Logout Confirmation Sheet Overlay (S40)
            if (showLogoutConfirm) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable { showLogoutConfirm = false }
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                            .background(cardColor)
                            .clickable(enabled = false) {}
                            .padding(24.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Drag Handle
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(textColor.copy(alpha = 0.15f))
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))

                            // Red Exit Warning Icon
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Log Out?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Are you sure you want to log out of Apartment Living?",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Action Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = { showLogoutConfirm = false },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .border(1.dp, borderColor, RoundedCornerShape(25.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = textColor
                                    ),
                                    shape = RoundedCornerShape(25.dp)
                                ) {
                                    Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        showLogoutConfirm = false
                                        onLogout()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFEF4444),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(25.dp)
                                ) {
                                    Text("Log out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewComplaintScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onSubmit: (Complaint) -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFFFFFFF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)
    val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0)

    var selectedCategory by remember { mutableStateOf("Plumbing") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("High") }
    var photoAttached by remember { mutableStateOf(false) }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photoAttached = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoAttached = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Complaint",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Category
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "Plumbing" to Icons.Outlined.WaterDrop,
                            "Electrical" to Icons.Outlined.Bolt
                        ).forEach { (category, icon) ->
                            val isSelected = selectedCategory == category
                            val chipBg = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else inputBackgroundColor
                            val chipBorderColor = if (isSelected) PrimaryBlue else borderColor

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorderColor, RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) PrimaryBlue else secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    color = if (isSelected) PrimaryBlue else textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(
                            "Security" to Icons.Outlined.Shield,
                            "Others" to Icons.Outlined.Info
                        ).forEach { (category, icon) ->
                            val isSelected = selectedCategory == category
                            val chipBg = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else inputBackgroundColor
                            val chipBorderColor = if (isSelected) PrimaryBlue else borderColor

                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorderColor, RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = category }
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) PrimaryBlue else secondaryTextColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category,
                                    color = if (isSelected) PrimaryBlue else textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Title
                Text(
                    text = "Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Brief description of the issue", color = secondaryTextColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor,
                        unfocusedContainerColor = inputBackgroundColor,
                        disabledContainerColor = inputBackgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    singleLine = true
                )

                // Description
                Text(
                    text = "Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(inputBackgroundColor)
                ) {
                    TextField(
                        value = description,
                        onValueChange = { if (it.length <= 500) description = it },
                        placeholder = { Text("Provide details about the issue...", color = secondaryTextColor) },
                        modifier = Modifier.fillMaxSize(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )

                    Text(
                        text = "${description.length}/500",
                        color = secondaryTextColor,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Priority
                Text(
                    text = "Priority",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("Low", "Medium", "High").forEach { priority ->
                        val isSelected = selectedPriority == priority
                        val buttonBg = if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else inputBackgroundColor
                        val buttonBorderColor = if (isSelected) PrimaryBlue else borderColor

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(buttonBg)
                                .border(1.dp, buttonBorderColor, RoundedCornerShape(12.dp))
                                .clickable { selectedPriority = priority },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = priority,
                                color = if (isSelected) PrimaryBlue else textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Attach Photo Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (photoAttached) PrimaryBlue.copy(alpha = 0.05f) else Color.Transparent)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                            drawRoundRect(
                                color = if (photoAttached) PrimaryBlue else borderColor,
                                style = stroke,
                                cornerRadius = CornerRadius(12.dp.toPx())
                            )
                        }
                        .clickable { showPhotoOptionsDialog = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoAttached) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "photo_attached.jpg",
                                color = PrimaryBlue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = textColor.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { photoAttached = false }
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = secondaryTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Attach photo",
                                color = secondaryTextColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Bottom Actions (Divider + Row)
            HorizontalDivider(color = borderColor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(26.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            showConfirmDialog = true
                        }
                    },
                    enabled = title.isNotEmpty(),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text("Submit Complaint", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Confirmation Sheet Overlay
        if (showConfirmDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { showConfirmDialog = false }
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(cardColor)
                        .clickable(enabled = false) {}
                        .padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Drag Handle
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(textColor.copy(alpha = 0.15f))
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Confirm Submission?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Please review your complaint before submitting.",
                            fontSize = 14.sp,
                            color = secondaryTextColor
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Summary Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(inputBackgroundColor)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COMPLAINT SUMMARY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                    )
                                    Text(
                                        text = "Pending",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$selectedCategory • $selectedPriority Priority",
                                fontSize = 13.sp,
                                color = secondaryTextColor
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = textColor.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Est. response time: ",
                                    fontSize = 13.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "Within 4 hours",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Warning Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF59E0B).copy(alpha = 0.05f))
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Once submitted, you can track the status but cannot edit the complaint.",
                                    fontSize = 13.sp,
                                    color = Color(0xFFF59E0B),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { showConfirmDialog = false },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(25.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = textColor
                                ),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("Go Back", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    showConfirmDialog = false
                                    val randomId = "#AL-2026-${(1000..9999).random()}"
                                    val todayStr = "May 22, 2026"
                                    val newComplaint = Complaint(
                                        id = randomId,
                                        title = title,
                                        category = selectedCategory,
                                        date = todayStr,
                                        status = "Pending",
                                        priority = selectedPriority,
                                        description = description,
                                        timeline = listOf(
                                            TimelineStep("Raised", "May 22, 8:00 AM", "active"),
                                            TimelineStep("Assigned", "Pending", "pending"),
                                            TimelineStep("In Progress", "Pending", "pending"),
                                            TimelineStep("Resolved", "Pending", "pending")
                                        ),
                                        photoAttached = photoAttached
                                    )
                                    onSubmit(newComplaint)
                                },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text("Confirm & Submit", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showPhotoOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showPhotoOptionsDialog = false },
                title = {
                    Text(
                        text = "Attach Photo",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                text = {
                    Text(
                        text = "Choose how you want to attach a photo to your complaint:",
                        color = secondaryTextColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPhotoOptionsDialog = false
                            try {
                                cameraLauncher.launch(null)
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Camera launch error", e)
                            }
                        }
                    ) {
                        Text("Take Photo", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPhotoOptionsDialog = false
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Gallery launch error", e)
                            }
                        }
                    ) {
                        Text("Choose from Media", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = cardColor
            )
        }
    }
}

@Composable
fun ComplaintDetailScreen(
    complaint: Complaint,
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFFFFFFF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)
    val accentColor = PrimaryBlue

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Complaint",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            IconButton(onClick = { /* Options Menu */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = textColor
                )
            }
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categoryIcon = when (complaint.category) {
                                "Plumbing" -> Icons.Outlined.WaterDrop
                                "Electrical" -> Icons.Outlined.Bolt
                                "Security" -> Icons.Outlined.Shield
                                else -> Icons.Outlined.Assignment
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = complaint.category,
                                fontSize = 13.sp,
                                color = secondaryTextColor
                            )
                        }

                        val statusColor = when (complaint.status) {
                            "In Progress" -> PrimaryBlue
                            "Pending" -> Color(0xFFF59E0B)
                            "Resolved" -> Color(0xFF22C55E)
                            else -> secondaryTextColor
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(statusColor.copy(alpha = 0.10f))
                                .border(1.dp, statusColor.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = complaint.status,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = complaint.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = complaint.id,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = secondaryTextColor
                        )
                        Text(text = "•", color = secondaryTextColor)
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = complaint.date,
                            fontSize = 12.sp,
                            color = secondaryTextColor
                        )
                    }
                }
            }

            // Description Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "DESCRIPTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = complaint.description,
                        fontSize = 15.sp,
                        color = textColor,
                        lineHeight = 22.sp
                    )

                    if (complaint.photoAttached) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(inputBackgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "complaint_photo.jpg",
                                    color = secondaryTextColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Timeline Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "STATUS TIMELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    complaint.timeline.forEachIndexed { index, step ->
                        val nextStepStatus = if (index < complaint.timeline.lastIndex) complaint.timeline[index + 1].status else "pending"
                        val lineActive = step.status == "done" && (nextStepStatus == "done" || nextStepStatus == "active")
                        val lineColor = if (lineActive) accentColor else textColor.copy(alpha = 0.1f)

                        TimelineItem(
                            label = step.label,
                            date = step.date,
                            status = step.status,
                            isLast = index == complaint.timeline.lastIndex,
                            lineColor = lineColor
                        )
                    }
                }
            }

            // Admin Update Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardColor)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "ADMIN UPDATES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (complaint.adminComment != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = complaint.adminComment.initials,
                                    color = accentColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Message Bubble
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(inputBackgroundColor)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = complaint.adminComment.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = complaint.adminComment.timeAgo,
                                        fontSize = 11.sp,
                                        color = secondaryTextColor
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = complaint.adminComment.comment,
                                    fontSize = 13.sp,
                                    color = secondaryTextColor,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No updates from administrator yet.",
                            fontSize = 14.sp,
                            color = secondaryTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    label: String,
    date: String,
    status: String, // "done", "active", "pending"
    isLast: Boolean,
    lineColor: Color
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val accentColor = PrimaryBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Left Column Node & line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (status == "done") accentColor else MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 2.dp,
                        color = if (status == "done" || status == "active") accentColor else textColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (status == "done") {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else if (status == "active") {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(lineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (status == "pending") textColor.copy(alpha = 0.4f) else textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                fontSize = 12.sp,
                color = secondaryTextColor
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
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
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = secondaryTextColor)
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    
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
            tint = if (isSelected) PrimaryBlue else secondaryTextColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PrimaryBlue else secondaryTextColor,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun ComplaintItem(
    icon: ImageVector,
    title: String,
    category: String,
    date: String,
    status: String,
    statusColor: Color,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val cardColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFFFFFFF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(inputBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = secondaryTextColor)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$category  •  $date",
                    color = secondaryTextColor,
                    fontSize = 12.sp
                )
                
                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Helpers & Sub-screens for Profile & Settings Redesign

fun String.capitalizeWords(): String {
    return this.split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

fun loadBitmapFromUri(context: android.content.Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            android.graphics.BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getInitials(name: String): String {
    if (name.isBlank()) return "RS"
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull() ?: ""}${parts[1].firstOrNull() ?: ""}".uppercase()
        parts.size == 1 -> "${parts[0].firstOrNull() ?: ""}".uppercase()
        else -> "RS"
    }
}

@Composable
fun ActivityItem(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    iconTint: Color,
    backgroundColor: Color
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = subtitle, fontSize = 12.sp, color = secondaryTextColor)
        }
        Text(text = time, fontSize = 12.sp, color = secondaryTextColor)
    }
}

@Composable
fun SettingsToggleItem(
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
fun EditProfileScreen(
    isDarkMode: Boolean,
    initialName: String,
    initialEmail: String,
    initialPhone: String,
    initialFlat: String,
    initialAvatarBitmap: android.graphics.Bitmap?,
    onDiscard: () -> Unit,
    onSave: (name: String, email: String, phone: String, flat: String, avatarBitmap: android.graphics.Bitmap?) -> Unit
) {
    var editName by remember { mutableStateOf(initialName) }
    var editEmail by remember { mutableStateOf(initialEmail) }
    var editPhone by remember { mutableStateOf(initialPhone) }
    var editFlat by remember { mutableStateOf(initialFlat) }

    val context = LocalContext.current
    var editAvatarBitmap by remember { mutableStateOf(initialAvatarBitmap) }
    var showAvatarOptionsDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            editAvatarBitmap = bitmap
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            editAvatarBitmap = loadBitmapFromUri(context, uri)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFFFFFFF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)
    val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0)

    val hasChanges = editName != initialName || editEmail != initialEmail || editPhone != initialPhone || editFlat != initialFlat || editAvatarBitmap != initialAvatarBitmap

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDiscard) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        // Warning Banner for Unsaved Changes
        if (hasChanges) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF3C7)) // Amber light
                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "You have unsaved changes. Save or discard before exiting.",
                        color = Color(0xFFD97706),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Scrollable Form
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Circle Avatar XL with camera overlay icon
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(110.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(3.dp, PrimaryBlue, CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f))
                        .clickable { showAvatarOptionsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (editAvatarBitmap != null) {
                        Image(
                            bitmap = editAvatarBitmap!!.asImageBitmap(),
                            contentDescription = "Avatar Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (editName.isNotBlank()) editName.first().toString().uppercase() else "R",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }

                // Camera overlay button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                        .border(2.dp, backgroundColor, CircleShape)
                        .clickable { showAvatarOptionsDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = "Edit photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Full Name Input
            Text(
                text = "Full Name",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = editName,
                onValueChange = { editName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Person, contentDescription = null, tint = secondaryTextColor)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email Address Input
            Text(
                text = "Email Address",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = editEmail,
                onValueChange = { editEmail = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Email, contentDescription = null, tint = secondaryTextColor)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Phone Number Input
            Text(
                text = "Phone Number",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = editPhone,
                onValueChange = { editPhone = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Phone, contentDescription = null, tint = secondaryTextColor)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Apartment Flat Number Input
            Text(
                text = "Apartment No.",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = editFlat,
                onValueChange = { editFlat = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Home, contentDescription = null, tint = secondaryTextColor)
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Sticky Footer Actions
        HorizontalDivider(color = borderColor)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onDiscard,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .border(1.dp, borderColor, RoundedCornerShape(26.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = textColor
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Discard", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    onSave(editName, editEmail, editPhone, editFlat, editAvatarBitmap)
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (showAvatarOptionsDialog) {
            AlertDialog(
                onDismissRequest = { showAvatarOptionsDialog = false },
                title = {
                    Text(
                        text = "Change Avatar",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                },
                text = {
                    Text(
                        text = "Choose how you want to update your avatar photo:",
                        color = secondaryTextColor
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showAvatarOptionsDialog = false
                            try {
                                cameraLauncher.launch(null)
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Camera launch error", e)
                            }
                        }
                    ) {
                        Text("Take Photo", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAvatarOptionsDialog = false
                            try {
                                galleryLauncher.launch("image/*")
                            } catch (e: Exception) {
                                android.util.Log.e("HomeScreen", "Gallery launch error", e)
                            }
                        }
                    ) {
                        Text("Choose from Media", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = cardColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSubScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onPasswordUpdated: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardColor = if (isDarkMode) Color(0xFF161616) else Color(0xFFFFFFFF)
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF262626) else Color(0xFFF1F5F9)
    val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0)

    // Password validation logic
    val hasMinLength = newPassword.length >= 8
    val hasNumber = newPassword.any { it.isDigit() }
    val hasUppercase = newPassword.any { it.isUpperCase() }
    val hasSpecial = newPassword.any { !it.isLetterOrDigit() }

    val rulesMetCount = (if (hasMinLength) 1 else 0) +
            (if (hasNumber) 1 else 0) +
            (if (hasUppercase) 1 else 0) +
            (if (hasSpecial) 1 else 0)

    val strengthText = when (rulesMetCount) {
        0 -> "Very Weak"
        1 -> "Weak"
        2 -> "Medium"
        3 -> "Strong"
        4 -> "Strong"
        else -> ""
    }

    val strengthColor = when (rulesMetCount) {
        0, 1 -> Color(0xFFEF4444) // Red
        2 -> Color(0xFFF59E0B) // Orange
        else -> Color(0xFF10B981) // Green
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Change Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Lock Logo Banner & Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardColor)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Update your password",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter your current password and choose a new strong one.",
                    fontSize = 13.sp,
                    color = secondaryTextColor,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Current Password Input
            Text(
                text = "Current Password",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = secondaryTextColor)
                },
                trailingIcon = {
                    IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                        Icon(
                            imageVector = if (currentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = secondaryTextColor
                        )
                    }
                },
                visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // New Password Input
            Text(
                text = "New Password",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = secondaryTextColor)
                },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = secondaryTextColor
                        )
                    }
                },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            // Password Strength Indicator Slots & Checklist
            if (newPassword.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Password Strength",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = strengthText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = strengthColor
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strength Progress Slots (4 segments)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..4) {
                            val active = rulesMetCount >= i
                            val color = if (active) strengthColor else textColor.copy(alpha = 0.05f)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rules Checklist
                    PasswordRuleRow(label = "At least 8 characters", met = hasMinLength, isDarkMode = isDarkMode)
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordRuleRow(label = "Contains a number", met = hasNumber, isDarkMode = isDarkMode)
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordRuleRow(label = "Contains uppercase letter", met = hasUppercase, isDarkMode = isDarkMode)
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordRuleRow(label = "Contains special character", met = hasSpecial, isDarkMode = isDarkMode)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Password Input
            Text(
                text = "Confirm Password",
                color = secondaryTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            TextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                ),
                leadingIcon = {
                    Icon(Icons.Outlined.Lock, contentDescription = null, tint = secondaryTextColor)
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = secondaryTextColor
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Sticky Footer Action
        val passwordsMatch = newPassword == confirmPassword && newPassword.isNotEmpty()
        val updateEnabled = currentPassword.isNotEmpty() && rulesMetCount == 4 && passwordsMatch

        HorizontalDivider(color = borderColor)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = onPasswordUpdated,
                enabled = updateEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Text("Update Password", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PasswordRuleRow(
    label: String,
    met: Boolean,
    isDarkMode: Boolean
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (met) Color(0xFF10B981) else secondaryTextColor.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = if (met) textColor else secondaryTextColor
        )
    }
}

// ==========================================
// PHASE 9 UI STATES SHOWCASE
// ==========================================

enum class ToastType {
    SUCCESS, ERROR, INFO
}

@Composable
fun ToastComponent(
    title: String,
    subtitle: String,
    type: ToastType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle
        ToastType.ERROR -> Icons.Default.Cancel
        ToastType.INFO -> Icons.Default.Info
    }
    val iconColor = when (type) {
        ToastType.SUCCESS -> Color(0xFF10B981) // Green
        ToastType.ERROR -> Color(0xFFEF4444)   // Red
        ToastType.INFO -> Color(0xFF3B82F6)    // Blue
    }
    val iconBg = iconColor.copy(alpha = 0.15f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF262626), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Text Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SkeletonLine(
    widthFraction: Float,
    height: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.Gray.copy(alpha = alpha))
    )
}

@Composable
fun SkeletonCircle(
    size: Dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = alpha))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkeletonLoadingScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)

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
                text = "Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile details skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(size = 56.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonLine(widthFraction = 0.4f, height = 12.dp)
                SkeletonLine(widthFraction = 0.7f, height = 16.dp)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Grid cards skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        SkeletonCircle(size = 32.dp)
                        SkeletonLine(widthFraction = 0.8f, height = 12.dp)
                        SkeletonLine(widthFraction = 0.5f, height = 10.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        SkeletonLine(widthFraction = 0.35f, height = 16.dp)

        Spacer(modifier = Modifier.height(16.dp))

        // Vertical cards skeleton
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.Gray.copy(alpha = 0.25f))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            SkeletonLine(widthFraction = 0.85f, height = 14.dp)
                            SkeletonLine(widthFraction = 0.6f, height = 10.dp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading your dashboard...",
                fontSize = 14.sp,
                color = secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ServerLightningIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(56.dp)) {
        val w = size.width
        val h = size.height
        val redColor = Color(0xFFEF4444)
        val stroke = 3.dp.toPx()

        // Top server rack node (rounded rect)
        drawRoundRect(
            color = redColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.24f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.22f),
            cornerRadius = CornerRadius(6.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Top server LED dot
        drawCircle(
            color = redColor,
            radius = 2.5f.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.35f)
        )

        // Bottom server rack node (rounded rect)
        drawRoundRect(
            color = redColor,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.54f),
            size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.22f),
            cornerRadius = CornerRadius(6.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Bottom server LED dot
        drawCircle(
            color = redColor,
            radius = 2.5f.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.65f)
        )

        // Lightning bolt path in the middle
        val boltPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.58f, h * 0.12f)
            lineTo(w * 0.40f, h * 0.52f)
            lineTo(w * 0.54f, h * 0.52f)
            lineTo(w * 0.44f, h * 0.88f)
            lineTo(w * 0.62f, h * 0.46f)
            lineTo(w * 0.48f, h * 0.46f)
            close()
        }

        // Draw lightning bolt
        drawPath(
            path = boltPath,
            color = redColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SomethingWentWrongScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onTryAgain: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val circleBgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF1F5F9)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Bar Back Button
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
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular container with custom server rack icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(circleBgColor)
                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                ServerLightningIcon()
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Something went wrong",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "We couldn't load your data. Please check your connection and try again.",
                fontSize = 15.sp,
                color = secondaryTextColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Error code: 500_INT_SERVER",
                fontSize = 12.sp,
                color = secondaryTextColor.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Action Button
            Button(
                onClick = onTryAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Try Again", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Report issue link
            Text(
                text = "Report this issue",
                color = PrimaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { /* action */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YouAreOfflineScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onRetryNow: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val circleBgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF1F5F9)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Back Button
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
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Slashed Wifi Icon inside circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(circleBgColor)
                    .border(1.dp, textColor.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = secondaryTextColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "You're offline",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Please check your internet connection. We'll automatically retry every few seconds.",
                fontSize = 15.sp,
                color = secondaryTextColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Retrying Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardColor)
                    .border(1.dp, textColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Retrying in 5s...",
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Cached Data Banner Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Showing cached data",
                            color = textColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Last synced 8 minutes ago",
                            color = secondaryTextColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Retry Now Outline Button
            Button(
                onClick = onRetryNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .border(1.dp, PrimaryBlue, RoundedCornerShape(26.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry Now", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsEmptyScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val circleBgColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFF1F5F9)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)

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
                text = "Notifications",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Bell Off icon inside glowing circular container
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(circleBgColor)
                    .border(1.dp, textColor.copy(alpha = 0.05f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "All caught up!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "You have no new notifications. We'll let you know when something needs your attention.",
                fontSize = 15.sp,
                color = secondaryTextColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Subscribe Button
            Button(
                onClick = { /* simulated action */ },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(48.dp)
                    .border(1.dp, PrimaryBlue, RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subscribe to alerts", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToastVariantsScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)

    var showSuccess by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(true) }
    var showInfo by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    text = "Toast Variants",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card skeleton background list
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardColor.copy(alpha = 0.3f))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Interactive toggles
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Interactive Controls",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showSuccess = !showSuccess },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showSuccess) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.2f),
                                contentColor = if (showSuccess) Color.White else textColor
                            )
                        ) {
                            Text("Success", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showError = !showError },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showError) Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.2f),
                                contentColor = if (showError) Color.White else textColor
                            )
                        ) {
                            Text("Error", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { showInfo = !showInfo },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showInfo) Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.2f),
                                contentColor = if (showInfo) Color.White else textColor
                            )
                        ) {
                            Text("Info", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Stacked Toasts
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showSuccess) {
                    ToastComponent(
                        title = "Complaint submitted successfully",
                        subtitle = "#AL-2024-0091",
                        type = ToastType.SUCCESS,
                        onDismiss = { showSuccess = false }
                    )
                }
                if (showError) {
                    ToastComponent(
                        title = "Failed to upload photo",
                        subtitle = "File size too large",
                        type = ToastType.ERROR,
                        onDismiss = { showError = false }
                    )
                }
                if (showInfo) {
                    ToastComponent(
                        title = "Status update available",
                        subtitle = "Tap to view",
                        type = ToastType.INFO,
                        onDismiss = { showInfo = false }
                    )
                }
            }
        }
    }
}

// ==========================================
// PHASE 10 ADVANCED: RESIDENT & DESIGN SCREENS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    noticeViewModel: com.simats.appartmentliving.ui.viewmodels.NoticeViewModel
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0)

    val noticesList by noticeViewModel.notices.collectAsState(initial = emptyList())
    val isLoading by noticeViewModel.isLoading.collectAsState(initial = false)
    val error by noticeViewModel.error.collectAsState(initial = null)

    var sortByLatest by remember { mutableStateOf(true) }

    val sortedList = remember(noticesList, sortByLatest) {
        if (sortByLatest) {
            noticesList.sortedByDescending { it.createdAt ?: "" }
        } else {
            noticesList.sortedWith(compareBy {
                when (it.priority.lowercase()) {
                    "high" -> 0
                    "medium" -> 1
                    else -> 2
                }
            })
        }
    }

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
                text = "Notices & Announcements",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            IconButton(onClick = { noticeViewModel.fetchNotices() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = textColor
                )
            }
        }

        // Sorting toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sortedList.size} Announcements",
                fontSize = 13.sp,
                color = secondaryTextColor,
                fontWeight = FontWeight.Medium
            )
            
            // Toggle sorting button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(textColor.copy(alpha = 0.05f))
                    .clickable { sortByLatest = !sortByLatest }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (sortByLatest) "Sort: Latest" else "Sort: Priority",
                    fontSize = 12.sp,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = error ?: "Failed to sync", color = Color(0xFFEF4444))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { noticeViewModel.fetchNotices() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Try Again", color = Color.White)
                    }
                }
            }
        } else if (sortedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No announcements found", color = secondaryTextColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                sortedList.forEach { notice ->
                    NoticeDashboardItem(
                        notice = notice,
                        isDarkMode = isDarkMode,
                        textColor = textColor,
                        secondaryTextColor = secondaryTextColor,
                        cardColor = cardColor,
                        borderColor = borderColor
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenancePaymentScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit
) {
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)

    var isPaid by remember { mutableStateOf(false) }
    var showPaymentSuccessToast by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    text = "Maintenance",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Due Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryBlue.copy(alpha = 0.08f), cardColor)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "DUE THIS MONTH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = if (isPaid) "₹0" else "₹4,500",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/ month",
                                fontSize = 14.sp,
                                color = secondaryTextColor,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isPaid) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Text("Paid for May 2026", color = Color(0xFF10B981), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Text("Due May 25, 2026 · 4 days left", color = Color(0xFFF59E0B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Late fee of ₹50/day applies after due date",
                            fontSize = 11.sp,
                            color = secondaryTextColor.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                if (!isPaid) {
                                    isPaid = true
                                    showPaymentSuccessToast = true
                                }
                            },
                            enabled = !isPaid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPaid) Color.Gray.copy(alpha = 0.15f) else PrimaryBlue
                            )
                        ) {
                            Text(
                                text = if (isPaid) "Paid" else "Pay Now · ₹4,500",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) secondaryTextColor else Color.White
                            )
                        }
                    }
                }

                // Breakdown Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryTextColor,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        listOf(
                            Pair("Maintenance Fee", "₹3,500"),
                            Pair("Water Charges", "₹500"),
                            Pair("Common Area", "₹500")
                        ).forEach { (label, amount) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = 14.sp, color = secondaryTextColor)
                                Text(amount, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = textColor)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textColor)
                            Text("₹4,500", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }
                }

                // Payment History
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Payment History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                        Text(
                            text = "Receipts",
                            fontSize = 13.sp,
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { /* simulated */ }
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column {
                            val history = listOf(
                                Triple("April 2026", "₹4,500", true),
                                Triple("March 2026", "₹4,500", true),
                                Triple("February 2026", "₹4,500", true),
                                Triple("January 2026", "₹4,500", true)
                            )
                            history.forEachIndexed { index, (month, amount, paid) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(month, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                                        Text(amount, fontSize = 12.sp, color = secondaryTextColor)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                            Text("Paid", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (index != history.size - 1) {
                                    HorizontalDivider(color = dividerColor)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Success Toast popup
        if (showPaymentSuccessToast) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                ToastComponent(
                    title = "Payment submitted successfully",
                    subtitle = "Transaction ID: #TXN-2026-9041",
                    type = ToastType.SUCCESS,
                    onDismiss = { showPaymentSuccessToast = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSummaryScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit
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
                text = "Design Summary",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Logo Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryBlue)
                            .drawBehind { /* glow */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Apartment Living", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text("Design System Overview", fontSize = 13.sp, color = secondaryTextColor)
                }
            }

            // Stats grid
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Pair("50", "Screens"),
                    Pair("11", "Components"),
                    Pair("10", "Phases")
                ).forEach { (value, label) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardColor)
                            .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            Text(label, fontSize = 11.sp, color = secondaryTextColor)
                        }
                    }
                }
            }

            // Colors Block
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("COLORS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            Pair(PrimaryBlue, "Accent"),
                            Pair(Color(0xFF22C55E), "Success"),
                            Pair(Color(0xFFF59E0B), "Warning"),
                            Pair(Color(0xFFEF4444), "Error"),
                            Pair(Color(0xFF1C1C1E), "Card")
                        ).forEach { (color, name) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color)
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                )
                                Text(name, fontSize = 10.sp, color = secondaryTextColor)
                            }
                        }
                    }
                }
            }

            // All 50 Screens Grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ALL 50 SCREENS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple responsive grid representation of 50 screen blocks (5 columns x 10 rows)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(10) { rowIdx ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { colIdx ->
                                    val screenNum = rowIdx * 5 + colIdx + 1
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(9f / 16f)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(textColor.copy(alpha = 0.03f))
                                            .border(0.5.dp, dividerColor, RoundedCornerShape(2.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Miniature wireframe details
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(2.dp)
                                        ) {
                                            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(textColor.copy(alpha = 0.05f)))
                                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(textColor.copy(alpha = 0.05f)).padding(top = 3.dp))
                                            Box(modifier = Modifier.fillMaxWidth(0.6f).height(1.dp).background(textColor.copy(alpha = 0.05f)).padding(top = 5.dp))
                                            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(3.dp).background(PrimaryBlue.copy(alpha = 0.2f)))
                                            Text(
                                                text = "S${screenNum.toString().padStart(2, '0')}",
                                                fontSize = 7.sp,
                                                color = secondaryTextColor.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // User Flow Diagram
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("USER FLOW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair("Splash → Onboarding", false),
                            Pair("Login → Dashboard", true),
                            Pair("Dashboard → Raise", false),
                            Pair("Raise → Track", true),
                            Pair("Track → Resolved", false)
                        ).forEach { (label, isAccent) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isAccent) PrimaryBlue else secondaryTextColor.copy(alpha = 0.4f))
                                )
                                Text(label, fontSize = 13.sp, color = textColor)
                            }
                        }
                    }
                }
            }

            Text(
                text = "Designed with care · Apartment Living © 2026",
                fontSize = 11.sp,
                color = secondaryTextColor.copy(alpha = 0.6f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
    }
}

// ==========================================
// PHASE 11: RESIDENT PAYMENTS
// ==========================================

@Composable
fun QRCodeComponent(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 100.dp,
    color: Color = Color.Black
) {
    Canvas(
        modifier = modifier
            .size(sizeDp)
            .background(Color.White)
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height
        
        // Let's draw the 3 large corner markers
        val markerSize = w * 0.25f
        
        fun drawMarker(x: Float, y: Float) {
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(markerSize, markerSize),
                style = Stroke(width = w * 0.03f)
            )
            val innerSize = markerSize * 0.5f
            val offset = (markerSize - innerSize) / 2
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x + offset, y + offset),
                size = androidx.compose.ui.geometry.Size(innerSize, innerSize)
            )
        }
        
        drawMarker(0f, 0f)
        drawMarker(w - markerSize, 0f)
        drawMarker(0f, h - markerSize)
        
        val gridCount = 15
        val cellSize = w / gridCount
        val random = java.util.Random(42)
        
        for (row in 0 until gridCount) {
            for (col in 0 until gridCount) {
                val isTopLeft = row < 5 && col < 5
                val isTopRight = row < 5 && col >= gridCount - 5
                val isBottomLeft = row >= gridCount - 5 && col < 5
                
                if (!isTopLeft && !isTopRight && !isBottomLeft) {
                    if (random.nextBoolean()) {
                        drawRect(
                            color = color,
                            topLeft = androidx.compose.ui.geometry.Offset(col * cellSize, row * cellSize),
                            size = androidx.compose.ui.geometry.Size(cellSize, cellSize)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentResidentBillScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onPay: () -> Unit,
    activeBill: com.simats.appartmentliving.data.PaymentDto? = null,
    allBills: List<com.simats.appartmentliving.data.PaymentDto> = emptyList()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)

    var subTab by remember { mutableStateOf("Current") }
    var selectedHistoryBill by remember { mutableStateOf<com.simats.appartmentliving.data.PaymentDto?>(null) }

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
                    text = "Bills & Payments",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                )
                IconButton(onClick = {
                    android.widget.Toast.makeText(context, "Receipt Downloaded", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Download",
                        tint = textColor
                    )
                }
            }

            // Sub Tab Selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (subTab == "Current") PrimaryBlue else cardColor)
                        .border(1.dp, if (subTab == "Current") Color.Transparent else textColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable { subTab = "Current" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Current Bill",
                        color = if (subTab == "Current") Color.White else textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (subTab == "History") PrimaryBlue else cardColor)
                        .border(1.dp, if (subTab == "History") Color.Transparent else textColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable { subTab = "History" }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Payment History",
                        color = if (subTab == "History") Color.White else textColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (subTab == "Current") {
                if (activeBill == null) {
                    // Empty state: No Unpaid Bills
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                                        .background(Color(0xFF10B981).copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "All Bills Paid",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "You don't have any pending maintenance bills at the moment.",
                                    color = secondaryTextColor,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Detailed Bill Details
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // New Bill Banner
                        val isPaid = activeBill.status == "Paid"
                        if (!isPaid) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.1f))
                                    .border(1.dp, PrimaryBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
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
                                    Icon(Icons.Outlined.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                }
                                Column {
                                    Text("New bill received", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Please clear your dues by end of this month.", color = secondaryTextColor, fontSize = 11.sp)
                                }
                            }
                        }

                        // Bill Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    Column {
                                        Text("Greenview Apartments · Block C", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("#BILL-${activeBill.month.replace(" ", "-")}", color = secondaryTextColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                                Spacer(modifier = Modifier.height(16.dp))

                                val chipBg = if (isPaid) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                                val chipDotColor = if (isPaid) Color(0xFF22C55E) else Color(0xFFF59E0B)
                                val chipText = if (isPaid) "Paid" else "Unpaid"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("${activeBill.month.uppercase()} ${activeBill.year ?: ""} BILL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor)
                                        Text("₹${String.format("%,d", activeBill.amount ?: activeBill.totalAmount)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(chipBg)
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(chipDotColor))
                                            Text(chipText, color = chipDotColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(if (isPaid) "Paid via ${activeBill.paymentApp ?: "UPI"}" else "Pending", color = secondaryTextColor, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Breakdown Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val lineItems = listOf(
                                    Pair("Monthly Maintenance", activeBill.amount ?: activeBill.totalAmount),
                                    Pair("Water Charges", activeBill.waterBill),
                                    Pair("Common Area Electricity", activeBill.electricityBill),
                                    Pair("Rent", activeBill.rent),
                                    Pair("Parking slot", activeBill.parkingFee),
                                    Pair("Penalty", activeBill.penalty),
                                    Pair("Other Charges", activeBill.otherCharges)
                                ).filter { it.second > 0 }
                                
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    lineItems.forEach { (label, amt) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(label, fontSize = 14.sp, color = secondaryTextColor)
                                            Text("₹${String.format("%,d", amt)}", fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        if (!isPaid) {
                            // Pay Methods Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("PAY USING ANY OF THESE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // QR Row
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        QRCodeComponent(sizeDp = 90.dp, color = if (isDarkMode) Color.Black else Color(0xFF0E0E0E))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Scan QR with any UPI app", fontSize = 13.sp, color = textColor, fontWeight = FontWeight.SemiBold)
                                            Text("PhonePe · GPay · Paytm · BHIM", fontSize = 11.sp, color = secondaryTextColor)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // UPI ID Card
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(textColor.copy(alpha = 0.03f))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("UPI ID", fontSize = 10.sp, color = secondaryTextColor)
                                            Text("greenview@upi", fontSize = 14.sp, color = textColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(onClick = {
                                            android.widget.Toast.makeText(context, "UPI ID copied", android.widget.Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Phone Number Card
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(textColor.copy(alpha = 0.03f))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("PhonePe Number", fontSize = 10.sp, color = secondaryTextColor)
                                            Text("+91 98765 12345", fontSize = 14.sp, color = textColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(onClick = {
                                            android.widget.Toast.makeText(context, "Phone number copied", android.widget.Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            } else {
                // Payment History List
                if (allBills.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
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
                                    text = "No Payment History",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "You don't have any billing records in your history yet.",
                                    color = secondaryTextColor,
                                    fontSize = 13.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        allBills.forEach { bill ->
                            val isPaid = bill.status == "Paid"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardColor)
                                    .border(1.dp, textColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                    .clickable { selectedHistoryBill = bill }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${bill.month} ${bill.year ?: ""}",
                                        color = textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isPaid) "Paid via ${bill.paymentApp ?: "UPI"}" else "Unpaid",
                                        color = secondaryTextColor,
                                        fontSize = 12.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "₹${String.format("%,d", bill.amount ?: bill.totalAmount)}",
                                        color = textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isPaid) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isPaid) "Paid" else "Due",
                                            color = if (isPaid) Color(0xFF22C55E) else Color(0xFFF59E0B),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Sticky Footer Button for current unpaid bill
        if (subTab == "Current" && activeBill != null) {
            val isPaid = activeBill.status == "Paid"
            val total = activeBill.amount ?: activeBill.totalAmount
            if (!isPaid) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(backgroundColor.copy(alpha = 0.95f))
                        .padding(24.dp)
                ) {
                    Button(
                        onClick = onPay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text("Pay ₹${String.format("%,d", total)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Selected History Bill Breakdown Dialog
        selectedHistoryBill?.let { bill ->
            val isPaid = bill.status == "Paid"
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { selectedHistoryBill = null }
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
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bill Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            IconButton(onClick = { selectedHistoryBill = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                            }
                        }

                        // Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${bill.month} ${bill.year ?: ""}",
                                    color = textColor,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPaid) "Paid on ${bill.createdAt?.take(10) ?: "Today"}" else "Unpaid",
                                    color = secondaryTextColor,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "₹${String.format("%,d", bill.amount ?: bill.totalAmount)}",
                                color = textColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))

                        // Breakdown Items
                        Text("BREAKDOWN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                        val items = listOf(
                            Pair("Monthly Maintenance", bill.amount ?: bill.totalAmount),
                            Pair("Water Charges", bill.waterBill),
                            Pair("Common Area Electricity", bill.electricityBill),
                            Pair("Rent", bill.rent),
                            Pair("Parking slot", bill.parkingFee),
                            Pair("Penalty", bill.penalty),
                            Pair("Other Charges", bill.otherCharges)
                        ).filter { it.second > 0 }

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items.forEach { (label, amt) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(label, fontSize = 14.sp, color = secondaryTextColor)
                                    Text("₹${String.format("%,d", amt)}", fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        if (isPaid) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                            Text("PAYMENT DETAILS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment App", fontSize = 14.sp, color = secondaryTextColor)
                                Text(bill.paymentApp ?: "UPI", fontSize = 14.sp, color = textColor, fontWeight = FontWeight.Medium)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Transaction ID", fontSize = 14.sp, color = secondaryTextColor)
                                Text(bill.transactionId ?: "N/A", fontSize = 14.sp, color = textColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                            }
                        }

                        Button(
                            onClick = { selectedHistoryBill = null },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSheetScreen(
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onSelectApp: (String) -> Unit,
    activeBill: com.simats.appartmentliving.data.PaymentDto? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)
    
    val apps = listOf(
        Triple("PhonePe", Color(0xFF8B5CF6), "P"),
        Triple("Google Pay", Color(0xFF3B82F6), "G"),
        Triple("Paytm", Color(0xFF0EA5E9), "P"),
        Triple("BHIM UPI", Color(0xFFEA580C), "B")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Dark Overlay Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onBack() }
        )

        // Bottom Sheet Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .border(1.dp, dividerColor, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(textColor.copy(alpha = 0.1f))
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val total = activeBill?.totalAmount ?: 23150
                    Text("Pay ₹${String.format("%,d", total)}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Text("to Greenview Apartments", fontSize = 14.sp, color = secondaryTextColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // UPI Apps
                Text("PAY WITH UPI APP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    apps.forEach { (name, color, initial) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(textColor.copy(alpha = 0.02f))
                                .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
                                .clickable { onSelectApp(name) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initial, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(name, fontSize = 15.sp, color = textColor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = secondaryTextColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // OR SCAN QR
                Text("OR SCAN QR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = secondaryTextColor, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(textColor.copy(alpha = 0.02f))
                        .border(1.dp, dividerColor, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QRCodeComponent(sizeDp = 60.dp, color = if (isDarkMode) Color.Black else Color(0xFF0E0E0E))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Scan with any UPI app", fontSize = 13.sp, color = textColor, fontWeight = FontWeight.SemiBold)
                        Text("greenview@upi", fontSize = 11.sp, color = secondaryTextColor, fontFamily = FontFamily.Monospace)
                    }
                    IconButton(onClick = {
                        android.widget.Toast.makeText(context, "UPI ID copied", android.widget.Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Payment is secure · End-to-end encrypted",
                    fontSize = 11.sp,
                    color = secondaryTextColor.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PaymentSuccessScreen(
    isDarkMode: Boolean,
    onDone: () -> Unit
) {
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color.Black
    val secondaryTextColor = textColor.copy(alpha = 0.6f)
    val dividerColor = textColor.copy(alpha = 0.08f)
    val successColor = Color(0xFF22C55E)

    var checkScale by remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.6f,
                stiffness = Spring.StiffnessLow
            )
        ) { value, _ ->
            checkScale = value
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(if (isDarkMode) Color(0xFF121212) else Color(0xFFF8FAFC))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = java.util.Random(100)
            val colors = listOf(successColor, PrimaryBlue, Color(0xFFF59E0B))
            repeat(15) { i ->
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height * 0.7f
                val radius = (4 + random.nextInt(6)).dp.toPx()
                val color = colors[random.nextInt(colors.size)]
                drawCircle(
                    color = color.copy(alpha = 0.4f),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(x, y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(successColor.copy(alpha = 0.15f))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(successColor)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(scaleX = checkScale, scaleY = checkScale)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text("Payment Successful", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text("You paid", fontSize = 15.sp, color = secondaryTextColor)
                Spacer(modifier = Modifier.height(2.dp))
                Text("₹23,150", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = successColor)
                Spacer(modifier = Modifier.height(4.dp))
                Text("to Greenview Apartments", fontSize = 13.sp, color = secondaryTextColor)

                Spacer(modifier = Modifier.height(32.dp))

                // Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, dividerColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val items = listOf(
                            Triple("Transaction ID", "TXN8492015421", true),
                            Triple("Bill", "#BILL-2026-0512", true),
                            Triple("Paid via", "PhonePe · UPI", false),
                            Triple("Date & Time", "May 21, 2026 · 2:45 PM IST", false),
                            Triple("Bank Ref", "HDFC · 421890XX", true)
                        )
                        items.forEach { (label, value, isMono) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontSize = 13.sp, color = secondaryTextColor)
                                Text(
                                    text = value,
                                    fontSize = 13.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = Color.White)
                        Text("Download Receipt", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, textColor.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Share", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = textColor.copy(alpha = 0.05f),
                            contentColor = textColor
                        )
                    ) {
                        Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeDashboardItem(
    notice: com.simats.appartmentliving.data.NoticeDto,
    isDarkMode: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color,
    borderColor: Color
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
                // Category Tag
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
                
                // Priority Badge
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

