package com.example.security_frontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.security_frontend.dto.request.LoginRequest
import com.example.security_frontend.dto.request.RegisterRequest
import com.example.security_frontend.dto.response.LoginResponse
import com.example.security_frontend.dto.response.UserResponse
import com.example.security_frontend.jwtData.SessionManager
import com.example.security_frontend.repository.AuthRepository
import com.example.security_frontend.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<LoginResponse>?>(null)
    val loginState: StateFlow<Resource<LoginResponse>?> = _loginState

    private val _registerState = MutableStateFlow<Resource<UserResponse>?>(null)
    val registerState: StateFlow<Resource<UserResponse>?> = _registerState

    fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            _loginState.value = Resource.Loading()
            authRepository.login(loginRequest)
                .onSuccess {
                    sessionManager.saveToken(it.token)
                    _loginState.value = Resource.Success(it)
                }
                .onFailure { e ->
                    val message = when (e) {
                        is IOException -> e.message ?: "Network error"
                        else -> "Unexpected error: ${e.message}"
                    }
                    _loginState.value = Resource.Error(message)
                }
        }
    }

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            _registerState.value = Resource.Loading()
            authRepository.register(registerRequest)
                .onSuccess {
                    _registerState.value = Resource.Success(it)
                }
                .onFailure { e ->
                    val message = when (e) {
                        is IOException -> e.message ?: "Network error"
                        else -> "Unexpected error: ${e.message}"
                    }
                    _loginState.value = Resource.Error(message)
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearToken()
        }
    }
}
