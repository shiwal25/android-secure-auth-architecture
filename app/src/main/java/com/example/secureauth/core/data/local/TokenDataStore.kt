package com.example.secureauth.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.tink.AeadSerializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplate
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

class TokenDataStore(private val context: Context) {

    companion object {
        private const val DATASTORE_FILE_NAME = "auth_prefs.json"
        private const val KEYSET_NAME = "auth_prefs_keyset"
        private const val KEYSET_PREFS_NAME = "auth_prefs_keyset_prefs"
        private const val MASTER_KEY_URI = "android-keystore://auth_master_key"
    }

    private val dataStore: DataStore<AuthPreferences> by lazy {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, KEYSET_PREFS_NAME)
            .withKeyTemplate(
                KeyTemplate.createFrom(PredefinedAeadParameters.AES256_GCM)
            )
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle

        val aead: Aead = keysetHandle.getPrimitive(
            com.google.crypto.tink.RegistryConfiguration.get(),
            Aead::class.java
        )

        val encryptedSerializer = AeadSerializer(
            aead = aead,
            wrappedSerializer = AuthPreferencesSerializer,
            associatedData = DATASTORE_FILE_NAME.encodeToByteArray()
        )

        DataStoreFactory.create(
            serializer = encryptedSerializer,
            produceFile = { File(context.filesDir, "datastore/$DATASTORE_FILE_NAME") }
        )
    }

    val accessToken: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(AuthPreferences()) else throw e }
        .map { prefs -> prefs.accessToken.ifEmpty { null } }

    val refreshToken: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(AuthPreferences()) else throw e }
        .map { prefs -> prefs.refreshToken.ifEmpty { null } }

    val userName: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(AuthPreferences()) else throw e }
        .map { prefs -> prefs.userName.ifEmpty { null } }

    val userId: Flow<String?> = dataStore.data
        .catch { e -> if (e is IOException) emit(AuthPreferences()) else throw e }
        .map { prefs -> prefs.userId.ifEmpty { null } }

    suspend fun saveSession(
        accessToken: String,
        refreshToken: String,
        userName: String,
        userId: String
    ) {
        dataStore.updateData { current ->
            current.copy(
                accessToken = accessToken,
                refreshToken = refreshToken,
                userName = userName,
                userId = userId
            )
        }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        dataStore.updateData { current ->
            current.copy(
                accessToken = accessToken,
                refreshToken = refreshToken
            )
        }
    }

    suspend fun clearSession() {
        dataStore.updateData { AuthPreferences() }
    }
}