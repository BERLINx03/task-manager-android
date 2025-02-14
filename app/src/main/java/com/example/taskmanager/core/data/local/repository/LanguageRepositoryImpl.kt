package com.example.taskmanager.core.data.local.repository

import android.content.Context
import com.example.taskmanager.core.data.local.LanguageDataStore
import com.example.taskmanager.core.domain.domain.Language
import com.example.taskmanager.core.domain.repository.LanguageRepository
import com.example.taskmanager.core.utils.ARABIC_CODE
import com.example.taskmanager.core.utils.ENGLISH_CODE
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class LanguageRepositoryImpl @Inject constructor(
    private val languageDataStore: LanguageDataStore,
    private val context: Context
) : LanguageRepository {
    override suspend fun setLanguage(languageCode: String) {
        languageDataStore.saveLanguage(languageCode)
        updateConfiguration(languageCode)
    }

    override fun getSelectedLanguage(): Flow<String> {
        return languageDataStore.getLanguage()
    }

    override fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language(ENGLISH_CODE, "English"),
            Language(ARABIC_CODE, "Arabic")
        )
    }

    private fun updateConfiguration(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration

        config.setLocale(locale)
        val newContext = context.createConfigurationContext(config)
        context.resources.updateConfiguration(
            config,
            newContext.resources.displayMetrics
        )
    }
}