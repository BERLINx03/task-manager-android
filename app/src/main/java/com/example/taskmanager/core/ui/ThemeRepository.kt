package com.example.taskmanager.core.ui

import kotlinx.coroutines.flow.Flow

/**
 * @author Abdallah Elsokkary
 */
interface ThemeRepository {
    fun isDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(isDark: Boolean)
}