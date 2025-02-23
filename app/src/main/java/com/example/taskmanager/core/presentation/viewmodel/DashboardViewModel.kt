package com.example.taskmanager.core.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.presentation.intents.DashboardIntents
import com.example.taskmanager.core.presentation.state.DashboardState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val userInfoDataStore: UserInfoDataStore
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState = _dashboardState.asStateFlow()



    init {
        getCachedAdminsCount()
        getCachedDepartmentsCounts()
        getCachedTasksCounts()
        getCachedManagersCounts()
        getCachedEmployeesCounts()
        viewModelScope.launch {
            userInfoDataStore.userInfoFlow.collectLatest { user ->
                _dashboardState.update {
                    it.copy(
                        user = user
                    )
                }

                Timber.i("user is ${user.firstName} ${user.lastName}")
            }
        }
    }

    fun onIntent(intent: DashboardIntents) {
        when (intent) {
            DashboardIntents.Refresh -> {
                viewModelScope.launch {
                    _dashboardState.update { it.copy(isRefreshing = true) }
                    try {
                        val refreshJob = listOf(
                            async { getAdminsCountFromNetwork() },
                            async { getManagersCountFromNetwork() },
                            async { getEmployeesCountFromNetwork() },
                            async { getDepartmentsCountFromNetwork() },
                            async { getTasksCountFromNetwork() },
                        )
                        refreshJob.awaitAll()

                        delay(300) //to let the animation finish before setting the value to false
                    } finally {
                        _dashboardState.update { it.copy(isRefreshing = false) }
                    }
                }
            }
        }
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
                is Resource.Loading -> {
                    _dashboardState.update { it.copy(isLoading = result.isLoading) }
                }
                is Resource.Success -> {
                    _dashboardState.update { it.copy(adminsCount = result.data) }
                }
            }
        }
    }

    private fun getDepartmentsCountFromNetwork(){
        viewModelScope.launch {
            adminRepository.getDepartmentsCountFromNetwork().collectLatest { result->
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
                        _dashboardState.update { it.copy(departmentsCount = result.data, isLoading = false) }
                    }
                }
            }
        }
    }
    private fun getCachedDepartmentsCounts() {
        viewModelScope.launch {
            when (val result = adminRepository.getCachedDepartmentsCount()){
                is Resource.Error -> {
                    _dashboardState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {
                    _dashboardState.update { it.copy(isLoading = result.isLoading) }
                }
                is Resource.Success -> {
                    _dashboardState.update { it.copy(departmentsCount = result.data) }
                }
            }
        }
    }

    private fun getTasksCountFromNetwork(){
        viewModelScope.launch {
            adminRepository.getTasksCountFromNetwork().collectLatest { result->
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
                        _dashboardState.update { it.copy(tasksCount = result.data, isLoading = false) }
                    }
                }
            }
        }
    }
    private fun getCachedTasksCounts() {
        viewModelScope.launch {
            when (val result = adminRepository.getCachedTasksCount()){
                is Resource.Error -> {
                    _dashboardState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {
                    _dashboardState.update { it.copy(isLoading = result.isLoading) }
                }
                is Resource.Success -> {
                    _dashboardState.update { it.copy(tasksCount = result.data) }
                }
            }
        }
    }

    private fun getManagersCountFromNetwork(){
        viewModelScope.launch {
            adminRepository.getManagersCountFromNetwork().collectLatest { result->
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
                        _dashboardState.update { it.copy(managersCount = result.data, isLoading = false) }
                    }
                }
            }
        }
    }
    private fun getCachedManagersCounts() {
        viewModelScope.launch {
            when (val result = adminRepository.getCachedManagersCount()){
                is Resource.Error -> {
                    _dashboardState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {
                    _dashboardState.update { it.copy(isLoading = result.isLoading) }
                }
                is Resource.Success -> {
                    _dashboardState.update { it.copy(managersCount = result.data) }
                }
            }
        }
    }

    private fun getEmployeesCountFromNetwork(){
        viewModelScope.launch {
            adminRepository.getEmployeesCountFromNetwork().collectLatest { result->
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
                        _dashboardState.update { it.copy(employeesCount = result.data, isLoading = false) }
                    }
                }
            }
        }
    }
    private fun getCachedEmployeesCounts() {
        viewModelScope.launch {
            when (val result = adminRepository.getCachedEmployeesCount()){
                is Resource.Error -> {
                    _dashboardState.update { it.copy(error = result.message) }
                }
                is Resource.Loading -> {
                    _dashboardState.update { it.copy(isLoading = result.isLoading) }
                }
                is Resource.Success -> {
                    _dashboardState.update { it.copy(employeesCount = result.data) }
                }
            }
        }
    }
}