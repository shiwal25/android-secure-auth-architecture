package com.example.secureauth.core.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secureauth.feature.auth.presentation.AuthNavGraph
import com.example.secureauth.feature.auth.presentation.AuthState
import com.example.secureauth.feature.auth.presentation.AuthViewModel
import com.example.secureauth.feature.home.presentation.MainNavGraph
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootNavigation() {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = authState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "RootAuthStateTransition"
    ) { state ->
        when (state) {
            AuthState.Loading         -> SplashContent()
            AuthState.Unauthenticated -> AuthNavGraph(authViewModel = authViewModel)
            AuthState.Authenticated   -> MainNavGraph(authViewModel = authViewModel)
        }
    }
}

@Composable
private fun SplashContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}