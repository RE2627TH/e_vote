package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.ResetPasswordRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {

    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState = _resetState.asStateFlow()

    fun resetPassword(studentId: String, email: String, password: String) {
        _resetState.value = ResetState.Loading
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.resetPassword(
                    ResetPasswordRequest(studentId, email, password)
                )

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        _resetState.value = ResetState.Success(result.message)
                    } else {
                        _resetState.value = ResetState.Error(result.message)
                    }
                } else {
                    _resetState.value = ResetState.Error("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error("Connection Failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _resetState.value = ResetState.Idle
    }
}

sealed class ResetState {
    object Idle : ResetState()
    object Loading : ResetState()
    data class Success(val message: String) : ResetState()
    data class Error(val message: String) : ResetState()
}
