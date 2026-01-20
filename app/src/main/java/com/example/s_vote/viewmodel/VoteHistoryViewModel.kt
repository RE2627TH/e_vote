package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.VoteHistoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class VoteHistoryState {
    object Idle : VoteHistoryState()
    object Loading : VoteHistoryState()
    data class Success(val theHistory: List<VoteHistoryItem>) : VoteHistoryState()
    data class Error(val message: String) : VoteHistoryState()
}

class VoteHistoryViewModel : ViewModel() {

    private val _historyState = MutableStateFlow<VoteHistoryState>(VoteHistoryState.Idle)
    val historyState: StateFlow<VoteHistoryState> = _historyState

    fun fetchVoteHistory(voterId: String) {
        if (voterId.isEmpty()) return
        
        viewModelScope.launch {
            _historyState.value = VoteHistoryState.Loading
            try {
                val response = RetrofitInstance.api.getVoteHistory(voterId)
                if (response.isSuccessful && response.body() != null) {
                    _historyState.value = VoteHistoryState.Success(response.body()!!)
                } else {
                    _historyState.value = VoteHistoryState.Error("Failed to fetch history")
                }
            } catch (e: Exception) {
                _historyState.value = VoteHistoryState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
