package com.example.security_frontend.network

import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.LoginResponse
import com.example.security_frontend.dto.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<UserResponse>

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}