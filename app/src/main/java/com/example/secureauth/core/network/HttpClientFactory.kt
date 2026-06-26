package com.example.secureauth.core.network

import com.example.secureauth.BuildConfig
import com.example.secureauth.core.data.local.TokenDataStore
import com.example.secureauth.feature.auth.data.remote.dto.RefreshRequest
import com.example.secureauth.feature.auth.data.remote.dto.RefreshResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

object HttpClientFactory {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun createPlainClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(Logging) {
            logger = Logger.DEFAULT
            level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
        }
    }

    fun createAuthenticatedClient(
        tokenDataStore: TokenDataStore,
        plainClient: HttpClient,
        refreshUrl: String
    ): HttpClient = HttpClient(Android) {

        install(ContentNegotiation) { json(json) }

        install(Logging) {
            logger = Logger.DEFAULT
            level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
        }

        install(Auth) {
            bearer {
                loadTokens {
                    val accessToken  = tokenDataStore.accessToken.first()
                    val refreshToken = tokenDataStore.refreshToken.first()
                    if (accessToken != null && refreshToken != null) {
                        BearerTokens(
                            accessToken  = accessToken,
                            refreshToken = refreshToken
                        )
                    } else {
                        null
                    }
                }
                refreshTokens {
                    val storedRefreshToken = tokenDataStore.refreshToken.first()

                    if (storedRefreshToken == null) {
                        tokenDataStore.clearSession()
                        return@refreshTokens null
                    }

                    try {
                        val response = plainClient.post(refreshUrl) {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(refreshToken = storedRefreshToken))
                            markAsRefreshTokenRequest()
                        }

                        when (response.status) {
                            HttpStatusCode.OK -> {
                                val body = response.body<RefreshResponse>()
                                tokenDataStore.saveTokens(
                                    accessToken  = body.accessToken,
                                    refreshToken = body.refreshToken
                                )
                                BearerTokens(
                                    accessToken  = body.accessToken,
                                    refreshToken = body.refreshToken
                                )
                            }
                            HttpStatusCode.Unauthorized,
                            HttpStatusCode.Forbidden -> {
                                tokenDataStore.clearSession()
                                null
                            }
                            else -> {
                                null
                            }
                        }
                    } catch (e: java.io.IOException) {
                        tokenDataStore.clearSession()
                        null
                    } catch (e: Exception) {
                        null
                    }
                }
                sendWithoutRequest { request ->
                    request.url.host == "api.yourbackend.com"   /*TODO*/
                }
            }
        }
    }
}