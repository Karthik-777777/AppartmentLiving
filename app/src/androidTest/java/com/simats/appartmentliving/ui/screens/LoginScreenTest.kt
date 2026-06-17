package com.simats.appartmentliving.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simats.appartmentliving.ui.theme.AppartmentLivingTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreen_RendersCorrectly() {
        composeTestRule.setContent {
            AppartmentLivingTheme(darkTheme = false) {
                LoginScreen(
                    isDarkMode = false,
                    onThemeToggle = {},
                    onLoginSuccess = {},
                    onSignUpClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Verify standard elements are displayed
        composeTestRule.onNodeWithText("Welcome back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Register your flat").assertIsDisplayed()
    }

    @Test
    fun testLoginScreen_AdminToggleWorks() {
        composeTestRule.setContent {
            AppartmentLivingTheme(darkTheme = false) {
                LoginScreen(
                    isDarkMode = false,
                    onThemeToggle = {},
                    onLoginSuccess = {},
                    onSignUpClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Initially we are in Resident mode, toggle should say "Switch to Admin Login"
        composeTestRule.onNodeWithText("Switch to Admin Login").assertIsDisplayed()

        // Tap Switch to Admin Login
        composeTestRule.onNodeWithText("Switch to Admin Login").performClick()

        // Verify elements updated for Admin Mode
        composeTestRule.onNodeWithText("Admin Portal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Access Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Switch to Resident Login").assertIsDisplayed()
    }

    @Test
    fun testLoginScreen_InputFieldsAcceptText() {
        composeTestRule.setContent {
            AppartmentLivingTheme(darkTheme = false) {
                LoginScreen(
                    isDarkMode = false,
                    onThemeToggle = {},
                    onLoginSuccess = {},
                    onSignUpClick = {},
                    onForgotPasswordClick = {}
                )
            }
        }

        // Perform email and password inputs
        composeTestRule.onNodeWithText("rahul@example.com").performTextInput("test@domain.com")
        composeTestRule.onNodeWithText("••••••••••••").performTextInput("secret123")

        // Assert they are displayed or updated in fields
        composeTestRule.onNodeWithText("test@domain.com").assertIsDisplayed()
    }
}
