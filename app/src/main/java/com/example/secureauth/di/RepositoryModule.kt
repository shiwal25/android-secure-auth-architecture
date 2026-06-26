package com.example.secureauth.di

import com.example.secureauth.feature.auth.domain.repository.AuthRepository
import com.example.secureauth.feature.auth.domain.repository.AuthRepositoryImpl
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val BASE_URL = "https://api.yourbackend.com"  /*TODO*/

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            apiService     = get(),
            tokenDataStore = get(),
            plainClient    = get(named("plain")),
            baseUrl        = BASE_URL
        )
    }
}