package com.simats.appartmentliving.data

import android.content.Context
import com.google.gson.Gson

class AuthManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_auth_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveSession(token: String, role: String, user: UserDto) {
        prefs.edit().apply {
            putString("token", token)
            putString("role", role)
            putString("user_json", gson.toJson(user))
            apply()
        }
        RetrofitClient.token = token
    }

    fun getToken(): String? = prefs.getString("token", null)
    fun getRole(): String? = prefs.getString("role", null)
    
    fun getUser(): UserDto? {
        val userJson = prefs.getString("user_json", null) ?: return null
        return try {
            gson.fromJson(userJson, UserDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        RetrofitClient.token = null
    }

    fun isUserLoggedIn(): Boolean {
        return getToken() != null
    }
}
