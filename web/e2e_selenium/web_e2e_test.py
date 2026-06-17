#!/usr/bin/env python3
"""
ApartmentLiving - Web E2E Functionality Testing Script using Selenium WebDriver

This script automates browser operations to verify authentication, dashboard loads, 
and page navigation for both Resident and Admin views on the React web client.

Pre-requisites:
1. Python Selenium library installed (`pip3 install selenium`)
2. Chrome Browser and corresponding chromedriver installed (or use Webdriver Manager).
3. React Web App running locally (usually `npm start` at http://localhost:3000).
"""

import time
import unittest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException

class ApartmentLivingWebE2ETests(unittest.TestCase):

    def setUp(self):
        # Configure Chrome options (headless option can be toggled for CI)
        options = webdriver.ChromeOptions()
        # options.add_argument("--headless") # Uncomment for headless execution in CI
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        
        self.driver = webdriver.Chrome(options=options)
        self.wait = WebDriverWait(self.driver, 10)
        self.base_url = "http://localhost:3000"

    def test_resident_web_login_flow(self):
        """
        E2E Test Flow:
        1. Open React web client login page.
        2. Enter resident email & password.
        3. Click 'Sign In' button.
        4. Verify URL transitions to '/resident-dashboard' or landing elements exist.
        """
        print("Starting E2E Web Resident Login Test Flow...")
        self.driver.get(f"{self.base_url}/login")

        try:
            # 1. Wait for email input to be visible
            email_input = self.wait.until(
                EC.presence_of_element_located((By.XPATH, "//input[@placeholder='Email Address' or @type='email']"))
            )
            print("Successfully reached Web Login Page.")

            # 2. Input resident credentials
            email_input.clear()
            email_input.send_keys("rahul@example.com")
            
            password_input = self.driver.find_element(By.XPATH, "//input[@type='password']")
            password_input.clear()
            password_input.send_keys("password123")
            print("Entered credentials.")

            # 3. Click Sign In
            login_btn = self.driver.find_element(By.XPATH, "//button[contains(text(), 'Sign In')]")
            login_btn.click()
            print("Clicked Login button.")

            # 4. Wait for dashboard landing
            # Verify top-bar profile name or sidebar navigation elements
            welcome_tag = self.wait.until(
                EC.presence_of_element_located((By.XPATH, "//*[contains(text(), 'Rahul') or contains(text(), 'Dashboard')]"))
            )
            self.assertTrue(welcome_tag.is_displayed())
            print("E2E Web Resident Login successful! Reached Dashboard.")

        except TimeoutException:
            self.fail("E2E Web Resident Test timed out waiting for elements.")

    def test_admin_web_portal_toggle_login(self):
        """
        E2E Test Flow:
        1. Open Login page.
        2. Toggle mode to Admin Login.
        3. Enter admin credentials.
        4. Submit and verify navigation to Admin Dashboard (/admin-dashboard).
        """
        print("Starting E2E Web Admin Login Test Flow...")
        self.driver.get(f"{self.base_url}/login")

        try:
            # 1. Wait for Toggle Mode button
            admin_toggle = self.wait.until(
                EC.element_to_be_clickable((By.XPATH, "//button[contains(text(), 'Admin Login')]"))
            )
            admin_toggle.click()
            print("Toggled to Admin Login view.")

            # 2. Enter admin credentials
            email_input = self.wait.until(
                EC.presence_of_element_located((By.XPATH, "//input[@type='email']"))
            )
            email_input.clear()
            email_input.send_keys("suresh.k@greenview.com")
            
            password_input = self.driver.find_element(By.XPATH, "//input[@type='password']")
            password_input.clear()
            password_input.send_keys("adminpass")
            print("Entered Admin credentials.")

            # 3. Submit
            submit_btn = self.driver.find_element(By.XPATH, "//button[contains(text(), 'Access Dashboard')]")
            submit_btn.click()
            print("Clicked Access Dashboard.")

            # 4. Verify Admin Panel landing
            admin_dashboard_header = self.wait.until(
                EC.presence_of_element_located((By.XPATH, "//*[contains(text(), 'Admin Panel') or contains(text(), 'Overview')]"))
            )
            self.assertTrue(admin_dashboard_header.is_displayed())
            print("E2E Web Admin Login successful! Reached Admin Panel.")

        except TimeoutException:
            self.fail("E2E Web Admin Test timed out waiting for elements.")

    def tearDown(self):
        # Close browser instance
        if self.driver:
            self.driver.quit()
            print("Browser closed.")

if __name__ == "__main__":
    unittest.main()
