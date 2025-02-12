package com.example.taskmanager.auth.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * @author Abdallah Elsokkary
 */

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenDataStore(private val context: Context){
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val AUTH_STATE = booleanPreferencesKey("auth_state")
    }
    val token: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[TOKEN_KEY] }

    val authState: Flow<Boolean?> = context.dataStore.data
        .map { preferences -> preferences[AUTH_STATE] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun saveAuthState(state: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTH_STATE] = state
        }
    }


    // Clear token on logout
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}