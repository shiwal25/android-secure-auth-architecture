package com.example.secureauth.feature.auth.presentation

sealed interface AuthUiState {
    data object Idle    : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class  Error(val message: String) : AuthUiState
}