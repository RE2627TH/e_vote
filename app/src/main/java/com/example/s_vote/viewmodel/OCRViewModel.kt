package com.example.s_vote.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.VerifyStudentRequest
import com.example.s_vote.ocr.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OcrViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<OcrState>(OcrState.Idle)
    val uiState = _uiState.asStateFlow()

    private var lastErrorTime = 0L

    private fun setError(message: String) {
        val now = System.currentTimeMillis()
        if (now - lastErrorTime > 1500) {
            _uiState.value = OcrState.Error(message)
            lastErrorTime = now
        }
    }

    /**
     * MAIN ENTRY POINT
     * Call this after camera / gallery gives bitmap
     */
    fun processIdCard(bitmap: Bitmap, context: Context) {
        _uiState.value = OcrState.Scanning

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            try {
                // 0️⃣ Downscale image ONCE
                val resizedBitmap = ImagePreprocessor.resize(bitmap, 1024)

                var finalRegNo: String? = null
                var lastRawOCR = ""
                val rotations = listOf(0f, 90f, 270f, 180f)

                for (rotation in rotations) {
                    val workingBitmap = if (rotation == 0f) resizedBitmap else ImagePreprocessor.rotate(resizedBitmap, rotation)

                    // 1️⃣ Preprocess
                    val gray = ImagePreprocessor.toGrayscale(workingBitmap)
                    val cleanBitmap = ImagePreprocessor.applyThreshold(gray)

                    // 2️⃣ Crop & OCR College
                    val collegeBitmap = CropUtils.cropCollege(cleanBitmap)
                    val rawCollege = CollegeOcrEngine.readCollegeName(context, collegeBitmap)
                    lastRawOCR = rawCollege // Capture for error reporting

                    val validCollege = CollegeValidator.validate(rawCollege)

                    // 3️⃣ OCR Register Number (Scan FULL Image because ID might be anywhere)
                    // We don't cropRegNo anymore because the box is full screen.
                    val regNo = RegisterOcrEngine.readRegisterNumber(context, cleanBitmap)
                    Log.d("OCR", "Rotation $rotation - College: $validCollege - Reg: $regNo")

                    // Success Condition 1: Valid Register Number found (Strongest signal)
                    if (regNo.length >= 7 && regNo.count { it.isDigit() } >= 6) { 
                         // Check for at least 6-7 digits to be sure it's not noise
                         finalRegNo = regNo.filter { it.isDigit() }
                         break 
                    }

                    // Success Condition 2: Valid College Found + Some digits
                    if (validCollege != null) {
                         // If regNo failed strict check but we have college, try lenient reg extraction
                         val digits = regNo.filter { it.isDigit() }
                         if (digits.length >= 5) {
                             finalRegNo = digits
                             break
                         }
                    }
                }

                if (finalRegNo != null) {
                    verifyStudentWithBackend(finalRegNo)
                } else {
                    val msg = if (lastRawOCR.length > 20) lastRawOCR.substring(0, 20) + "..." else lastRawOCR
                    setError("ID Failed. Saw: '$msg'. Card must be in purple box.")
                }

            } catch (e: Exception) {
                setError("OCR Failed: ${e.message}")
            }
        }
    }

    fun verifyStudentId(regNo: String) {
        verifyStudentWithBackend(regNo)
    }

    private fun verifyStudentWithBackend(regNo: String) {
        _uiState.value = OcrState.Verifying

        viewModelScope.launch {
            try {
                val response =
                    RetrofitInstance.api.verifyStudent(
                        VerifyStudentRequest(regNo)
                    )

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success && result.studentData != null) {
                        _uiState.value =
                            OcrState.Verified(result.studentData)
                    } else {
                        setError("${result.message}\n(Scanned ID: $regNo)")
                    }
                } else {
                    setError("Verification failed: Server error")
                }
            } catch (e: Exception) {
                setError("Network error: ${e.message}")
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
