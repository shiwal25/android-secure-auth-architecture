package com.example.secureauth.core.data.local

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object AuthPreferencesSerializer : Serializer<AuthPreferences> {

    override val defaultValue: AuthPreferences = AuthPreferences()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun readFrom(input: InputStream): AuthPreferences {
        return try {
            json.decodeFromString(
                deserializer = AuthPreferences.serializer(),
                string       = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: AuthPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                json.encodeToString(
                    serializer = AuthPreferences.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}