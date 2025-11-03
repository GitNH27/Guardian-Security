package com.example.security_frontend.repository

import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.LoginResponse
import com.example.security_frontend.dto.response.UserResponse
import com.example.security_frontend.network.ApiService
import java.io.IOException

class AuthRepository(private val apiService: ApiService) {

    suspend fun register(registerRequest: RegisterRequest): Result<UserResponse> {
        return runCatching {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw IOException("Registration failed: ${response.errorBody()?.string() ?: response.message()}")
            }
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return runCatching {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                throw IOException("Login failed: ${response.errorBody()?.string() ?: response.message()}")
            }
        }
    }
}
