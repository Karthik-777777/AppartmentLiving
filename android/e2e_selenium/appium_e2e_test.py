#!/usr/bin/env python3
"""
ApartmentLiving - Appium (Selenium-based) E2E Functionality Testing Script

Appium extends the Selenium WebDriver API to support native mobile application automation.
This test case launches the compiled ApartmentLiving Android App, enters credentials, toggles 
admin mode, logs in, and verifies the dashboard rendering.

Pre-requisites:
1. Node.js & Appium Server installed (`npm install -g appium`)
2. Appium UIAutomator2 driver installed (`appium driver install uiautomator2`)
3. Android Emulator or physical device running and connected via ADB.
4. Python Appium library installed (`pip3 install Appium-Python-Client`)
"""

import time
import unittest
from appium import webdriver
from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException

class ApartmentLivingE2ETests(unittest.TestCase):

    def setUp(self):
        # Desired Capabilities for Android UiAutomator2 (Selenium-based Appium Driver)
        self.caps = {
            "platformName": "Android",
            "automationName": "UiAutomator2",
            "deviceName": "Android Emulator",
            "appPackage": "com.simats.appartmentliving",
            "appActivity": ".MainActivity",
            "noReset": False,
            "fullReset": False,
            "newCommandTimeout": 300,
            "ensureWebviewsHavePages": True
        }
        
        # Appium Server URL (Appium 2.x defaults to http://127.0.0.1:4723/)
        self.driver = webdriver.Remote("http://127.0.0.1:4723", self.caps)
        self.wait = WebDriverWait(self.driver, 10)

    def test_resident_login_and_dashboard_flow(self):
        """
        E2E Test Flow:
        1. Open App. Wait for Splash Screen to complete.
        2. Verify landing on Login Screen.
        3. Enter Resident email & password.
        4. Click 'Sign In' button.
        5. Verify user navigates to Resident Home Dashboard and visualizes flat details.
        """
        print("Starting E2E Resident Login Test Flow...")

        try:
            # 1. Wait for email text field to load (confirms Login screen is displayed)
            # Compose inputs display with content description or standard resource ID
            email_field = self.wait.until(
                EC.presence_of_element_located((AppiumBy.XPATH, "//android.widget.EditText[contains(@text, 'rahul@example.com')]"))
            )
            print("Successfully reached Login Screen.")

            # 2. Enter email
            email_field.clear()
            email_field.send_keys("rahul@example.com")
            print("Entered email.")

            # 3. Enter password
            password_field = self.driver.find_element(AppiumBy.XPATH, "//android.widget.EditText[contains(@text, '••••••••••••')]")
            password_field.clear()
            password_field.send_keys("password123")
            print("Entered password.")

            # 4. Click Sign In Button
            # Locate button containing 'Sign In' text
            signin_button = self.driver.find_element(AppiumBy.XPATH, "//*[@text='Sign In' or @content-desc='Sign In']")
            signin_button.click()
            print("Clicked 'Sign In' button.")

            # 5. Wait for Dashboard to render (Verify dashboard title, profile or welcome text)
            welcome_banner = self.wait.until(
                EC.presence_of_element_located((AppiumBy.XPATH, "//*[contains(@text, 'Rahul') or contains(@text, 'Flat')]"))
            )
            self.assertTrue(welcome_banner.is_displayed())
            print("E2E Login flow completed successfully! Navigated to home dashboard.")

        except TimeoutException:
            self.fail("E2E Test timed out waiting for UI element rendering.")
        except Exception as e:
            self.fail(f"E2E Test encountered an error: {str(e)}")

    def test_admin_login_toggle_and_portal_verification(self):
        """
        E2E Test Flow:
        1. Open App.
        2. Toggle mode to Admin Mode.
        3. Verify Admin UI overlays ('Admin Mode', 'Admin Portal', 'Access Dashboard').
        4. Enter Admin email & password.
        5. Click 'Access Dashboard' button.
        6. Verify redirection to Admin dashboard.
        """
        print("Starting E2E Admin Login Test Flow...")

        try:
            # 1. Wait for Login Screen
            switch_to_admin = self.wait.until(
                EC.presence_of_element_located((AppiumBy.XPATH, "//*[@text='Switch to Admin Login']"))
            )
            
            # 2. Click switch to admin
            switch_to_admin.click()
            print("Toggled to Admin Login Mode.")

            # 3. Verify Admin specific layout texts
            admin_portal_header = self.wait.until(
                EC.presence_of_element_located((AppiumBy.XPATH, "//*[@text='Admin Portal']"))
            )
            self.assertTrue(admin_portal_header.is_displayed())
            
            access_dash_btn = self.driver.find_element(AppiumBy.XPATH, "//*[@text='Access Dashboard']")
            self.assertTrue(access_dash_btn.is_displayed())
            print("Admin UI layout components validated successfully.")

            # 4. Enter Admin credentials
            email_field = self.driver.find_element(AppiumBy.XPATH, "//android.widget.EditText[contains(@text, 'suresh.k@greenview.com')]")
            email_field.clear()
            email_field.send_keys("suresh.k@greenview.com")
            
            password_field = self.driver.find_element(AppiumBy.XPATH, "//android.widget.EditText[contains(@text, '••••••••••••')]")
            password_field.clear()
            password_field.send_keys("adminpass")
            
            # 5. Click Access Dashboard button
            access_dash_btn.click()
            print("Clicked 'Access Dashboard' button.")

            # 6. Verify landing on Admin Home Dashboard
            admin_dash_header = self.wait.until(
                EC.presence_of_element_located((AppiumBy.XPATH, "//*[contains(@text, 'Admin') and contains(@text, 'Dashboard')]"))
            )
            self.assertTrue(admin_dash_header.is_displayed())
            print("Admin login flow completed successfully.")

        except TimeoutException:
            self.fail("E2E Admin Test timed out waiting for UI element rendering.")
        except Exception as e:
            self.fail(f"E2E Admin Test encountered an error: {str(e)}")

    def tearDown(self):
        # Shut down driver session
        if self.driver:
            self.driver.quit()
            print("Driver session closed.")

if __name__ == "__main__":
    unittest.main()
