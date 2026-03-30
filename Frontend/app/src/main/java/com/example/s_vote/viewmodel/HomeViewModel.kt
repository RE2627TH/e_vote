package com.example.s_vote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.ElectionStatus
import com.example.s_vote.api.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class HomeViewModel : ViewModel() {

    private val _electionStatus = MutableStateFlow(ElectionStatus())
    val electionStatus = _electionStatus.asStateFlow()
    
    private val _allElections = MutableStateFlow<List<ElectionStatus>>(emptyList())
    val allElections = _allElections.asStateFlow()
    
    private var countdownJob: kotlinx.coroutines.Job? = null
    
    // Countdown state
    private val _countdownDays = MutableStateFlow("00")
    val countdownDays = _countdownDays.asStateFlow()
    
    private val _countdownHours = MutableStateFlow("00")
    val countdownHours = _countdownHours.asStateFlow()
    
    private val _countdownMinutes = MutableStateFlow("00")
    val countdownMinutes = _countdownMinutes.asStateFlow()
    
    private val _countdownSeconds = MutableStateFlow("00")
    val countdownSeconds = _countdownSeconds.asStateFlow()

    private val _countdownLabel = MutableStateFlow("Election Ends in")
    val countdownLabel = _countdownLabel.asStateFlow()

    init {
        fetchActiveElection()
        fetchAllElections()
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // 5 seconds
                fetchActiveElection()
                fetchAllElections()
            }
        }
    }

    private fun fetchActiveElection() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getActiveElection()
                if (response.isSuccessful && response.body() != null) {
                    val election = response.body()!!
                    _electionStatus.value = election
                    
                    if (election.status == "UPCOMING") {
                        _countdownLabel.value = "Election Starts in"
                        startCountdown(election.startDate)
                    } else if (election.status == "ACTIVE") {
                        _countdownLabel.value = "Election Ends in"
                        startCountdown(election.endDate)
                    } else {
                        _countdownLabel.value = "Election Ended"
                        _countdownDays.value = "00"
                        _countdownHours.value = "00"
                        _countdownMinutes.value = "00"
                        _countdownSeconds.value = "00"
                        countdownJob?.cancel()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error fetching active election: ${e.message}", e)
            }
        }
    }

    fun fetchAllElections() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getElections()
                if (response.isSuccessful && response.body() != null) {
                    _allElections.value = response.body()!!
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Error fetching all elections: ${e.message}", e)
            }
        }
    }

    private fun startCountdown(endDateStr: String?) {
        if (endDateStr == null) return
        
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            // PHP usually returns time in server timezone. Ideally we sync timezones.
            // Assuming local emulator time matches server or slightly off.
            // format.timeZone = TimeZone.getTimeZone("UTC") // Uncomment if server is UTC
            
            val endDate = try {
                 format.parse(endDateStr)
            } catch (e: Exception) { null }

            if (endDate != null) {
                while (true) {
                    val now = System.currentTimeMillis()
                    val diff = endDate.time - now
                    
                    if (diff > 0) {
                        val d = TimeUnit.MILLISECONDS.toDays(diff)
                        val h = TimeUnit.MILLISECONDS.toHours(diff) % 24
                        val m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
                        val s = TimeUnit.MILLISECONDS.toSeconds(diff) % 60
                        
                        _countdownDays.value = "%02d".format(d)
                        _countdownHours.value = "%02d".format(h)
                        _countdownMinutes.value = "%02d".format(m)
                        _countdownSeconds.value = "%02d".format(s)
                    } else {
                         _countdownDays.value = "00"
                        _countdownHours.value = "00"
                        _countdownMinutes.value = "00"
                        _countdownSeconds.value = "00"
                        break
                    }
                    delay(1000)
                }
            }
        }
    }
}
