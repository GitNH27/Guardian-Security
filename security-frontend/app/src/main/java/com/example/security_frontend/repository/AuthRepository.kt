package com.example.security_frontend.repository

import com.example.security_frontend.dto.LoginRequest
import com.example.security_frontend.dto.RegisterRequest
import com.example.security_frontend.network.ApiService

class AuthRepository(private val apiService: ApiService) {

    suspend fun register(registerRequest: RegisterRequest) = apiService.register(registerRequest)

    suspend fun login(loginRequest: LoginRequest) = apiService.login(loginRequest)

}