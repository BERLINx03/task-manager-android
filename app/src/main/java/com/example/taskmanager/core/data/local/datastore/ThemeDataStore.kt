package com.example.taskmanager.core.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class ThemeDataStore @Inject constructor(
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(THEME_SETTINGS)
    private val isDarkModeKey = booleanPreferencesKey(DARK_MODE_KEY)

    suspend fun saveThemeMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isDarkModeKey] = isDarkMode
        }
    }

    fun getThemeMode(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[isDarkModeKey] ?: false
        }
    }

    companion object {
        private const val THEME_SETTINGS = "theme_settings"
        private const val DARK_MODE_KEY = "dark_mode"
    }
}