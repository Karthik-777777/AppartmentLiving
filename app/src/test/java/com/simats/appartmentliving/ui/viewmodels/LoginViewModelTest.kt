package com.simats.appartmentliving.ui.viewmodels

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        viewModel = LoginViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateIsIdle() {
        assertEquals(LoginResult.Idle, viewModel.loginResult.value)
    }

    @Test
    fun testResetMethodSetsStateToIdle() {
        viewModel.reset()
        assertEquals(LoginResult.Idle, viewModel.loginResult.value)
    }

    @Test
    fun testLoginMethodUpdatesStateToLoading() = runTest {
        viewModel.login("test@example.com", "password")
        assertEquals(LoginResult.Loading, viewModel.loginResult.value)
    }
}
