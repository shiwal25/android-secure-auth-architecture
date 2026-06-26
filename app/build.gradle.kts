plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.secureauth"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.secureauth"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //koin
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation(platform("io.insert-koin:koin-bom:4.2.1"))
    implementation("io.insert-koin:koin-android")
    implementation("io.insert-koin:koin-androidx-compose")
    implementation("io.insert-koin:koin-androidx-compose-navigation")

    //Jetpack Navigation
    implementation("androidx.navigation3:navigation3-ui:1.1.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.10.0")

    //LifeCyle and ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")

    //Ktor
    implementation(platform("io.ktor:ktor-bom:3.5.0"))
    implementation("io.ktor:ktor-client-android")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-client-auth")

    //Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    //DataStore
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.datastore:datastore:1.3.0-alpha09")
    implementation("androidx.datastore:datastore-tink:1.3.0-alpha07")
    implementation("com.google.crypto.tink:tink-android:1.21.0")

    //Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
}