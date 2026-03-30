package com.example.s_vote.repository

import com.example.s_vote.api.ApiClient
import com.example.s_vote.model.*
import retrofit2.Response


class AuthRepository {

    suspend fun loginUser(request: LoginRequest): Response<LoginResponse> {
        return ApiClient.api.loginUser(request)
    }

    suspend fun registerUser(request: RegisterRequest): Response<RegisterResponse> {
        return ApiClient.api.registerUser(request)
    }
}
