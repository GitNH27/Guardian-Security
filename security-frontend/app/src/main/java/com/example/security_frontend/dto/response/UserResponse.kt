package com.example.security_frontend.dto.response

data class UserResponse(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String
)