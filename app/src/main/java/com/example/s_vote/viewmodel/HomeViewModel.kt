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
    
    // Countdown state
    private val _countdownDays = MutableStateFlow("00")
    val countdownDays = _countdownDays.asStateFlow()
    
    private val _countdownHours = MutableStateFlow("00")
    val countdownHours = _countdownHours.asStateFlow()
    
    private val _countdownMinutes = MutableStateFlow("00")
    val countdownMinutes = _countdownMinutes.asStateFlow()
    
    private val _countdownSeconds = MutableStateFlow("00")
    val countdownSeconds = _countdownSeconds.asStateFlow()

    init {
        fetchActiveElection()
    }

    private fun fetchActiveElection() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getActiveElection()
                if (response.isSuccessful && response.body() != null) {
                    _electionStatus.value = response.body()!!
                    startCountdown(response.body()!!.endDate)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startCountdown(endDateStr: String?) {
        if (endDateStr == null) return
        
        viewModelScope.launch {
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
