package com.example.taskmanager.core.data.local.repository

import android.content.Context
import com.example.taskmanager.core.data.local.datastore.LanguageDataStore
import com.example.taskmanager.core.domain.model.Language
import com.example.taskmanager.core.domain.repository.LanguageRepository
import com.example.taskmanager.core.utils.DataStoreConstants
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
            Language(DataStoreConstants.ENGLISH_CODE, "English"),
            Language(DataStoreConstants.ARABIC_CODE, "Arabic")
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