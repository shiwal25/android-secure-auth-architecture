package com.example.secureauth.feature.auth.domain.repository

import com.example.secureauth.core.data.local.TokenDataStore
import com.example.secureauth.feature.auth.data.remote.AuthApiService
import com.example.secureauth.feature.auth.data.remote.dto.ApiError
import com.example.secureauth.feature.auth.data.remote.dto.AuthResponse
import com.example.secureauth.feature.auth.data.remote.dto.LoginRequest
import com.example.secureauth.feature.auth.data.remote.dto.RefreshRequest
import com.example.secureauth.feature.auth.data.remote.dto.RefreshResponse
import com.example.secureauth.feature.auth.data.remote.dto.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val tokenDataStore: TokenDataStore,
    private val plainClient: HttpClient,
    private val baseUrl: String
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.status == HttpStatusCode.OK) {
                val body = response.body<AuthResponse>()
                tokenDataStore.saveSession(
                    accessToken  = body.accessToken,
                    refreshToken = body.refreshToken,
                    userName     = body.name,
                    userId       = body.userId
                )
                Result.success(Unit)
            } else {
                val error = runCatching { response.body<ApiError>() }.getOrNull()
                Result.failure(Exception(error?.getReadableMessage() ?: "Login failed"))
            }

        } catch (e: IOException) {
            // Network-level failure: no internet, DNS failure, timeout
            Result.failure(Exception("No internet connection. Please check your network."))
        } catch (e: Exception) {
            // Programming error: JSON parse failure, unexpected null — surface for debugging
            Result.failure(Exception(e.message ?: "An unexpected error occurred"))
        }
    }

    override suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            val response = apiService.register(RegisterRequest(name, email, password))

            if (response.status == HttpStatusCode.OK ||
                response.status == HttpStatusCode.Created) {
                val body = response.body<AuthResponse>()
                tokenDataStore.saveSession(
                    accessToken  = body.accessToken,
                    refreshToken = body.refreshToken,
                    userName     = body.name,
                    userId       = body.userId
                )
                Result.success(Unit)
            } else {
                val error = runCatching { response.body<ApiError>() }.getOrNull()
                Result.failure(Exception(error?.getReadableMessage() ?: "Registration failed"))
            }

        } catch (e: IOException) {
            Result.failure(Exception("No internet connection. Please check your network."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "An unexpected error occurred"))
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        val storedRefreshToken = tokenDataStore.refreshToken.first()

        if (storedRefreshToken == null) {
            tokenDataStore.clearSession()
            return Result.failure(Exception("No refresh token available"))
        }

        return try {
            val response = plainClient.post("$baseUrl/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken = storedRefreshToken))
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    val body = response.body<RefreshResponse>()
                    tokenDataStore.saveTokens(
                        accessToken  = body.accessToken,
                        refreshToken = body.refreshToken
                    )
                    Result.success(Unit)
                }
                HttpStatusCode.Unauthorized,
                HttpStatusCode.Forbidden -> {
                    // Refresh token revoked — must log out
                    tokenDataStore.clearSession()
                    Result.failure(Exception("Session expired. Please log in again."))
                }
                else -> {
                    // 5xx — do NOT clear session, might be a temporary server error
                    Result.failure(Exception("Server error. Please try again."))
                }
            }

        } catch (e: IOException) {
            // Expired token + no internet = can't recover — send to login
            tokenDataStore.clearSession()
            Result.failure(Exception("No internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Refresh failed"))
        }
    }

    override suspend fun logout() {
        // Optionally call backend /logout here to revoke the server-side refresh token.
        // Even if it fails, clear locally so the user is logged out on-device.
        tokenDataStore.clearSession()
    }
}