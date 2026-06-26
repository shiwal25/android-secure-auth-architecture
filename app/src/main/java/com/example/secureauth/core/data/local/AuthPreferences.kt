package com.example.secureauth.core.data.local

import kotlinx.serialization.Serializable

@Serializable
data class AuthPreferences(
    val accessToken: String  = "",
    val refreshToken: String = "",
    val userName: String     = "",
    val userId: String       = ""
)