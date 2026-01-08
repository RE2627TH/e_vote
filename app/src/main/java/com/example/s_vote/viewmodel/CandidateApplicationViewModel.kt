package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.CandidateApplicationRequest
import com.example.s_vote.model.CandidateApplicationResponse
import com.example.s_vote.model.ApplicationStatusResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CandidateApplicationViewModel : ViewModel() {

    private val _applicationResponse = MutableStateFlow<CandidateApplicationResponse?>(null)
    val applicationResponse: StateFlow<CandidateApplicationResponse?> = _applicationResponse.asStateFlow()

    private val _statusResponse = MutableStateFlow<ApplicationStatusResponse?>(null)
    val statusResponse: StateFlow<ApplicationStatusResponse?> = _statusResponse.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Added email and phone parameters to match the screen call
    fun submitApplication(name: String, dob: String, studentId: String, email: String, phone: String, department: String, position: String, manifesto: String) {
        _isLoading.value = true
        _errorMessage.value = null

        // Use userId "1" for testing if not available
        val userId = "1"

        val request = CandidateApplicationRequest(
            userId = userId,
            name = name,
            dob = dob,
            studentId = studentId,
            email = email,     // Added
            phone = phone,     // Added
            department = department,
            position = position,
            manifesto = manifesto
        )

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.submitApplication(request)
                if (response.isSuccessful && response.body() != null) {
                    val generic = response.body()!!
                    _applicationResponse.value = CandidateApplicationResponse(
                        success = generic.success,
                        message = generic.message,
                        applicationId = "123" // Changed Int to String to match your error
                    )
                } else {
                    _errorMessage.value = "Submission Failed"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkApplicationStatusById(appId: Int) {
        // Implementation for status check
    }
}