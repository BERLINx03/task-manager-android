package com.example.taskmanager.core.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskmanager.core.utils.LANGUAGE_SITTINGS
import com.example.taskmanager.core.utils.SELECTED_LANGUAGE_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class LanguageDataStore @Inject constructor(
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(LANGUAGE_SITTINGS)
    private val selectedLanguageKey = stringPreferencesKey(SELECTED_LANGUAGE_KEY)

    suspend fun saveLanguage(languageCode: String){
        context.dataStore.edit { preferences ->
            preferences[selectedLanguageKey] = languageCode
        }
    }

    fun getLanguage(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[selectedLanguageKey] ?: Locale.getDefault().language
        }
    }
}