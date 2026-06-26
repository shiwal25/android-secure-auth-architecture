package com.example.secureauth.feature.auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit
) {
    val uiState by authViewModel.loginUiState.collectAsStateWithLifecycle()
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    DisposableEffect(Unit) { onDispose { authViewModel.resetLoginState() } }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState is AuthUiState.Error,
            textStyle = TextStyle(color = Color.Black),
            placeholder = {
                Text("Email")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Blue,
                unfocusedLabelColor = Color.Gray,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray
            )
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState is AuthUiState.Error,
            textStyle = TextStyle(color = Color.Black),
            placeholder = {
                Text("Password")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedLabelColor = Color.Blue,
                unfocusedLabelColor = Color.Gray,
                focusedBorderColor = Color.Blue,
                unfocusedBorderColor = Color.Gray
            )
        )

        if (uiState is AuthUiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState != AuthUiState.Loading
        ) {
            if (uiState == AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Log In")
            }
        }
        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}