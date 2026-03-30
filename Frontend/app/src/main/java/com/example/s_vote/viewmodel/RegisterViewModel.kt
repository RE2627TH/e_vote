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

    private val _otpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpState = _otpState.asStateFlow()

    fun sendOtp(email: String) {
        _otpState.value = OtpState.Sending
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.sendOtp(com.example.s_vote.model.SendOtpRequest(email))
                if (response.isSuccessful && response.body()?.success == true) {
                    _otpState.value = OtpState.Sent(
                        response.body()?.message ?: "OTP sent to your email!",
                        response.body()?.dev_otp
                    )
                } else {
                    _otpState.value = OtpState.Error(response.body()?.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                _otpState.value = OtpState.Error("Connection Failed: ${e.message}")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        _otpState.value = OtpState.Verifying
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.verifyOtp(com.example.s_vote.model.VerifyOtpRequest(email, otp))
                if (response.isSuccessful && response.body()?.success == true) {
                    _otpState.value = OtpState.Verified
                } else {
                    _otpState.value = OtpState.Error(response.body()?.message ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                _otpState.value = OtpState.Error("Connection Failed: ${e.message}")
            }
        }
    }

    fun resetOtpState() {
        _otpState.value = OtpState.Idle
    }

    fun register(name: String, dob: String, student_id: String, department: String, email: String, password: String, role: String) {
        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
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

sealed class OtpState {
    object Idle : OtpState()
    object Sending : OtpState()
    data class Sent(val message: String, val devOtp: String? = null) : OtpState()
    object Verifying : OtpState()
    object Verified : OtpState()
    data class Error(val message: String) : OtpState()
}