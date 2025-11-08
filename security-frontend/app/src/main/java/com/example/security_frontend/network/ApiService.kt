package com.example.security_frontend.network

import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.AuthResponse
import com.example.security_frontend.dto.response.UserResponse
import com.example.security_frontend.dto.response.VerifyUserResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/verifycode")
    suspend fun sendVerificationCode(@Body registerRequest: RegisterRequest): Response<VerifyUserResponse>

    @POST("auth/verify-registration")
    suspend fun verifyRegistration(
        @Body registerRequest: RegisterRequest,
        @Query("code") code: String
    ): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
}