package com.example.security_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.AuthResponse
import com.example.security_frontend.dto.response.UserResponse
import com.example.security_frontend.dto.response.VerifyUserResponse
import com.example.security_frontend.jwtData.SessionManager
import com.example.security_frontend.repository.AuthRepository
import com.example.security_frontend.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // For storing user details between registration steps
    private var pendingRegisterRequest: RegisterRequest? = null

    private val _loginState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val loginState: StateFlow<Resource<AuthResponse>?> = _loginState

    private val _sendCodeState = MutableStateFlow<Resource<VerifyUserResponse>?>(null)
    val sendCodeState: StateFlow<Resource<VerifyUserResponse>?> = _sendCodeState

    private val _verifyRegistrationState = MutableStateFlow<Resource<UserResponse>?>(null)
    val verifyRegistrationState: StateFlow<Resource<UserResponse>?> = _verifyRegistrationState

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            authRepository.login(loginRequest)
                .onSuccess {
                    sessionManager.saveToken(it.token)
                    _loginState.value = Resource.Success(it)
                }
                .onFailure { e ->
                    _loginState.value = Resource.Error(e.message ?: "An unknown login error occurred.")
                }
        }
    }

    // Step 1: Send user details to get verification code
    fun sendVerificationCode(registerRequest: RegisterRequest) {
        pendingRegisterRequest = registerRequest // <-- Store the user's details
        viewModelScope.launch {
            _sendCodeState.value = Resource.Loading()
            authRepository.sendVerificationCode(registerRequest)
                .onSuccess {
                    _sendCodeState.value = Resource.Success(it)
                }
                .onFailure { e ->
                    _sendCodeState.value = Resource.Error(e.message ?: "Failed to send verification code.")
                }
        }
    }

    // Step 2: Send the code to complete registration
    fun verifyRegistration(code: String) {
        pendingRegisterRequest?.let { request ->
            viewModelScope.launch {
                _verifyRegistrationState.value = Resource.Loading()
                authRepository.verifyRegistration(request, code)
                    .onSuccess {
                        _verifyRegistrationState.value = Resource.Success(it)
                        pendingRegisterRequest = null // Clear after successful registration
                    }
                    .onFailure { e ->
                        _verifyRegistrationState.value = Resource.Error(e.message ?: "Verification failed.")
                    }
            }
        } ?: run {
            _verifyRegistrationState.value = Resource.Error("Registration details were lost. Please start over.")
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearToken()
        }
    }

    // Reset states, e.g., when navigating away from a screen
    fun clearStates() {
        _loginState.value = null
        _sendCodeState.value = null
        _verifyRegistrationState.value = null
    }
}
