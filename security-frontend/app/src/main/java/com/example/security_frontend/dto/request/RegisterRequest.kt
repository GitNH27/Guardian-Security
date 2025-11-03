package com.example.security_frontend.dto.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
)