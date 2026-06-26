package com.example.secureauth

import android.app.Application
import com.example.secureauth.di.myModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level

class SecureAuthApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@SecureAuthApplication)
            modules(myModules)
        }
    }
}