package com.example.secureauth.core.util

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    private const val EXPIRY_BUFFER_SECONDS = 30L

    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size < 3) return true

            val payloadJson = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING),
                Charsets.UTF_8
            )

            val exp = JSONObject(payloadJson).getLong("exp")
            val nowSeconds = System.currentTimeMillis() / 1000L

            (nowSeconds + EXPIRY_BUFFER_SECONDS) >= exp

        } catch (e: Exception) {
            true
        }
    }
}