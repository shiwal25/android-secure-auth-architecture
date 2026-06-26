package com.example.secureauth.feature.auth.data.remote

import com.example.secureauth.feature.auth.data.remote.dto.LoginRequest
import com.example.secureauth.feature.auth.data.remote.dto.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApiService(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun login(request: LoginRequest): HttpResponse =
        client.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

    suspend fun register(request: RegisterRequest): HttpResponse =
        client.post("$baseUrl/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
}