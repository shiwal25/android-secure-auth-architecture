package com.example.secureauth.feature.auth.presentation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    @SerialName("name")  val name: String  = "",
    @SerialName("email") val email: String = ""
)