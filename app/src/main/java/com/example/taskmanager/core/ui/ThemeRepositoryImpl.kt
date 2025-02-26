package com.example.taskmanager.core.ui

import com.example.taskmanager.core.data.local.datastore.ThemeDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class ThemeRepositoryImpl @Inject constructor(
    private val themeDataStore: ThemeDataStore
) : ThemeRepository {
    override fun isDarkMode(): Flow<Boolean> {
        return themeDataStore.getThemeMode()
    }

    override suspend fun setDarkMode(isDark: Boolean) {
        themeDataStore.saveThemeMode(isDark)
    }
}