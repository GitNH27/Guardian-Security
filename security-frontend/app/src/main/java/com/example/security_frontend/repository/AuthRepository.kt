package com.example.security_frontend.repository

import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.LoginResponse
import com.example.security_frontend.dto.response.UserResponse
import com.example.security_frontend.network.ApiService
import java.io.IOException
import org.json.JSONObject

class AuthRepository(private val apiService: ApiService) {

    // Helper function to extract clean message from backend
    private fun parseErrorMessage(json: String?): String {
        if (json.isNullOrBlank()) return "Unknown error"

        return try {
            val jsonObject = JSONObject(json)
            when {
                jsonObject.has("error") -> jsonObject.getString("error")
                jsonObject.keys().hasNext() -> {
                    // handle validation errors like {"email": "Invalid email format"}
                    val key = jsonObject.keys().next()
                    "${key.capitalize()}: ${jsonObject.getString(key)}"
                }
                else -> "Unknown error"
            }
        } catch (e: Exception) {
            "Error parsing response"
        }
    }

    suspend fun register(registerRequest: RegisterRequest): Result<UserResponse> {
        return runCatching {
            val response = apiService.register(registerRequest)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMsg = try {
                    response.errorBody()?.string()?.let { json ->
                        // crude parsing to extract "error" field
                        JSONObject(json).optString("error", "Registration failed")
                    } ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                throw IOException(errorMsg)
            }
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<LoginResponse> {
        return runCatching {
            val response = apiService.login(loginRequest)

            if (response.isSuccessful && response.body() != null) {
                response.body()!!
            } else {
                val errorMsg = response.errorBody()?.string() ?: response.message()
                throw IOException(errorMsg)
            }
        }
    }
}
