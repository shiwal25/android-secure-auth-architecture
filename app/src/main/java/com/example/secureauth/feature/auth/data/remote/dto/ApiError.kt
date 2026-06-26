package com.example.secureauth.feature.auth.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ApiError(
    @SerialName("message") val message: String? = null,
    @SerialName("error")   val error: String? = null
) {
    fun getReadableMessage(): String =
        message ?: error ?: "An unexpected error occurred"
}