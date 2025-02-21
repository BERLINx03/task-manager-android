package com.example.taskmanager.core.domain.repository

import com.example.taskmanager.core.domain.model.Language
import kotlinx.coroutines.flow.Flow

/**
 * @author Abdallah Elsokkary
 */
interface LanguageRepository {
    suspend fun setLanguage(languageCode: String)
    fun getSelectedLanguage(): Flow<String>
    fun getAvailableLanguages(): List<Language>
}