package com.example.s_vote.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s_vote.api.OcrRetrofitInstance
import com.example.s_vote.api.RetrofitInstance
import com.example.s_vote.model.VerifyStudentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

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

    fun processIdCard(bitmap: Bitmap, context: Context) {
        _uiState.value = OcrState.Scanning

        viewModelScope.launch {
            try {
                // 1. Get Registered ID from session
                val sharedPref = context.getSharedPreferences("s_vote_prefs", Context.MODE_PRIVATE)
                val registeredId = sharedPref.getString("STUDENT_ID", "") ?: ""

                // 2. Convert Bitmap to MultipartBody.Part
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val byteArray = stream.toByteArray()
                val requestFile = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", "id_card.jpg", requestFile)

                // 3. Upload to Python OCR Server
                val ocrResponse = OcrRetrofitInstance.api.uploadOcrImage(body)

                if (ocrResponse.isSuccessful && ocrResponse.body() != null) {
                    val result = ocrResponse.body()!!
                    
                    if (result.version != "2.4") {
                        setError("Update Required: Please restart ocr_server.py (V2.4).")
                        return@launch
                    }

                    val ocrId = result.studentId.trim().uppercase()
                    val ocrName = result.studentName.trim()
                    val ocrDept = result.collegeName.trim()
                    
                    Log.d("OCR_ONLINE", "Scanned: [$ocrId], Quality: ${result.isQualityScan}")

                    // "No-Fail" Strategy: Always return to detail screen with what we found
                    _uiState.value = OcrState.Detected(ocrId, ocrName, ocrDept)
                } else {
                    setError("OCR Server Error: ${ocrResponse.code()}")
                }

            } catch (e: Exception) {
                Log.e("OCR_ERROR", "Scanner Exception", e)
                setError("Network/Scan Error: ${e.message}")
            }
        }
    }

    private fun isOcrNumeric(text: String): Boolean = text.all { it.isDigit() }


    fun verifyStudentId(regNo: String) {
        verifyStudentWithBackend(regNo)
    }

    private fun verifyStudentWithBackend(regNo: String) {
        _uiState.value = OcrState.Verifying

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.verifyStudent(VerifyStudentRequest(regNo))
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success && result.studentData != null) {
                        _uiState.value = OcrState.Verified(result.studentData)
                    } else {
                        setError("${result.message}\n(Scanned ID: $regNo)")
                    }
                } else {
                    setError("Verification failed: Server error or invalid ID")
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
    data class Detected(val id: String, val name: String, val dept: String) : OcrState()
    data class Verified(val studentData: com.example.s_vote.model.StudentData) : OcrState()
    data class Error(val message: String) : OcrState()
}
