package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class OcrResponse(
    @SerializedName("college_name") val collegeName: String,
    @SerializedName("student_name") val studentName: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("version") val version: String? = null,
    @SerializedName("is_quality_scan") val isQualityScan: Boolean = false
)
