package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()

    fun register(name: String, dob: String, student_id: String, department: String, email: String, password: String, role: String) {
        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                // Corrected: Use RetrofitInstance directly
                val request = RegisterRequest(name, dob, student_id, department, email, password, role)
                val response = RetrofitInstance.api.registerUser(request)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        _registerState.value = RegisterState.Success(result.message, result.userId ?: result.user?.id)
                    } else {
                        _registerState.value = RegisterState.Error(result.message)
                    }
                } else {
                    _registerState.value = RegisterState.Error("Server Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Connection Failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _registerState.value = RegisterState.Idle
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val message: String, val userId: String?) : RegisterState()
    data class Error(val message: String) : RegisterState()
}