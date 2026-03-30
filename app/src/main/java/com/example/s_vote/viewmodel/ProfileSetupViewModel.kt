package com.example.s_vote.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.SessionManager
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.ProfileSetupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProfileSetupViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    private val _setupState = MutableStateFlow<ProfileSetupState>(ProfileSetupState.Idle)
    val setupState = _setupState.asStateFlow()

    fun completeSetup(college: String, photoBitmap: Bitmap?) {
        if (college.isEmpty()) {
            _setupState.value = ProfileSetupState.Error("Please enter your college name")
            return
        }

        _setupState.value = ProfileSetupState.Loading

        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: ""
                
                var base64Photo: String? = null
                photoBitmap?.let {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()
                    base64Photo = "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
                }

                val response = RetrofitInstance.api.completeProfileSetup(
                    ProfileSetupRequest(userId, college, base64Photo)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    sessionManager.setProfileCompleted()
                    _setupState.value = ProfileSetupState.Success
                } else {
                    _setupState.value = ProfileSetupState.Error(response.body()?.message ?: "Failed to save profile")
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileSetupViewModel", "Setup failed", e)
                _setupState.value = ProfileSetupState.Error("Connection Failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _setupState.value = ProfileSetupState.Idle
    }
}

sealed class ProfileSetupState {
    object Idle : ProfileSetupState()
    object Loading : ProfileSetupState()
    object Success : ProfileSetupState()
    data class Error(val message: String) : ProfileSetupState()
}
