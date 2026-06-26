package com.example.secureauth.feature.auth.presentation

sealed interface AuthState {
    data object Loading         : AuthState
    data object Unauthenticated : AuthState
    data object Authenticated   : AuthState
}