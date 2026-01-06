package com.jeruk.alp_frontend.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 1. Setup DataStore (Otomatis membuat file user_prefs di HP)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_ID = stringPreferencesKey("user_id") // Tambahan kalau butuh ID
    }

    // 2. Baca Data (Mengalir terus datanya / Realtime)
    val userToken: Flow<String?> = context.dataStore.data.map { it[USER_TOKEN] }
    val userEmail: Flow<String?> = context.dataStore.data.map { it[USER_EMAIL] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE] }

    // 3. Simpan Data (Panggil saat Login Sukses)
    suspend fun saveUser(token: String, email: String, role: String, id: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN] = token
            preferences[USER_EMAIL] = email
            preferences[USER_ROLE] = role
            preferences[USER_ID] = id
        }
    }

    // 4. Hapus Data (Panggil saat Logout)
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}