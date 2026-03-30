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

    fun submitApplication(
        userId: String,
        name: String,
        studentId: String,
        position: String,
        manifesto: String,
        course: String,
        college: String,
        goals: String,
        pledges: String,
        symbolName: String,
        photo: String?,
        symbol: String?
    ) {
        _isLoading.value = true
        _errorMessage.value = null

        val request = CandidateApplicationRequest(
            userId = userId,
            name = name,
            studentId = studentId,
            position = position,
            manifesto = manifesto,
            course = course,
            college = college,
            goals = goals,
            pledges = pledges,
            symbolName = symbolName,
            photo = photo,
            symbol = symbol
        )

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.submitApplication(request)
                if (response.isSuccessful && response.body() != null) {
                    _applicationResponse.value = response.body()
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
        // Implementation for status check can be added if API is defined
    }
}