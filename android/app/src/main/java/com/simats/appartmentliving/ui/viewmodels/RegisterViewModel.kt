package com.simats.appartmentliving.ui.viewmodels

import com.simats.appartmentliving.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class RegisterResult {
    object Idle : RegisterResult()
    object Loading : RegisterResult()
    object Success : RegisterResult()
    data class Error(val message: String) : RegisterResult()
}

sealed class OtpState {
    object Idle : OtpState()
    object Loading : OtpState()
    data class Success(val message: String) : OtpState()
    data class Error(val message: String) : OtpState()
}

class RegisterViewModel {
    private val _registerResult = MutableStateFlow<RegisterResult>(RegisterResult.Idle)
    val registerResult: StateFlow<RegisterResult> = _registerResult

    private val _otpSendingState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpSendingState: StateFlow<OtpState> = _otpSendingState

    private val _otpVerifyingState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpVerifyingState: StateFlow<OtpState> = _otpVerifyingState

    fun register(
        email: String,
        password: String,
        residentName: String,
        phone: String,
        block: String,
        flatNumber: String,
        flatType: String,
        ownerType: String
    ) {
        _registerResult.value = RegisterResult.Loading
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                // Calculate maintenanceAmount based on flatType
                val maintenanceAmount = when (flatType) {
                    "1 BHK" -> 1500
                    "2 BHK" -> 3500
                    "3 BHK" -> 5000
                    else -> 8000 // Villa
                }
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.register(
                        RegisterRequest(
                            email = email,
                            password = password,
                            role = "resident",
                            residentName = residentName,
                            phone = phone,
                            block = block,
                            flatNumber = flatNumber,
                            flatType = flatType,
                            ownerType = ownerType,
                            maintenanceAmount = maintenanceAmount
                        )
                    )
                }
                _registerResult.value = RegisterResult.Success
            } catch (e: retrofit2.HttpException) {
                val errorMsg = parseHttpError(e)
                _registerResult.value = RegisterResult.Error(errorMsg)
            } catch (e: Exception) {
                _registerResult.value = RegisterResult.Error(e.message ?: "Failed to register")
            }
        }
    }

    fun sendOtp(email: String) {
        _otpSendingState.value = OtpState.Loading
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.sendOtp(SendOtpRequest(email = email))
                }
                _otpSendingState.value = OtpState.Success(response.message)
            } catch (e: retrofit2.HttpException) {
                val errorMsg = parseHttpError(e)
                _otpSendingState.value = OtpState.Error(errorMsg)
            } catch (e: Exception) {
                _otpSendingState.value = OtpState.Error(e.message ?: "Failed to send OTP")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        _otpVerifyingState.value = OtpState.Loading
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.verifyOtp(VerifyOtpRequest(email = email, otp = otp))
                }
                if (response.verified) {
                    _otpVerifyingState.value = OtpState.Success(response.message)
                } else {
                    _otpVerifyingState.value = OtpState.Error(response.message)
                }
            } catch (e: retrofit2.HttpException) {
                val errorMsg = parseHttpError(e)
                _otpVerifyingState.value = OtpState.Error(errorMsg)
            } catch (e: Exception) {
                _otpVerifyingState.value = OtpState.Error(e.message ?: "Failed to verify OTP")
            }
        }
    }

    fun resetOtpState() {
        _otpSendingState.value = OtpState.Idle
        _otpVerifyingState.value = OtpState.Idle
    }

    fun reset() {
        _registerResult.value = RegisterResult.Idle
        resetOtpState()
    }

    private fun parseHttpError(e: retrofit2.HttpException): String {
        return try {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                try {
                    val map = com.google.gson.Gson().fromJson(errorBody, Map::class.java)
                    map["message"] as? String ?: map["error"] as? String ?: errorBody
                } catch (p: Exception) {
                    errorBody.trim().removeSurrounding("\"")
                }
            } else {
                "Error ${e.code()}: ${e.message()}"
            }
        } catch (jsonEx: Exception) {
            "Error ${e.code()}: ${e.message()}"
        }
    }
}
