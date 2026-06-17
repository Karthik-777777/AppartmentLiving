package com.simats.appartmentliving.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.appartmentliving.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    isDarkMode: Boolean,
    onBackClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var otpText by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    val backgroundColor = if (isDarkMode) Color(0xFF0C0C0E) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Logo Card: Key Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9))
                    .border(
                        width = 1.dp,
                        color = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Key,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Password Title
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subhead description
            Text(
                text = "Enter your registered email address and we'll send you a 6-digit OTP to reset your password.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = secondaryTextColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Email Address Input Label
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = secondaryTextColor,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Email TextField
            TextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(inputBackgroundColor)
                    .border(
                        width = 1.dp,
                        color = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(14.dp)
                    ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    cursorColor = PrimaryBlue
                ),
                placeholder = { Text("rahul@example.com", color = secondaryTextColor.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = secondaryTextColor.copy(alpha = 0.5f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send OTP Button
            Button(
                onClick = { isOtpSent = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = "Send OTP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // OTP Section
            Text(
                text = "Enter OTP",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isOtpSent) secondaryTextColor else secondaryTextColor.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))

            // OTP Input Cells Row
            OtpInputField(
                otpText = otpText,
                onOtpTextChange = { otpText = it },
                isDarkMode = isDarkMode,
                enabled = isOtpSent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Verify OTP Button
            val isOtpValid = otpText.length == 6
            OutlinedButton(
                onClick = {
                    if (isOtpValid) {
                        onBackClick()
                    }
                },
                enabled = isOtpValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isOtpValid) PrimaryBlue else if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    disabledContentColor = secondaryTextColor.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = "Verify OTP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isOtpValid) PrimaryBlue else secondaryTextColor.copy(alpha = 0.4f),
                        fontSize = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    isDarkMode: Boolean,
    enabled: Boolean,
    otpLength: Int = 6
) {
    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= otpLength && it.all { char -> char.isDigit() }) {
                onOtpTextChange(it)
            }
        },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(otpLength) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = enabled && index == otpText.length
                    
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9)
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isFocused -> PrimaryBlue
                                    char.isNotEmpty() -> PrimaryBlue.copy(alpha = 0.5f)
                                    else -> if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0)
                                },
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = char.ifEmpty { "-" },
                            color = if (char.isNotEmpty()) {
                                if (isDarkMode) Color.White else Color(0xFF0F172A)
                            } else {
                                if (isDarkMode) Color(0xFF52525B) else Color(0xFF94A3B8)
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    )
}
