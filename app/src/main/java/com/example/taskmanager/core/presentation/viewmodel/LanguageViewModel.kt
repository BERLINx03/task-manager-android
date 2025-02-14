package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.domain.repository.LanguageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageRepository: LanguageRepository
): ViewModel() {

    private val _currentLanguage = MutableStateFlow<String>("")
    val currentLanguage= _currentLanguage.asStateFlow()

    val availableLanguages = languageRepository.getAvailableLanguages()

    init {
        viewModelScope.launch {
            languageRepository.getSelectedLanguage().collect { language ->
                _currentLanguage.value = language
            }
        }
    }

    fun changeLanguage(languageCode: String){
        viewModelScope.launch {
            languageRepository.setLanguage(languageCode)
        }
    }
}