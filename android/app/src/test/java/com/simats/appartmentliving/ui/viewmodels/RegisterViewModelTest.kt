package com.simats.appartmentliving.ui.viewmodels

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStatesAreIdle() {
        assertEquals(RegisterResult.Idle, viewModel.registerResult.value)
        assertEquals(OtpState.Idle, viewModel.otpSendingState.value)
        assertEquals(OtpState.Idle, viewModel.otpVerifyingState.value)
    }

    @Test
    fun testResetMethodSetsStatesToIdle() {
        viewModel.reset()
        assertEquals(RegisterResult.Idle, viewModel.registerResult.value)
        assertEquals(OtpState.Idle, viewModel.otpSendingState.value)
        assertEquals(OtpState.Idle, viewModel.otpVerifyingState.value)
    }

    @Test
    fun testRegisterMethodUpdatesStateToLoading() = runTest {
        viewModel.register(
            email = "test@example.com",
            password = "password123",
            residentName = "John Doe",
            phone = "1234567890",
            block = "A",
            flatNumber = "101",
            flatType = "2 BHK",
            ownerType = "Owner"
        )
        assertEquals(RegisterResult.Loading, viewModel.registerResult.value)
    }

    @Test
    fun testSendOtpMethodUpdatesStateToLoading() = runTest {
        viewModel.sendOtp("test@example.com")
        assertEquals(OtpState.Loading, viewModel.otpSendingState.value)
    }

    @Test
    fun testVerifyOtpMethodUpdatesStateToLoading() = runTest {
        viewModel.verifyOtp("test@example.com", "1234")
        assertEquals(OtpState.Loading, viewModel.otpVerifyingState.value)
    }
}
