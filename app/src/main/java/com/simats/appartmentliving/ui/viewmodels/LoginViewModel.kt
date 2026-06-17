package com.simats.appartmentliving.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simats.appartmentliving.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class LoginResult {
    object Idle : LoginResult()
    object Loading : LoginResult()
    data class Success(val role: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

class LoginViewModel(context: Context) {
    private val authManager = AuthManager(context)
    
    private val _loginResult = MutableStateFlow<LoginResult>(LoginResult.Idle)
    val loginResult: StateFlow<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        _loginResult.value = LoginResult.Loading
        // Launch a coroutine to make API call
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
            try {
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    RetrofitClient.apiService.login(LoginRequest(email, password))
                }
                authManager.saveSession(response.token, response.role, response.user)
                _loginResult.value = LoginResult.Success(response.role)
            } catch (e: retrofit2.HttpException) {
                val errorMsg = try {
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
                _loginResult.value = LoginResult.Error(errorMsg)
            } catch (e: Exception) {
                _loginResult.value = LoginResult.Error(e.message ?: "Invalid email or password")
            }
        }
    }
    
    fun reset() {
        _loginResult.value = LoginResult.Idle
    }
}
