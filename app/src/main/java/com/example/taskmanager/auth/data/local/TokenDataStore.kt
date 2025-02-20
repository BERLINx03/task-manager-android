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
        private val USER_ROLE = stringPreferencesKey("user_role")
    }
    val token: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[TOKEN_KEY] }

    val authState: Flow<Boolean?> = context.dataStore.data
        .map { preferences -> preferences[AUTH_STATE] }

    val userRole: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_ROLE] }

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

    suspend fun saveUserRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_ROLE] = role
        }
    }


    // Clear token on logout
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
        }
    }
}