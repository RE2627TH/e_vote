package com.example.s_vote.viewmodel

import com.example.s_vote.SessionManager

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Login UI State
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    /**
     * Login Function with Role Verification
     */
    fun login(email: String, password: String, intendedRole: String) {
        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.loginUser(
                    LoginRequest(email, password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Log.d("LoginViewModel", "API Response: $result")

                    if (result.success) {
                        val actualRole = result.role?.trim()?.lowercase() ?: ""
                        val intended = intendedRole.trim().lowercase()

                        // 1. Check for redirection FIRST (Application incomplete)
                        if (result.redirect_to_form == true) {
                             _loginState.value = LoginState.RedirectToForm(result.user_id ?: 0)
                             return@launch
                        }

                        // 2. Role Verification
                        val isStudentMatch = (intended == "student" && (actualRole == "student" || actualRole == "user"))
                        val isExactMatch = actualRole == intended

                        if (isExactMatch || isStudentMatch) {
                            // ✅ Save session data using SessionManager
                            val sessionManager = SessionManager(getApplication())
                            sessionManager.saveSession(
                                token = result.token,
                                userId = result.user_id.toString(),
                                studentId = result.student_id,
                                role = result.role,
                                isSubscribed = result.is_subscribed == 1,
                                isProfileCompleted = result.is_profile_completed == 1
                            )

                            // Re-init Retrofit with the new token
                            RetrofitInstance.init(getApplication())

                            // 🚨 Fetch and Update FCM Token
                            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val token = task.result
                                    viewModelScope.launch {
                                        try {
                                            RetrofitInstance.api.updateFcmToken(mapOf(
                                                "user_id" to result.user_id.toString(),
                                                "fcm_token" to token
                                            ))
                                        } catch (e: Exception) {
                                            Log.e("LoginViewModel", "FCM Update failed: ${e.message}")
                                        }
                                    }
                                }
                            }

                            _loginState.value = LoginState.Success(result.role ?: "")
                        } else {
                            _loginState.value = LoginState.Error("Access Denied: You do not have $intendedRole privileges on this portal.")
                        }

                    } else {
                        _loginState.value = LoginState.Error(result.message ?: "Login failed")
                    }

                } else {
                    _loginState.value = LoginState.Error("Server error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login error", e)
                _loginState.value = LoginState.Error("Connection Failed: ${e.message ?: "Unknown error"}")
            }
        }
    }

    /**
     * Reset state after navigation
     */
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}

/**
 * Login State sealed class
 */
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class RedirectToForm(val userId: Int) : LoginState()
    data class Error(val message: String) : LoginState()
}
