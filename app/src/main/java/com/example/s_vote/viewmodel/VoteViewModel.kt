package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.VoteRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoteViewModel : ViewModel() {

    private val _voteState = MutableStateFlow<VoteState>(VoteState.Idle)
    val voteState = _voteState.asStateFlow()

    fun castVote(userId: String, candidateId: String, position: String) {
        _voteState.value = VoteState.Loading
        viewModelScope.launch {
            try {
                // 1. Fetch Active Election ID
                val electionResponse = RetrofitInstance.api.getActiveElection()
                val electionId: String = if (electionResponse.isSuccessful && electionResponse.body() != null) {
                    electionResponse.body()!!.id
                } else {
                    "1" // Default to "1" as a String
                }

                // 2. Cast Vote
                val request = VoteRequest(
                    userId = userId,
                    candidateId = candidateId,
                    position = position,
                    electionId = electionId.toIntOrNull() ?: 1
                )

                val response = RetrofitInstance.api.castVote(request)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        _voteState.value = VoteState.Success
                    } else {
                        _voteState.value = VoteState.Error(result.message)
                    }
                } else {
                     _voteState.value = VoteState.Error("Vote failed: ${response.message()}")
                }

            } catch (e: Exception) {
                _voteState.value = VoteState.Error("Network error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _voteState.value = VoteState.Idle
    }
}

sealed class VoteState {
    object Idle : VoteState()
    object Loading : VoteState()
    object Success : VoteState()
    data class Error(val message: String) : VoteState()
}