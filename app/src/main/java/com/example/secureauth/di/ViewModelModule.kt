package com.example.secureauth.di

import com.example.secureauth.feature.auth.presentation.AuthViewModel
import com.example.secureauth.feature.home.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf( ::AuthViewModel )
    viewModelOf( ::HomeViewModel)

}