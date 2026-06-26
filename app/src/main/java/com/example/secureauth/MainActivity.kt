package com.example.secureauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.secureauth.core.navigation.RootNavigation
import com.example.secureauth.ui.theme.SecureAuthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SecureAuthTheme {
                RootNavigation()
            }
        }
    }
}