package com.jeruk.alp_frontend.utils

import android.util.Base64
import android.util.Log
import org.json.JSONObject

/**
 * Utility to decode and inspect JWT tokens
 */
object TokenUtils {

    private const val TAG = "TokenUtils"

    /**
     * Decode JWT token to extract user info and role
     * JWT format: header.payload.signature
     */
    fun decodeJwt(token: String): JwtPayload? {
        try {
            // Split token into parts
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e(TAG, "Invalid JWT format: expected 3 parts, got ${parts.size}")
                return null
            }

            // Decode the payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)

            Log.d(TAG, "Decoded JWT payload: $decodedString")

            // Parse JSON
            val json = JSONObject(decodedString)

            return JwtPayload(
                userId = json.optInt("userId", -1),
                username = json.optString("username", ""),
                role = json.optString("role", ""),
                exp = json.optLong("exp", 0L),
                iat = json.optLong("iat", 0L)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding JWT: ${e.message}", e)
            return null
        }
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        val payload = decodeJwt(token) ?: return true
        val currentTime = System.currentTimeMillis() / 1000 // Convert to seconds
        return payload.exp > 0 && currentTime > payload.exp
    }

    /**
     * Check if user has admin role
     */
    fun isAdmin(token: String): Boolean {
        val payload = decodeJwt(token) ?: return false
        return payload.role.equals("admin", ignoreCase = true)
    }
}

/**
 * Data class representing JWT payload
 */
data class JwtPayload(
    val userId: Int,
    val username: String,
    val role: String,
    val exp: Long,  // Expiration timestamp (seconds since epoch)
    val iat: Long   // Issued at timestamp (seconds since epoch)
)

