package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.ElectionStatus
import com.example.s_vote.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    private val _electionStatus = MutableStateFlow<ElectionStatus?>(null)
    val electionStatus = _electionStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun fetchElectionStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitInstance.api.getActiveElection()
                if (response.isSuccessful && response.body() != null) {
                    _electionStatus.value = response.body()
                    // If published, fetch results immediately
                    if (response.body()!!.isPublished) {
                        fetchResults()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _results = MutableStateFlow<List<com.example.s_vote.model.ElectionResult>>(emptyList())
    val results = _results.asStateFlow()

    fun fetchResults() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getResults()
                if (response.isSuccessful && response.body() != null) {
                    _results.value = response.body()!!
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
