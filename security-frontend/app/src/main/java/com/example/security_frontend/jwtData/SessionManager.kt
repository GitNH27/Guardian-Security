package com.example.security_frontend.jwtData

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("auth_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
    }

    // Save token
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    // Get token as Flow
    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    // Clear token
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
