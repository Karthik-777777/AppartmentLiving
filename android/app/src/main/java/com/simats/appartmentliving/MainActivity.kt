package com.simats.appartmentliving

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simats.appartmentliving.ui.screens.LoginScreen
import com.simats.appartmentliving.ui.screens.SplashScreen
import com.simats.appartmentliving.ui.theme.AppartmentLivingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.simats.appartmentliving.data.RetrofitClient.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(true) }
            
            AppartmentLivingTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        isDarkMode = isDarkMode,
                        onThemeToggle = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val authManager = remember { com.simats.appartmentliving.data.AuthManager(context) }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToLogin = {
                    val token = authManager.getToken()
                    val role = authManager.getRole()
                    if (token != null && role != null) {
                        com.simats.appartmentliving.data.RetrofitClient.token = token
                        if (role.lowercase() == "admin") {
                            navController.navigate("admin_home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onLoginSuccess = { role ->
                    if (role == "Admin") {
                        navController.navigate("admin_home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onSignUpClick = {
                    navController.navigate("register")
                },
                onForgotPasswordClick = {
                    navController.navigate("forgot_password")
                }
            )
        }
        composable("forgot_password") {
            com.simats.appartmentliving.ui.screens.ForgotPasswordScreen(
                isDarkMode = isDarkMode,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("register") {
            com.simats.appartmentliving.ui.screens.RegisterScreen(
                isDarkMode = isDarkMode,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { 
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onContinueClick = {
                    // Navigate to Aadhaar upload
                }
            )
        }
        composable("home") {
            com.simats.appartmentliving.ui.screens.HomeScreen(
                isDarkMode = isDarkMode,
                onLogout = {
                    authManager.clearSession()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onThemeToggle = onThemeToggle,
                onChangePasswordClick = { navController.navigate("change_password") },
                onAboutClick = { navController.navigate("about") }
            )
        }
        composable("admin_home") {
            com.simats.appartmentliving.ui.screens.AdminScreen(
                isDarkMode = isDarkMode,
                onThemeToggle = onThemeToggle,
                onLogout = {
                    authManager.clearSession()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("change_password") {
            com.simats.appartmentliving.ui.screens.ChangePasswordScreen(
                isDarkMode = isDarkMode,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("about") {
            com.simats.appartmentliving.ui.screens.AboutScreen(
                isDarkMode = isDarkMode,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}