package com.example.security_frontend.network

import com.example.security_frontend.data.LoginRequest
import com.example.security_frontend.data.LoginResponse
import com.example.security_frontend.data.RegisterRequest
import com.example.security_frontend.data.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<UserResponse>

    @POST("api/auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
}