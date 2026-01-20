package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.Candidate
import com.example.s_vote.model.CreateElectionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    // Candidates List
    private val _pendingCandidates = MutableStateFlow<List<Candidate>>(emptyList())
    val pendingCandidates = _pendingCandidates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _dashboardStats = MutableStateFlow<com.example.s_vote.model.AdminDashboardStats?>(null)
    val dashboardStats = _dashboardStats.asStateFlow()

    private var lastMessageTime = 0L

    private fun setMessage(msg: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMessageTime > 2000) {
            _message.value = msg
            lastMessageTime = currentTime
        }
    }

    fun fetchDashboardStats() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getDashboardStats()
                if (response.isSuccessful && response.body()?.success == true) {
                    _dashboardStats.value = response.body()!!.stats
                }
            } catch (e: Exception) {
                // Fail silently or log
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPendingCandidates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Assuming getCandidates supports status query param
                val response = RetrofitInstance.api.getCandidatesByStatus("pending")
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    android.util.Log.d("AdminVM", "Loaded ${list.size} pending candidates")
                    _pendingCandidates.value = list
                } else {
                    android.util.Log.e("AdminVM", "Failed to load: ${response.code()}")
                    setMessage("Failed to load pending candidates")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveCandidate(userId: String) {
        manageCandidate(userId, "approve")
    }

    fun rejectCandidate(userId: String) {
        manageCandidate(userId, "reject")
    }

    private fun manageCandidate(userId: String, action: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.manageCandidate(userId, action)
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Candidate ${action}d successfully")
                    fetchPendingCandidates() // Refresh list
                } else {
                    setMessage("Failed to $action candidate")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createElection(title: String, startDate: String, endDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CreateElectionRequest(title, startDate, endDate)
                val response = RetrofitInstance.api.createElection(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Election created!")
                } else {
                    setMessage("Failed to create election")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Results
    private val _results = MutableStateFlow<List<com.example.s_vote.model.ElectionResult>>(emptyList())
    val results = _results.asStateFlow()

    fun fetchResults() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getResults()
                if (response.isSuccessful && response.body() != null) {
                    _results.value = response.body()!!
                } else {
                    setMessage("Failed to load results")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun publishResults() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.publishResults()
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Results Published Successfully!")
                } else {
                    setMessage("Failed to publish results")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearMessage() {
        _message.value = null
    }

    // Polling Logic
    private var job: kotlinx.coroutines.Job? = null

    fun startPolling() {
        if (job?.isActive == true) return
        job = viewModelScope.launch {
            while (true) {
                fetchDashboardStats()
                fetchResults()
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds
            }
        }
    }

    fun stopPolling() {
        job?.cancel()
        job = null
    }
}
