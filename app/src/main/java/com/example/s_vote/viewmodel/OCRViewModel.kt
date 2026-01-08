package com.example.s_vote.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.VerifyStudentRequest
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class OcrViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<OcrState>(OcrState.Idle)
    val uiState = _uiState.asStateFlow()

    private var lastErrorTime = 0L

    private fun setError(message: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastErrorTime > 2000) {
            _uiState.value = OcrState.Error(message)
            lastErrorTime = currentTime
        }
    }

    fun processImage(uri: Uri, context: Context) {
        _uiState.value = OcrState.Scanning
        try {
            val image = InputImage.fromFilePath(context, uri)
            processInputImage(image)
        } catch (e: Exception) {
            setError("Error processing image: ${e.message}")
        }
    }

    fun processBitmap(bitmap: android.graphics.Bitmap) {
        _uiState.value = OcrState.Scanning
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            processInputImage(image)
        } catch (e: Exception) {
            setError("Error processing bitmap: ${e.message}")
        }
    }

    private fun processInputImage(image: InputImage) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val text = visionText.text
                Log.d("OCR", "Extracted: $text")
                validateExtractedText(text)
            }
            .addOnFailureListener { e ->
                setError("Failed to scan ID: ${e.message}")
            }
    }

    private fun validateExtractedText(text: String) {
        // 1. Verify College Name
        if (!text.contains("Saveetha", ignoreCase = true)) {
            setError("Invalid ID Card: College verification failed")
            return
        }

        // 2. Extract Student ID (assuming it's a number/alphanumeric pattern)
        // Adjust regex based on actual ID format (e.g., 12345678 or RR2021...)
        val idPattern = Pattern.compile("\\b\\d{8,}\\b") // Matches 8+ digits
        val matcher = idPattern.matcher(text)

        if (matcher.find()) {
            val studentId = matcher.group()
            verifyStudentWithBackend(studentId)
        } else {
            // Fallback: Try alphanumeric if digits fail (e.g. 19EUS101)
            val alphaNumPattern = Pattern.compile("\\b[0-9]{2}[A-Z]{3}[0-9]{3}\\b")
            val alphaMatcher = alphaNumPattern.matcher(text)
            
            if (alphaMatcher.find()) {
                 verifyStudentWithBackend(alphaMatcher.group())
            } else {
                setError("Could not detect Student ID")
            }
        }
    }

    private fun verifyStudentWithBackend(studentId: String) {
        _uiState.value = OcrState.Verifying

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.verifyStudent(VerifyStudentRequest(studentId))
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success && result.studentData != null) {
                        _uiState.value = OcrState.Verified(result.studentData)
                    } else {
                        setError(result.message)
                    }
                } else {
                    setError("Verification Failed: Server Error")
                }
            } catch (e: Exception) {
                setError("Network Error: ${e.message}")
            }
        }
    }
    fun resetState() {
        _uiState.value = OcrState.Idle
    }
}

sealed class OcrState {
    object Idle : OcrState()
    object Scanning : OcrState()
    object Verifying : OcrState()
    data class Verified(val studentData: com.example.s_vote.model.StudentData) : OcrState()
    data class Error(val message: String) : OcrState()
}