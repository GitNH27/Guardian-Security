package com.example.security_frontend.dto.response

data class LoginResponse(
    val token: String,
    val userResponse: UserResponse
)