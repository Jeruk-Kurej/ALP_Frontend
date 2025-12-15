package com.jeruk.alp_frontend.utils

import android.util.Log

/**
 * Debug utility to diagnose 403 errors
 */
object DebugHelper {
    private const val TAG = "DEBUG_403"

    fun log403Error(
        source: String,
        token: String?,
        endpoint: String,
        errorBody: String?
    ) {
        Log.e(TAG, "═══════════════════════════════════════════")
        Log.e(TAG, "🔥 403 FORBIDDEN ERROR DETECTED!")
        Log.e(TAG, "═══════════════════════════════════════════")
        Log.e(TAG, "Source: $source")
        Log.e(TAG, "Endpoint: $endpoint")
        Log.e(TAG, "─────────────────────────────────────────────")

        if (token != null && token.isNotEmpty()) {
            Log.e(TAG, "Token Status: PRESENT")
            Log.e(TAG, "Token Length: ${token.length}")
            Log.e(TAG, "Token Preview: ${token.take(30)}...")

            // Try to decode token
            try {
                val payload = TokenUtils.decodeJwt(token)
                if (payload != null) {
                    Log.e(TAG, "─────────────────────────────────────────────")
                    Log.e(TAG, "TOKEN DETAILS:")
                    Log.e(TAG, "  User ID: ${payload.userId}")
                    Log.e(TAG, "  Username: ${payload.username}")
                    Log.e(TAG, "  Role: ${payload.role}")
                    Log.e(TAG, "  Issued At: ${payload.iat}")
                    Log.e(TAG, "  Expires At: ${payload.exp}")

                    val isExpired = TokenUtils.isTokenExpired(token)
                    val isAdmin = TokenUtils.isAdmin(token)

                    Log.e(TAG, "  Is Expired: $isExpired ${if (isExpired) "❌" else "✅"}")
                    Log.e(TAG, "  Is Admin: $isAdmin ${if (isAdmin) "✅" else "❌"}")

                    if (isExpired) {
                        Log.e(TAG, "─────────────────────────────────────────────")
                        Log.e(TAG, "⚠️ TOKEN IS EXPIRED!")
                        Log.e(TAG, "SOLUTION: LOGOUT AND LOGIN AGAIN")
                    }

                    if (!isAdmin) {
                        Log.e(TAG, "─────────────────────────────────────────────")
                        Log.e(TAG, "⚠️ USER IS NOT ADMIN!")
                        Log.e(TAG, "SOLUTION: Login with admin account or grant admin role")
                    }
                } else {
                    Log.e(TAG, "⚠️ Could not decode token!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error decoding token: ${e.message}")
            }
        } else {
            Log.e(TAG, "Token Status: EMPTY or NULL ❌")
        }

        Log.e(TAG, "─────────────────────────────────────────────")
        if (errorBody != null) {
            Log.e(TAG, "Error Response Body:")
            Log.e(TAG, errorBody)
        } else {
            Log.e(TAG, "No error body received")
        }

        Log.e(TAG, "═══════════════════════════════════════════")
        Log.e(TAG, "POSSIBLE CAUSES:")
        Log.e(TAG, "1. Token expired - LOGOUT and LOGIN again")
        Log.e(TAG, "2. Not admin - Need admin role in backend")
        Log.e(TAG, "3. Resource ownership - Trying to access another user's data")
        Log.e(TAG, "4. Backend validation - Check backend logs")
        Log.e(TAG, "═══════════════════════════════════════════")
    }

    fun logTokenInfo(token: String?, source: String) {
        Log.d(TAG, "═══════════════════════════════════════════")
        Log.d(TAG, "🔍 TOKEN INFO from $source")
        Log.d(TAG, "═══════════════════════════════════════════")

        if (token.isNullOrEmpty()) {
            Log.e(TAG, "❌ TOKEN IS NULL OR EMPTY!")
            return
        }

        Log.d(TAG, "Token Length: ${token.length}")
        Log.d(TAG, "Token Preview: ${token.take(50)}...")

        try {
            val payload = TokenUtils.decodeJwt(token)
            if (payload != null) {
                Log.d(TAG, "User ID: ${payload.userId}")
                Log.d(TAG, "Username: ${payload.username}")
                Log.d(TAG, "Role: ${payload.role}")
                Log.d(TAG, "Is Expired: ${TokenUtils.isTokenExpired(token)}")
                Log.d(TAG, "Is Admin: ${TokenUtils.isAdmin(token)}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding: ${e.message}")
        }

        Log.d(TAG, "═══════════════════════════════════════════")
    }
}

