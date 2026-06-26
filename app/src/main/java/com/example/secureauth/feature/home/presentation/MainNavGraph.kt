package com.example.secureauth.feature.home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.secureauth.core.navigation.Routes
import com.example.secureauth.feature.auth.presentation.AuthViewModel

@Composable
fun MainNavGraph(authViewModel: AuthViewModel) {
    val backStack = remember { NavBackStack(Routes.HomeRoute) }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<Routes.HomeRoute> {
                HomeScreen(authViewModel = authViewModel)
            }
        }
    )
}