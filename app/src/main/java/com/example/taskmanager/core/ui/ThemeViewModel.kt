package com.example.taskmanager.core.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
): ViewModel() {

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.isDarkMode().collect { darkMode ->
                _isDarkMode.value = darkMode
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            themeRepository.setDarkMode(!_isDarkMode.value)
        }
    }
}