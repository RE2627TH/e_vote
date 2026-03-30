package com.example.s_vote.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.AppUser
import com.example.s_vote.model.Candidate
import com.example.s_vote.model.CandidateApplicationRequest
import com.example.s_vote.model.CandidateApplicationResponse
import com.example.s_vote.model.ProfileResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CandidateViewModel : ViewModel() {

    private val TAG = "CandidateViewModel"

    // Candidate Listing State
    private val _candidates = MutableStateFlow<List<Candidate>>(emptyList())
    val candidates: StateFlow<List<Candidate>> = _candidates.asStateFlow()

    // Application Submission State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _activeElection = MutableStateFlow<com.example.s_vote.ElectionStatus?>(null)
    val activeElection: StateFlow<com.example.s_vote.ElectionStatus?> = _activeElection.asStateFlow()

    private val _submitSuccess = MutableStateFlow<CandidateApplicationResponse?>(null)
    val submitSuccess: StateFlow<CandidateApplicationResponse?> = _submitSuccess.asStateFlow()

    fun fetchCandidates(position: String = "ALL") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Map navigation role names to exact DB position strings
                val apiPosition = when(position.lowercase().replace(" ", "_")) {
                    "all_candidates", "all" -> "ALL"
                    "president" -> "President"
                    "vice_president" -> "Vice President"
                    "sports_secretary" -> "Sports Secretary"
                    "cultural_secretary" -> "Cultural Secretary"
                    "discipline_secretary" -> "Discipline Secretary"
                    "treasurer" -> "Treasurer"
                    else -> position // Fallback for direct matches
                }
                
                // Fetch only ACCEPTED (Approved) candidates that are ready for viewing/voting
                val response = RetrofitInstance.api.getCandidatesByStatus("ACCEPTED", apiPosition)
                if (response.isSuccessful) {
                    _candidates.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Error: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Existing submit logic retained but using shared Retrofit
    fun submitCandidateApplication(
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
        _submitSuccess.value = null

        viewModelScope.launch {
            try {
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

                val response = RetrofitInstance.api.submitApplication(request)

                if (response.isSuccessful) {
                     _submitSuccess.value = response.body()
                } else {
                    _errorMessage.value = response.message() ?: "Submission failed"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    // Profile / Dashboard State
    private val _profile = MutableStateFlow<AppUser?>(null)
    val profile: StateFlow<AppUser?> = _profile.asStateFlow()

    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = RetrofitInstance.api.getProfile(userId)
                if (result.isSuccessful && result.body()?.success == true) {
                    _profile.value = result.body()?.user
                } else {
                    _errorMessage.value = result.body()?.message ?: "Failed to load candidate profile."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Connection Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchActiveElection() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getActiveElection()
                if (response.isSuccessful && response.body() != null) {
                    _activeElection.value = response.body()
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    fun clearAllState() {
        _submitSuccess.value = null
        _errorMessage.value = null
    }

    private val _updateWithImageSuccess = MutableStateFlow<String?>(null)
    val updateWithImageSuccess: StateFlow<String?> = _updateWithImageSuccess.asStateFlow()

    fun updateProfile(
        userId: String,
        name: String,
        course: String,
        college: String,
        tagline: String,
        goals: String,
        pledges: String,
        symbolName: String,
        photo: String?,
        symbol: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = com.example.s_vote.model.UpdateProfileRequest(
                    userId, name, course, college, tagline, goals, pledges, symbolName, photo, symbol
                )
                val response = RetrofitInstance.api.updateProfile(request)
                if (response.isSuccessful) {
                    // Refresh profile
                    fetchProfile(userId)
                    _errorMessage.value = "Profile Updated!"
                } else {
                    _errorMessage.value = response.message() ?: "Update failed"
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun uploadImage(
        bitmap: android.graphics.Bitmap, 
        context: android.content.Context
    ): String? {
         // Convert bitmap to file and then MultipartBody.Part
         // This logic is complex to put inside ViewModel without helper.
         // Better to accept MultipartBody.Part directly from UI/Utils
         return null
    }

    fun uploadImageFile(part: okhttp3.MultipartBody.Part, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.uploadImage(part)
                if (response.isSuccessful) {
                    onResult(response.body()?.filePath)
                } else {
                    _errorMessage.value = response.message() ?: "Upload failed"
                    onResult(null)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                onResult(null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitFeedback(candidateId: String, userName: String, rating: Int, comment: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = com.example.s_vote.model.FeedbackRequest(candidateId, userName, rating, comment)
                val response = RetrofitInstance.api.submitFeedback(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    onResult(true, response.body()?.message ?: "Success")
                    // Refresh candidates to show new feedback if applicable
                    fetchCandidates() 
                } else {
                    onResult(false, response.body()?.message ?: "Failed")
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}