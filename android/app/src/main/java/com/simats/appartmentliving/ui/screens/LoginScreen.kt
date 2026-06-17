package com.simats.appartmentliving.ui.screens

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.appartmentliving.ui.theme.PrimaryBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onForgotPasswordClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel = remember { com.simats.appartmentliving.ui.viewmodels.LoginViewModel(context) }
    val loginResult by viewModel.loginResult.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("Resident") }

    LaunchedEffect(loginResult) {
        when (loginResult) {
            is com.simats.appartmentliving.ui.viewmodels.LoginResult.Success -> {
                val role = (loginResult as com.simats.appartmentliving.ui.viewmodels.LoginResult.Success).role
                onLoginSuccess(if (role.lowercase() == "admin") "Admin" else "Resident")
                viewModel.reset()
            }
            is com.simats.appartmentliving.ui.viewmodels.LoginResult.Error -> {
                errorMessage = (loginResult as com.simats.appartmentliving.ui.viewmodels.LoginResult.Error).message
            }
            else -> {}
        }
    }

    val backgroundColor = if (isDarkMode) Color(0xFF0C0C0E) else Color(0xFFF8FAFC)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val inputBackgroundColor = if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9)

    Scaffold(
        containerColor = backgroundColor,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedRole == "Admin") "Are you a resident?" else "Are you an admin?",
                    color = secondaryTextColor,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedRole == "Admin") "Switch to Resident Login" else "Switch to Admin Login",
                    color = PrimaryBlue,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.clickable {
                        selectedRole = if (selectedRole == "Admin") "Resident" else "Admin"
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Admin Top Header Bar & Status Badge
            if (selectedRole == "Admin") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(PrimaryBlue)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onThemeToggle() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Admin Mode",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Theme toggle top right for Resident Mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(inputBackgroundColor)
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkMode) Color.Yellow else PrimaryBlue
                        )
                    }
                }
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // App Logo Card with Optional Shield Badge overlay
                Box {
                    // Main Building Icon Card
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isDarkMode) Color(0xFF18181C) else Color(0xFFF1F5F9))
                            .border(
                                width = 1.dp,
                                color = if (isDarkMode) Color(0xFF27272A) else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(18.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Building2Icon(
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF3B82F6)
                        )
                    }

                    // Admin Badge overlay in bottom-right corner
                    if (selectedRole == "Admin") {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 4.dp, y = 4.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .border(width = 2.dp, color = backgroundColor, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Welcome / Admin Portal Title
                Text(
                    text = if (selectedRole == "Admin") "Admin Portal" else "Welcome back",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 30.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Subtitle
                Text(
                    text = if (selectedRole == "Admin") "Sign in to manage the society" else "Sign in to manage your apartment",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = secondaryTextColor,
                        fontSize = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Email Field Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = if (selectedRole == "Admin") "Admin Email" else "Email Address",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                        placeholder = { 
                            Text(
                                text = if (selectedRole == "Admin") "suresh.k@greenview.com" else "rahul@example.com", 
                                color = secondaryTextColor.copy(alpha = 0.5f)
                            ) 
                        },
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
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Password Field Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = password,
                        onValueChange = { password = it },
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
                        placeholder = { Text("••••••••••••", color = secondaryTextColor.copy(alpha = 0.5f)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = secondaryTextColor.copy(alpha = 0.5f)
                            )
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remember Me & Forgot Password Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var rememberMe by remember { mutableStateOf(false) }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = PrimaryBlue,
                                uncheckedColor = secondaryTextColor.copy(alpha = 0.5f),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Remember me",
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    }

                    Text(
                        text = "Forgot password?",
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onForgotPasswordClick() }
                    )
                }

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Access Dashboard / Sign In Button
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            viewModel.login(email, password)
                        } else {
                            errorMessage = "Please fill in all fields"
                        }
                    },
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
                    if (loginResult is com.simats.appartmentliving.ui.viewmodels.LoginResult.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (selectedRole == "Admin") "Access Dashboard" else "Sign In",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                // Resident-only sections (Register Flat link)
                if (selectedRole != "Admin") {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Register Flat section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "New here? ",
                            color = secondaryTextColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Register your flat",
                            color = PrimaryBlue,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable { onSignUpClick() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
