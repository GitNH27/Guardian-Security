package com.example.security_frontend.repository

import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.AuthResponse
import com.example.security_frontend.dto.response.UserResponse
import com.example.security_frontend.dto.response.VerifyUserResponse
import com.example.security_frontend.network.ApiService
import java.io.IOException

class AuthRepository(private val apiService: ApiService) {

    suspend fun sendVerificationCode(registerRequest: RegisterRequest): Result<VerifyUserResponse> {
        return try {
            // Note: apiService.sendVerificationCode must now return Response<VerifyUserResponse>
            val response = apiService.sendVerificationCode(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException("Failed to send verification code: ${response.errorBody()?.string() ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyRegistration(registerRequest: RegisterRequest, code: String): Result<UserResponse> {
        return try {
            val response = apiService.verifyRegistration(registerRequest, code)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException("Registration verification failed: ${response.errorBody()?.string() ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(loginRequest)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException("Login failed: ${response.errorBody()?.string() ?: response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
