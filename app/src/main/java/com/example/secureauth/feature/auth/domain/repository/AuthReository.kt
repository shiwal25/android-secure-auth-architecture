package com.example.secureauth.feature.auth.domain.repository

interface AuthRepository {

    /**
     * Logs the user in. On success, saves tokens + profile to DataStore.
     * The DataStore emission drives authState to Authenticated via AuthViewModel.
     */
    suspend fun login(email: String, password: String): Result<Unit>

    /**
     * Registers a new user. On success, saves tokens + profile to DataStore.
     */
    suspend fun register(name: String, email: String, password: String): Result<Unit>

    /**
     * Attempts to refresh the access token using the stored refresh token.
     * Called only at startup if the locally-stored token is found to be expired.
     * Mid-session refresh is handled automatically by the Ktor Auth plugin.
     */
    suspend fun refreshToken(): Result<Unit>

    /**
     * Clears local session. Optionally calls backend /logout to revoke refresh token.
     */
    suspend fun logout()
}