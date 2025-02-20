package com.example.taskmanager.admin.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.admin.presentation.intents.DashboardIntents
import com.example.taskmanager.admin.presentation.state.DashboardState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState = _dashboardState.asStateFlow()

    fun onIntent(intent: DashboardIntents) {
        when (intent) {
            is DashboardIntents.GetAdminsCount -> {
                getAdminsCountFromNetwork()
            }
        }
    }

    init {
        getCachedAdminsCount()
    }

    private fun getAdminsCountFromNetwork(){
        viewModelScope.launch {
            adminRepository.getAdminsCountFromNetwork().collectLatest { result->
                when (result){
                    is Resource.Error -> {
                        _dashboardState.update { it.copy(error = result.message, isLoading = false) }
                    }
                    is Resource.Loading -> {
                        _dashboardState.update {
                            it.copy(isLoading = result.isLoading)
                        }
                    }
                    is Resource.Success -> {
                        _dashboardState.update { it.copy(adminsCount = result.data, isLoading = false) }
                    }
                }
            }
        }
    }
    private fun getCachedAdminsCount() {
        viewModelScope.launch {
            when (val result = adminRepository.getCachedAdminsCount()){
                is Resource.Error -> {
                    _dashboardState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _dashboardState.update { it.copy(adminsCount = result.data) }
                }
            }
        }
    }
}