package com.example.s_vote.viewmodel

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

                        // Role Verification
                        // Handle students which might come back as 'student' or 'user' depending on DB
                        val isStudentMatch = (intended == "student" && (actualRole == "student" || actualRole == "user"))
                        val isExactMatch = actualRole == intended

                        if (isExactMatch || isStudentMatch) {
                            // ✅ Save session data only if roles match
                            val sharedPref = getApplication<Application>()
                                .getSharedPreferences("s_vote_prefs", Context.MODE_PRIVATE)

                            sharedPref.edit()
                                .putString("USER_ID", result.user_id.toString())
                                .putString("STUDENT_ID", result.student_id)
                                .putString("USER_ROLE", result.role)
                                .putBoolean("IS_LOGGED_IN", true)
                                .apply()

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
    data class Error(val message: String) : LoginState()
}
