package com.example.s_vote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.SessionManager
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.Candidate
import com.example.s_vote.model.CreateElectionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    // For Candidate Review (Pending Approvals)
    private val _candidateReviewList = MutableStateFlow<List<Candidate>>(emptyList())
    val candidateReviewList = _candidateReviewList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    private val _dashboardStats = MutableStateFlow<com.example.s_vote.model.AdminDashboardStats?>(null)
    val dashboardStats = _dashboardStats.asStateFlow()

    // Students List
    private val _students = MutableStateFlow<List<com.example.s_vote.model.AppUser>>(emptyList())
    val students = _students.asStateFlow()

    // For Candidate Management (All Candidate Users)
    private val _candidateUserList = MutableStateFlow<List<com.example.s_vote.model.AppUser>>(emptyList())
    val candidateUserList = _candidateUserList.asStateFlow()

    // Elections List
    private val _elections = MutableStateFlow<List<com.example.s_vote.ElectionStatus>>(emptyList())
    val elections = _elections.asStateFlow()

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

    fun fetchCandidateReviewList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch all candidates regardless of status
                val response = RetrofitInstance.api.getCandidatesByStatus("ALL")
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    android.util.Log.d("AdminVM", "Loaded ${list.size} candidates")
                    _candidateReviewList.value = list
                } else {
                    android.util.Log.e("AdminVM", "Failed to load: ${response.code()}")
                    setMessage("Failed to load candidates")
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

    fun deleteCandidate(userId: String) {
        manageCandidate(userId, "delete")
    }

    private fun manageCandidate(userId: String, action: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.manageCandidate(userId, action)
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Candidate ${action}d successfully")
                    fetchCandidateReviewList() // Refresh list
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
    
    fun updateElectionStatus(electionId: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = mapOf("election_id" to electionId, "status" to status)
                val response = RetrofitInstance.api.updateElectionStatus(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Status updated to $status")
                    fetchElections() // Refresh list
                } else {
                    setMessage("Failed to update status")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteElection(electionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = mapOf("election_id" to electionId)
                val response = RetrofitInstance.api.deleteElection(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Election deleted successfully")
                    fetchElections() // Refresh list
                } else {
                    setMessage("Failed to delete election")
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

    fun fetchResults(electionId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = sessionManager.getUserId() ?: ""
                val response = RetrofitInstance.api.getResults(userId, electionId)
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

    fun fetchElections() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getElections()
                if (response.isSuccessful && response.body() != null) {
                    _elections.value = response.body()!!
                } else {
                    setMessage("Failed to load elections")
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

    fun fetchStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getStudents()
                if (response.isSuccessful && response.body() != null) {
                    _students.value = response.body()!!
                } else {
                    setMessage("Failed to load students")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCandidateUserList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getCandidateUsers()
                if (response.isSuccessful && response.body() != null) {
                    _candidateUserList.value = response.body()!!
                } else {
                    setMessage("Failed to load candidates")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Selected Student Detail
    private val _selectedStudent = MutableStateFlow<com.example.s_vote.model.ProfileResponse?>(null)
    val selectedStudent = _selectedStudent.asStateFlow()

    fun fetchStudentDetail(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getProfile(userId)
                if (response.isSuccessful && response.body() != null) {
                    _selectedStudent.value = response.body()
                } else {
                    setMessage("Failed to load student details")
                }
            } catch (e: Exception) {
                setMessage("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteStudent(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = mapOf("user_id" to userId)
                val response = RetrofitInstance.api.deleteUser(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    setMessage("Student deleted successfully")
                    onSuccess()
                } else {
                    setMessage(response.body()?.message ?: "Failed to delete student")
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
                // fetchResults() // Removed from global polling as it now depends on ID
                kotlinx.coroutines.delay(5000) // Poll every 5 seconds
            }
        }
    }

    fun stopPolling() {
        job?.cancel()
        job = null
    }
}
