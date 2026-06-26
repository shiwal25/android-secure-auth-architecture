package com.example.secureauth.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureauth.core.data.local.TokenDataStore
import com.example.secureauth.core.util.JwtUtils
import com.example.secureauth.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _loginUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginUiState: StateFlow<AuthUiState> = _loginUiState.asStateFlow()

    private val _registerUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerUiState: StateFlow<AuthUiState> = _registerUiState.asStateFlow()

    private var isFirstEmission = true

    init {
        observeTokenForAuthState()
    }

    private fun observeTokenForAuthState() {
        viewModelScope.launch {
            tokenDataStore.accessToken.collect { token ->
                if (isFirstEmission) {
                    isFirstEmission = false
                    handleStartup(token)
                } else {
                    // Subsequent emissions:
                    // Non-null → authenticated (login saved a token, or refresh succeeded)
                    // Null     → unauthenticated (logout cleared session, or forced logout)
                    _authState.value = if (token != null) {
                        AuthState.Authenticated
                    } else {
                        AuthState.Unauthenticated
                    }
                }
            }
        }
    }

    private suspend fun handleStartup(token: String?) {
        when {
            token == null -> {
                _authState.value = AuthState.Unauthenticated
            }
            !JwtUtils.isTokenExpired(token) -> {
                _authState.value = AuthState.Authenticated
            }
            else -> {
                val result = authRepository.refreshToken()
                _authState.value = if (result.isSuccess) {
                    AuthState.Authenticated
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (_loginUiState.value == AuthUiState.Loading) return
        viewModelScope.launch {
            _loginUiState.value = AuthUiState.Loading
            val result = authRepository.login(email, password)
            _loginUiState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (_registerUiState.value == AuthUiState.Loading) return
        viewModelScope.launch {
            _registerUiState.value = AuthUiState.Loading
            val result = authRepository.register(name, email, password)
            _registerUiState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun resetLoginState()    { _loginUiState.value    = AuthUiState.Idle }
    fun resetRegisterState() { _registerUiState.value = AuthUiState.Idle }
}