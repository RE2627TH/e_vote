package com.example.s_vote.api

import com.example.s_vote.ElectionStatus
import com.example.s_vote.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    /* =======================
       1. AUTH
       ======================= */

    @POST("register.php")
    suspend fun registerUser(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

    @POST("login.php")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("reset_password.php")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<GenericResponse>


    /* =======================
       2. CANDIDATE
       ======================= */

    @POST("submit_application.php")
    suspend fun submitApplication(
        @Body request: CandidateApplicationRequest
    ): Response<CandidateApplicationResponse>

    @POST("update_profile.php")
    suspend fun updateUserProfile(
        @Body request: UpdateUserProfileRequest
    ): Response<GenericResponse>

    @POST("update_candidate_profile.php")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<GenericResponse>

    @Multipart
    @POST("upload_image.php")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("submit_feedback.php")
    suspend fun submitFeedback(
        @Body request: FeedbackRequest
    ): Response<GenericResponse>


    /* =======================
       3. VOTING
       ======================= */

    @POST("vote.php")
    suspend fun castVote(
        @Body request: VoteRequest
    ): Response<GenericResponse>


    /* =======================
       4. VERIFICATION
       ======================= */

    @POST("verify_student.php")
    suspend fun verifyStudent(
        @Body request: VerifyStudentRequest
    ): Response<VerifyStudentResponse>


    /* =======================
       5. GET DATA (USER)
       ======================= */

    @GET("get_candidates.php")
    suspend fun getCandidates(): Response<List<Candidate>>

    // ✅ IMPORTANT: Profile uses ProfileResponse (NOT LoginResponse)
    @GET("get_profile.php")
    suspend fun getProfile(
        @Query("user_id") userId: String
    ): Response<ProfileResponse>


    /* =======================
       6. ADMIN
       ======================= */

    @GET("get_candidates.php")
    suspend fun getCandidatesByStatus(
        @Query("status") status: String
    ): Response<List<Candidate>>

    @GET("admin_approve.php")
    suspend fun manageCandidate(
        @Query("user_id") userId: String,
        @Query("action") action: String
    ): Response<GenericResponse>

    @GET("get_results.php")
    suspend fun getResults(): Response<List<ElectionResult>>

    @POST("create_election.php")
    suspend fun createElection(
        @Body request: CreateElectionRequest
    ): Response<GenericResponse>

    @GET("get_active_election.php")
    suspend fun getActiveElection(): Response<ElectionStatus>

    @GET("dashboard_stats.php")
    suspend fun getDashboardStats(): Response<AdminDashboardStatsResponse>
}
