package com.example.secureauth.feature.home.data.remote

import com.example.secureauth.feature.auth.presentation.UserProfile
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get


class HomeApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {
    /**
     * This call goes through the authenticated HttpClient.
     * The built-in Auth plugin:
     *   1. Attaches Authorization: Bearer <token> (via sendWithoutRequest)
     *   2. If backend returns 401, silently calls refreshTokens {}
     *   3. Saves new tokens, retries this exact request
     *   4. Returns the successful response
     * HomeViewModel never knows any of this happened.
     */
    suspend fun getProfile(): UserProfile =
        client.get("$baseUrl/user/profile").body()
}