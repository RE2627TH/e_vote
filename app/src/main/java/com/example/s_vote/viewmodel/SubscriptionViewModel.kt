package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.SubscriptionRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SubscriptionState {
    object Idle : SubscriptionState()
    object Loading : SubscriptionState()
    object Success : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

class SubscriptionViewModel : ViewModel() {
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Idle)
    val subscriptionState = _subscriptionState.asStateFlow()

    fun completePayment(userId: String) {
        viewModelScope.launch {
            _subscriptionState.value = SubscriptionState.Loading
            try {
                val response = RetrofitInstance.api.updateSubscription(
                    SubscriptionRequest(userId, "PAY_" + System.currentTimeMillis())
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _subscriptionState.value = SubscriptionState.Success
                } else {
                    _subscriptionState.value = SubscriptionState.Error(response.body()?.message ?: "Payment failed")
                }
            } catch (e: Exception) {
                _subscriptionState.value = SubscriptionState.Error("Connection Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _subscriptionState.value = SubscriptionState.Idle
    }
}
