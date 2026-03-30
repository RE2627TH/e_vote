package com.example.s_vote.model

import com.google.gson.annotations.SerializedName

data class AppNotification(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
    @SerializedName("screen") val screen: String? = null,
    @SerializedName("data_id") val dataId: String? = null,
    @SerializedName("is_read") val isRead: Int = 0,
    @SerializedName("created_at") val createdAt: String
)

data class NotificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("notifications") val notifications: List<AppNotification> = emptyList()
)
