package com.jeruk.alp_frontend.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Membuat extension context untuk DataStore
val Context.dataStore by preferencesDataStore(name = "admin_settings")

class AdminRepository(private val context: Context) {

    companion object {
        val ADMIN_PIN_KEY = stringPreferencesKey("admin_pin")
    }

    // Ambil PIN (Flow biar reaktif)
    val adminPinFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[ADMIN_PIN_KEY] // Return null jika belum diset
        }

    // Simpan PIN Baru
    suspend fun savePin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[ADMIN_PIN_KEY] = pin
        }
    }
}