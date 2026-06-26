package com.example.secureauth.di

import com.example.secureauth.core.data.local.TokenDataStore
import com.example.secureauth.core.network.HttpClientFactory
import com.example.secureauth.feature.auth.data.remote.AuthApiService
import com.example.secureauth.feature.home.data.remote.HomeApiService
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val BASE_URL    = "https://api.yourbackend.com"  /*TODO*/
private const val REFRESH_URL = "$BASE_URL/auth/refresh"

val networkModule = module {
    single { TokenDataStore(get()) }

    single(named("plain")) {
        HttpClientFactory.createPlainClient()
    }

    single(named("auth")) {
        HttpClientFactory.createAuthenticatedClient(
            tokenDataStore = get(),
            plainClient    = get(named("plain")),
            refreshUrl     = REFRESH_URL
        )
    }

    single {
        AuthApiService(
            client = get(named("plain")),
            baseUrl = BASE_URL
        )
    }

    single {
        HomeApiService(
            client = get(named("auth")),
            baseUrl = BASE_URL
        )
    }
}