package com.example.secureauth.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.secureauth.core.data.local.TokenDataStore
import com.example.secureauth.feature.auth.presentation.UserProfile
import com.example.secureauth.feature.home.data.remote.HomeApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface ProfileUiState {
    data object Loading                             : ProfileUiState
    data class  Success(val profile: UserProfile)  : ProfileUiState
    data class  Error(val message: String)          : ProfileUiState
}

class HomeViewModel(
    private val homeApiService: HomeApiService,
    tokenDataStore: TokenDataStore
) : ViewModel() {

    val userName: StateFlow<String?> = tokenDataStore.userName
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileUiState: StateFlow<ProfileUiState> = _profileUiState.asStateFlow()

    init { fetchProfile() }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileUiState.value = ProfileUiState.Loading
            try {
                val profile = homeApiService.getProfile()
                _profileUiState.value = ProfileUiState.Success(profile)
            } catch (e: IOException) {
                _profileUiState.value = ProfileUiState.Error("No internet connection")
            } catch (e: Exception) {
                _profileUiState.value =
                    ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }
}