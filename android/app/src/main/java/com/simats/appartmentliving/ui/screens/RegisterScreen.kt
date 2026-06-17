package com.simats.appartmentliving.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.appartmentliving.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    isDarkMode: Boolean,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val viewModel = remember { com.simats.appartmentliving.ui.viewmodels.RegisterViewModel() }
    val registerResult by viewModel.registerResult.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var currentStep by remember { mutableStateOf(1) }

    // Step 1 States
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var apartmentNo by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var unitType by remember { mutableStateOf("2 BHK") } // Default selection in mockup

    val otpSendingState by viewModel.otpSendingState.collectAsState()
    val otpVerifyingState by viewModel.otpVerifyingState.collectAsState()

    var otpSent by remember { mutableStateOf(false) }
    var otpVerified by remember { mutableStateOf(false) }
    var otpText by remember { mutableStateOf("") }

    LaunchedEffect(otpSendingState) {
        if (otpSendingState is com.simats.appartmentliving.ui.viewmodels.OtpState.Success) {
            otpSent = true
        }
    }

    LaunchedEffect(otpVerifyingState) {
        if (otpVerifyingState is com.simats.appartmentliving.ui.viewmodels.OtpState.Success) {
            otpVerified = true
        }
    }

    LaunchedEffect(email) {
        otpSent = false
        otpVerified = false
        otpText = ""
        viewModel.resetOtpState()
    }

    LaunchedEffect(registerResult) {
        when (registerResult) {
            is com.simats.appartmentliving.ui.viewmodels.RegisterResult.Success -> {
                currentStep = 4 // Success screen
                viewModel.reset()
            }
            is com.simats.appartmentliving.ui.viewmodels.RegisterResult.Error -> {
                errorMessage = (registerResult as com.simats.appartmentliving.ui.viewmodels.RegisterResult.Error).message
            }
            else -> {}
        }
    }

    // Step 2 States
    var profilePhotoUploaded by remember { mutableStateOf(false) }
    var aadhaarFrontUploaded by remember { mutableStateOf(true) } // Pre-uploaded in mockup
    var aadhaarBackUploaded by remember { mutableStateOf(false) }

    // Constants
    val backgroundColor = if (isDarkMode) Color(0xFF0C0C0E) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9)
    val cardBorderColor = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0)

    // Back Press Interception to move step-by-step
    BackHandler(enabled = true) {
        when (currentStep) {
            5 -> onBackClick()
            4 -> onBackClick()
            3 -> currentStep = 2
            2 -> currentStep = 1
            1 -> onBackClick()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            if (currentStep != 4) { // Success screen has no top bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            when (currentStep) {
                                5 -> onBackClick()
                                3 -> currentStep = 2
                                2 -> currentStep = 1
                                1 -> onBackClick()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentStep) {
                            1 -> "Create Account"
                            2 -> "Verification"
                            3 -> "Review Details"
                            5 -> "Application Status"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
        ) {
            when (currentStep) {
                1 -> Step1PersonalDetails(
                    isDarkMode = isDarkMode,
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    email = email,
                    onEmailChange = { email = it },
                    password = password,
                    onPasswordChange = { password = it },
                    phone = phone,
                    onPhoneChange = { phone = it },
                    apartmentNo = apartmentNo,
                    onApartmentNoChange = { apartmentNo = it },
                    floor = floor,
                    onFloorChange = { floor = it },
                    unitType = unitType,
                    onUnitTypeChange = { unitType = it },
                    onContinue = { currentStep = 2 },
                    onLoginClick = onLoginClick,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    inputBackgroundColor = inputBackgroundColor,
                    cardBorderColor = cardBorderColor,
                    otpSent = otpSent,
                    otpVerified = otpVerified,
                    otpText = otpText,
                    onOtpTextChange = { otpText = it },
                    otpSendingState = otpSendingState,
                    otpVerifyingState = otpVerifyingState,
                    onSendOtpClick = { viewModel.sendOtp(it) },
                    onVerifyOtpClick = { emailVal, otpVal -> viewModel.verifyOtp(emailVal, otpVal) }
                )

                2 -> Step2Documents(
                    isDarkMode = isDarkMode,
                    profilePhotoUploaded = profilePhotoUploaded,
                    onProfilePhotoClick = { profilePhotoUploaded = !profilePhotoUploaded },
                    aadhaarFrontUploaded = aadhaarFrontUploaded,
                    onAadhaarFrontClick = { aadhaarFrontUploaded = !aadhaarFrontUploaded },
                    aadhaarBackUploaded = aadhaarBackUploaded,
                    onAadhaarBackClick = { aadhaarBackUploaded = !aadhaarBackUploaded },
                    onBack = { currentStep = 1 },
                    onContinue = { currentStep = 3 },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    inputBackgroundColor = inputBackgroundColor,
                    cardBorderColor = cardBorderColor
                )

                3 -> Step3Review(
                    isDarkMode = isDarkMode,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    apartmentNo = apartmentNo,
                    floor = floor,
                    unitType = unitType,
                    aadhaarFrontUploaded = aadhaarFrontUploaded,
                    aadhaarBackUploaded = aadhaarBackUploaded,
                    onEditPersonal = { currentStep = 1 },
                    onEditDocs = { currentStep = 2 },
                    onSubmit = {
                        val blockPart = if (apartmentNo.contains("-")) apartmentNo.substringBefore("-").trim() else ""
                        val flatPart = if (apartmentNo.contains("-")) apartmentNo.substringAfter("-").trim() else apartmentNo.trim()
                        viewModel.register(
                            email = email.trim(),
                            password = password.trim(),
                            residentName = fullName.trim(),
                            phone = phone.trim(),
                            block = blockPart,
                            flatNumber = flatPart,
                            flatType = unitType,
                            ownerType = "Owner"
                        )
                    },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    inputBackgroundColor = inputBackgroundColor,
                    cardBorderColor = cardBorderColor,
                    isLoading = registerResult is com.simats.appartmentliving.ui.viewmodels.RegisterResult.Loading,
                    errorMessage = errorMessage
                )

                4 -> SuccessScreen(
                    onCheckStatus = { currentStep = 5 },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    inputBackgroundColor = inputBackgroundColor
                )

                5 -> StatusScreen(
                    isDarkMode = isDarkMode,
                    onLogout = onLoginClick,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }
    }
}

// ==========================================
// STEP 1: PERSONAL DETAILS
// ==========================================
@Composable
fun Step1PersonalDetails(
    isDarkMode: Boolean,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    apartmentNo: String,
    onApartmentNoChange: (String) -> Unit,
    floor: String,
    onFloorChange: (String) -> Unit,
    unitType: String,
    onUnitTypeChange: (String) -> Unit,
    onContinue: () -> Unit,
    onLoginClick: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    inputBackgroundColor: Color,
    cardBorderColor: Color,
    otpSent: Boolean,
    otpVerified: Boolean,
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    otpSendingState: com.simats.appartmentliving.ui.viewmodels.OtpState,
    otpVerifyingState: com.simats.appartmentliving.ui.viewmodels.OtpState,
    onSendOtpClick: (String) -> Unit,
    onVerifyOtpClick: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    val isFormValid = fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && phone.isNotBlank() &&
            apartmentNo.isNotBlank() && floor.isNotBlank() && otpVerified

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Progress Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Step 1 of 3", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Personal Details", color = secondaryTextColor, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight()
                    .background(PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Full Name Field
        Text("Full Name", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = fullName,
            onValueChange = onFullNameChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBackgroundColor,
                unfocusedContainerColor = inputBackgroundColor,
                disabledContainerColor = inputBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = PrimaryBlue
            ),
            placeholder = { Text("e.g. Rahul Sharma", color = secondaryTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Outlined.Person, contentDescription = null, tint = secondaryTextColor.copy(alpha = 0.5f))
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Address Field
        Text("Email Address", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBackgroundColor,
                unfocusedContainerColor = inputBackgroundColor,
                disabledContainerColor = inputBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = PrimaryBlue
            ),
            placeholder = { Text("rahul@example.com", color = secondaryTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = secondaryTextColor.copy(alpha = 0.5f))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // OTP Verification UI Section
        if (!otpVerified) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { onSendOtpClick(email.trim()) },
                enabled = email.isNotBlank() && email.contains("@") && email.contains(".") && otpSendingState !is com.simats.appartmentliving.ui.viewmodels.OtpState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
                )
            ) {
                if (otpSendingState is com.simats.appartmentliving.ui.viewmodels.OtpState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (otpSent) "Resend OTP" else "Send OTP",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (otpSendingState is com.simats.appartmentliving.ui.viewmodels.OtpState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = (otpSendingState as com.simats.appartmentliving.ui.viewmodels.OtpState.Error).message,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Email Verified Successfully ✓",
                    color = Color(0xFF10B981),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (otpSent && !otpVerified) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Enter Verification Code (OTP)", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = otpText,
                onValueChange = onOtpTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBackgroundColor,
                    unfocusedContainerColor = inputBackgroundColor,
                    disabledContainerColor = inputBackgroundColor,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = PrimaryBlue
                ),
                placeholder = { Text("6-digit code", color = secondaryTextColor.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = secondaryTextColor.copy(alpha = 0.5f))
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { onVerifyOtpClick(email.trim(), otpText.trim()) },
                enabled = otpText.isNotBlank() && otpText.length == 6 && otpVerifyingState !is com.simats.appartmentliving.ui.viewmodels.OtpState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    disabledContainerColor = Color(0xFF10B981).copy(alpha = 0.5f)
                )
            ) {
                if (otpVerifyingState is com.simats.appartmentliving.ui.viewmodels.OtpState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = "Verify OTP",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (otpVerifyingState is com.simats.appartmentliving.ui.viewmodels.OtpState.Error) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = (otpVerifyingState as com.simats.appartmentliving.ui.viewmodels.OtpState.Error).message,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        Text("Password", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        var passwordVisible by remember { mutableStateOf(false) }
        TextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBackgroundColor,
                unfocusedContainerColor = inputBackgroundColor,
                disabledContainerColor = inputBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = PrimaryBlue
            ),
            placeholder = { Text("••••••••••••", color = secondaryTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = secondaryTextColor.copy(alpha = 0.5f))
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = secondaryTextColor.copy(alpha = 0.5f)
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Field
        Text("Phone Number", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = inputBackgroundColor,
                unfocusedContainerColor = inputBackgroundColor,
                disabledContainerColor = inputBackgroundColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                cursorColor = PrimaryBlue
            ),
            placeholder = { Text("+91 98765 43210", color = secondaryTextColor.copy(alpha = 0.5f)) },
            leadingIcon = {
                Icon(Icons.Outlined.Phone, contentDescription = null, tint = secondaryTextColor.copy(alpha = 0.5f))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apartment No. & Floor Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Apartment No.", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = apartmentNo,
                    onValueChange = onApartmentNoChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor,
                        unfocusedContainerColor = inputBackgroundColor,
                        disabledContainerColor = inputBackgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = PrimaryBlue
                    ),
                    placeholder = { Text("e.g. C-404", color = secondaryTextColor.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Text(
                            text = "#",
                            color = secondaryTextColor.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    singleLine = true
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("Floor", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = floor,
                    onValueChange = onFloorChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputBackgroundColor,
                        unfocusedContainerColor = inputBackgroundColor,
                        disabledContainerColor = inputBackgroundColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = PrimaryBlue
                    ),
                    placeholder = { Text("e.g. 4", color = secondaryTextColor.copy(alpha = 0.5f)) },
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Unit Type Selection Grid
        Text("Unit Type", color = secondaryTextColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            UnitTypeItem(
                label = "1 BHK",
                selected = unitType == "1 BHK",
                onClick = { onUnitTypeChange("1 BHK") },
                isDarkMode = isDarkMode,
                textColor = textColor,
                inputBackgroundColor = inputBackgroundColor,
                cardBorderColor = cardBorderColor,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            UnitTypeItem(
                label = "2 BHK",
                selected = unitType == "2 BHK",
                onClick = { onUnitTypeChange("2 BHK") },
                isDarkMode = isDarkMode,
                textColor = textColor,
                inputBackgroundColor = inputBackgroundColor,
                cardBorderColor = cardBorderColor,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            UnitTypeItem(
                label = "3 BHK",
                selected = unitType == "3 BHK",
                onClick = { onUnitTypeChange("3 BHK") },
                isDarkMode = isDarkMode,
                textColor = textColor,
                inputBackgroundColor = inputBackgroundColor,
                cardBorderColor = cardBorderColor,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            UnitTypeItem(
                label = "Villa",
                selected = unitType == "Villa",
                onClick = { onUnitTypeChange("Villa") },
                isDarkMode = isDarkMode,
                textColor = textColor,
                inputBackgroundColor = inputBackgroundColor,
                cardBorderColor = cardBorderColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Continue Button
        Button(
            onClick = onContinue,
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                disabledContainerColor = PrimaryBlue.copy(alpha = 0.5f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                color = secondaryTextColor,
                fontSize = 14.sp
            )
            Text(
                text = "Login here",
                color = PrimaryBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}

@Composable
fun UnitTypeItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    textColor: Color,
    inputBackgroundColor: Color,
    cardBorderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) PrimaryBlue.copy(alpha = 0.1f) else inputBackgroundColor)
            .border(
                width = 1.dp,
                color = if (selected) PrimaryBlue else cardBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) PrimaryBlue else textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ==========================================
// STEP 2: DOCUMENTS UPLOAD
// ==========================================
@Composable
fun Step2Documents(
    isDarkMode: Boolean,
    profilePhotoUploaded: Boolean,
    onProfilePhotoClick: () -> Unit,
    aadhaarFrontUploaded: Boolean,
    onAadhaarFrontClick: () -> Unit,
    aadhaarBackUploaded: Boolean,
    onAadhaarBackClick: () -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    inputBackgroundColor: Color,
    cardBorderColor: Color
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Progress Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Step 2 of 3", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Documents", color = secondaryTextColor, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.66f)
                    .fillMaxHeight()
                    .background(PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Profile Photo dashed upload circle
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .drawBehind {
                        drawCircle(
                            color = if (profilePhotoUploaded) PrimaryBlue else secondaryTextColor.copy(alpha = 0.4f),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                        )
                    }
                    .clip(CircleShape)
                    .background(if (profilePhotoUploaded) PrimaryBlue.copy(alpha = 0.05f) else Color.Transparent)
                    .clickable { onProfilePhotoClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUploaded) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "profile_pic.jpg",
                            color = PrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = "Upload",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Profile Photo", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Clear photo showing your face",
                color = secondaryTextColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
        HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))

        // Aadhaar Card Section
        Text("Aadhaar Card", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Required for society security verification", color = secondaryTextColor, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Front Side
            AadhaarUploadCard(
                label = "Front Side",
                uploaded = aadhaarFrontUploaded,
                fileName = "aadhaar_front.jpg",
                onClick = onAadhaarFrontClick,
                isDarkMode = isDarkMode,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                inputBackgroundColor = inputBackgroundColor,
                modifier = Modifier.weight(1f)
            )

            // Back Side
            AadhaarUploadCard(
                label = "Back Side",
                uploaded = aadhaarBackUploaded,
                fileName = "aadhaar_back.jpg",
                onClick = onAadhaarBackClick,
                isDarkMode = isDarkMode,
                textColor = textColor,
                secondaryTextColor = secondaryTextColor,
                inputBackgroundColor = inputBackgroundColor,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Navigation Buttons Bottom Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back Arrow Button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Transparent)
                    .border(1.dp, cardBorderColor, RoundedCornerShape(14.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AadhaarUploadCard(
    label: String,
    uploaded: Boolean,
    fileName: String,
    onClick: () -> Unit,
    isDarkMode: Boolean,
    textColor: Color,
    secondaryTextColor: Color,
    inputBackgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (uploaded) PrimaryBlue.copy(alpha = 0.05f) else inputBackgroundColor)
            .border(
                width = 1.dp,
                color = if (uploaded) PrimaryBlue else if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        if (uploaded) {
            // Checkmark in Top Right corner
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = fileName,
                    color = secondaryTextColor,
                    fontSize = 10.sp
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Upload,
                    contentDescription = null,
                    tint = secondaryTextColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Max 5MB (JPG/PNG)",
                    color = secondaryTextColor.copy(alpha = 0.5f),
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ==========================================
// STEP 3: REVIEW DETAILS
// ==========================================
@Composable
fun Step3Review(
    isDarkMode: Boolean,
    fullName: String,
    email: String,
    phone: String,
    apartmentNo: String,
    floor: String,
    unitType: String,
    aadhaarFrontUploaded: Boolean,
    aadhaarBackUploaded: Boolean,
    onEditPersonal: () -> Unit,
    onEditDocs: () -> Unit,
    onSubmit: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    inputBackgroundColor: Color,
    cardBorderColor: Color,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val scrollState = rememberScrollState()
    val initials = if (fullName.isNotBlank()) {
        fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    } else {
        "RS"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Progress Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Step 3 of 3", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Review & Submit", color = secondaryTextColor, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Card 1: Personal Details
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(inputBackgroundColor)
                .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = fullName.ifEmpty { "Rahul Sharma" },
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = email.ifEmpty { "rahul@example.com" },
                                color = secondaryTextColor,
                                fontSize = 13.sp
                            )
                        }
                    }

                    IconButton(onClick = onEditPersonal) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Personal",
                            tint = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = cardBorderColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Phone", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = phone.ifEmpty { "+91 98765 43210" }, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Apartment", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${apartmentNo.ifEmpty { "C-404" }} ($unitType)", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column {
                    Text("Floor", color = secondaryTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${floor.ifEmpty { "4" }}${getOrdinalSuffix(floor.ifEmpty { "4" })} Floor", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Documents Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(inputBackgroundColor)
                .border(1.dp, cardBorderColor, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Documents", color = textColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onEditDocs) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Documents",
                            tint = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aadhaar Front row
                DocumentRowItem(
                    label = "Aadhaar Front",
                    uploaded = aadhaarFrontUploaded,
                    cardBorderColor = cardBorderColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Aadhaar Back row
                DocumentRowItem(
                    label = "Aadhaar Back",
                    uploaded = aadhaarBackUploaded,
                    cardBorderColor = cardBorderColor,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Bottom section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFEF4444),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = "By submitting, you agree to the society rules and regulations.",
                color = secondaryTextColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Submit Application",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentRowItem(
    label: String,
    uploaded: Boolean,
    cardBorderColor: Color,
    textColor: Color,
    secondaryTextColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (uploaded) Color.Transparent else cardBorderColor.copy(alpha = 0.2f))
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (uploaded) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = if (uploaded) PrimaryBlue else secondaryTextColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = if (uploaded) textColor else secondaryTextColor.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }

        if (uploaded) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(12.dp)
                )
            }
        } else {
            Text(
                text = "Missing",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

fun getOrdinalSuffix(value: String): String {
    val num = value.toIntOrNull() ?: return "th"
    if (num in 11..13) return "th"
    return when (num % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

// ==========================================
// STEP 4: SUCCESS SCREEN
// ==========================================
@Composable
fun SuccessScreen(
    onCheckStatus: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    inputBackgroundColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Center Content Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Application Submitted!",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your registration details have been sent to the society admin for verification.",
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Reference ID Box
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(inputBackgroundColor)
                        .padding(vertical = 16.dp, horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Reference ID",
                            color = secondaryTextColor,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "#AL-REQ-8492",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Button
        Button(
            onClick = onCheckStatus,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = "Check Status",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            )
        }
    }
}

// ==========================================
// STEP 5: APPLICATION STATUS SCREEN
// ==========================================
@Composable
fun StatusScreen(
    isDarkMode: Boolean,
    onLogout: () -> Unit,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Center Content Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Circular timer/clock progress
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Draw background track
                        drawCircle(
                            color = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0),
                            style = Stroke(width = 5.dp.toPx())
                        )
                        // Draw progress arc (sweep 270 degrees)
                        drawArc(
                            color = PrimaryBlue,
                            startAngle = -90f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 5.dp.toPx())
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Under Review",
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "The admin is currently reviewing your details. This usually takes 24-48 hours.",
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Vertical Timeline
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Node 1: Submitted
                    TimelineNode(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        },
                        title = "Submitted",
                        subtitle = "Today, 10:30 AM",
                        showLine = true,
                        isDarkMode = isDarkMode
                    )

                    // Node 2: Under Review
                    TimelineNode(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(2.dp, PrimaryBlue, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue)
                                )
                            }
                        },
                        title = "Under Review",
                        subtitle = "Admin verification pending",
                        showLine = true,
                        isDarkMode = isDarkMode
                    )

                    // Node 3: Decision
                    TimelineNode(
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0),
                                        shape = CircleShape
                                    )
                            )
                        },
                        title = "Decision",
                        subtitle = "Approval or rejection",
                        showLine = false,
                        isDarkMode = isDarkMode
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                onClick = { /* Contact Support */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, PrimaryBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryBlue)
            ) {
                Text(
                    text = "Contact Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Logout Action Row
            Row(
                modifier = Modifier
                    .clickable { onLogout() }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TimelineNode(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    showLine: Boolean,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            icon()
            if (showLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0))
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(top = 1.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF0F172A),
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 12.sp
            )
        }
    }
}
