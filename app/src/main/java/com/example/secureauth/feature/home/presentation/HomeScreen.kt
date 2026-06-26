package com.example.secureauth.feature.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.secureauth.feature.auth.presentation.AuthViewModel
import com.example.secureauth.feature.auth.presentation.UserProfile
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(authViewModel: AuthViewModel) {
    val homeViewModel: HomeViewModel = koinViewModel()
    val userName     by homeViewModel.userName.collectAsStateWithLifecycle()
    val profileState by homeViewModel.profileUiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        userName = userName,
        profileState = profileState,
        onRetryClick = { homeViewModel.fetchProfile() },
        onLogoutClick = { authViewModel.logout() }
    )
}

@Composable
fun HomeScreenContent(
    userName: String?,
    profileState: ProfileUiState,
    onRetryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {

        Text(
            text  = if (userName != null) "Welcome back, $userName!" else "Welcome back!",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(32.dp))

        when (val state = profileState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is ProfileUiState.Success -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Name: ${state.profile.name}")
                        Text("Email: ${state.profile.email}")
                    }
                }
            }
            is ProfileUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = state.message,
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onRetryClick) {
                            Text("Retry")
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Log Out")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenSuccessPreview() {
    HomeScreenContent(
        userName = "Shivam",
        profileState = ProfileUiState.Success(profile = UserProfile("Shivam","shivam@gmail.com")),
        onRetryClick = {},
        onLogoutClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenLoadingPreview() {
    HomeScreenContent(
        userName = null,
        profileState = ProfileUiState.Loading,
        onRetryClick = {},
        onLogoutClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun HomeScreenErrorPreview() {
    HomeScreenContent(
        userName = "Shivam",
        profileState = ProfileUiState.Error(message = "Unable to connect to secure server."),
        onRetryClick = {},
        onLogoutClick = {}
    )
}