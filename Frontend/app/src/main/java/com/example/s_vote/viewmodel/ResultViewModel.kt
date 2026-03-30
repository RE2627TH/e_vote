package com.example.s_vote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.ElectionStatus
import com.example.s_vote.SessionManager
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.ElectionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private var pollJob: Job? = null

    private val _electionStatus = MutableStateFlow<ElectionStatus?>(null)
    val electionStatus = _electionStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _results = MutableStateFlow<List<ElectionResult>>(emptyList())
    val results = _results.asStateFlow()

    private val _userRole = MutableStateFlow<String?>("student")
    val userRole = _userRole.asStateFlow()

    init {
        _userRole.value = sessionManager.getUserRole()?.lowercase()
    }

    fun fetchElectionStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getActiveElection()
                if (response.isSuccessful && response.body() != null) {
                    val status = response.body()!!
                    _electionStatus.value = status
                    
                    val role = sessionManager.getUserRole()?.lowercase() ?: "student"
                    val userId = sessionManager.getUserId() ?: ""

                    val isEnded = status.status == "CLOSED"
                    val isPublished = status.isPublished
                    
                    if (status.isActive || (isEnded && !isPublished)) {
                        // Poll if active OR closed but results not out yet
                        startLivePolling(userId)
                    } else if (isPublished || isEnded) {
                        // If ended and results available, fetch once
                        fetchResults(userId)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun startLivePolling(userId: String) {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                fetchResults(userId)
                delay(5000) // Poll every 5 seconds for live updates
            }
        }
    }

    fun fetchResults(userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getResults(userId)
                if (response.isSuccessful && response.body() != null) {
                    _results.value = response.body()!!
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun endElection() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.publishResults()
                if (response.isSuccessful && response.body()?.success == true) {
                    // Refresh status to update UI
                    fetchElectionStatus()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
