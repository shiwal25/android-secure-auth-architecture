package com.example.secureauth.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.secureauth.core.navigation.Routes

@Composable
fun AuthNavGraph(authViewModel: AuthViewModel) {
    val backStack = remember { NavBackStack(Routes.LoginRoute, Routes.RegisterRoute) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Routes.LoginRoute> {
                LoginScreen(
                    authViewModel        = authViewModel,
                    onNavigateToRegister = { backStack.add(Routes.RegisterRoute) }
                )
            }
            entry<Routes.RegisterRoute> {
                RegisterScreen(
                    authViewModel     = authViewModel,
                    onNavigateToLogin = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}