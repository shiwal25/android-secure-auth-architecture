package com.example.secureauth.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Routes : NavKey{
    @Serializable
    data object LoginRoute    : Routes
    @Serializable
    data object RegisterRoute : Routes
    @Serializable
    data object HomeRoute     : Routes
}