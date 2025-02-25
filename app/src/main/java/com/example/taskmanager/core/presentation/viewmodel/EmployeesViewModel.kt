package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.EmployeesIntents
import com.example.taskmanager.core.presentation.state.EmployeesState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class EmployeesViewModel @Inject constructor(
    private val sharedRepository: SharedRepository,
    private val adminRepository: AdminRepository,
    private val userInfoDataStore: UserInfoDataStore,
    userRoleDataStore: TokenDataStore,
) : ViewModel() {

    private val _employeesState = MutableStateFlow(EmployeesState())
    val employeesState = _employeesState.asStateFlow()

    val userRole = userRoleDataStore.userRole

    private val pageSize = 10

    init {
        loadEmployees(forceFetchFromRemote = false)
        viewModelScope.launch {
            userInfoDataStore.userInfoFlow.collect { user ->
                _employeesState.update {
                    it.copy(user = user)
                }
            }
        }
    }

    fun onIntent(intent: EmployeesIntents) {
        when (intent) {
            is EmployeesIntents.Load -> loadEmployees(intent.forceFetchFromRemote)
            is EmployeesIntents.LoadNextPage -> loadNextPage()
            is EmployeesIntents.LoadPreviousPage -> loadPreviousPage()
            is EmployeesIntents.OnSearchQueryChange -> search(query = intent.query)
            is EmployeesIntents.Refresh -> {
                _employeesState.update { it.copy(isRefreshing = true) }
                viewModelScope.launch {
                    try {
                        loadEmployees(forceFetchFromRemote = true)
                        delay(300)
                    } finally {
                        _employeesState.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is EmployeesIntents.SetSortOption -> setSortOption(intent.sortOption)
        }
    }
    private fun setSortOption(sortOption: String) {
        if (_employeesState.value.sortOption == sortOption) return

        _employeesState.update {
            it.copy(sortOption = sortOption)
        }
        loadEmployees(forceFetchFromRemote = false)
    }

    private fun loadEmployees(forceFetchFromRemote: Boolean) {
        if (_employeesState.value.isLoading) return

        _employeesState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            sharedRepository.getPagedEmployees(
                page = 1,
                limit = pageSize,
                search = _employeesState.value.searchQuery,
                sort = _employeesState.value.sortOption,
                forceFetchFromRemote = forceFetchFromRemote,
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _employeesState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                        Timber.e("Error loading employees: ${resource.message}")
                    }

                    is Resource.Loading -> {
                        _employeesState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val employees = resource.data.data
                        if (employees != null) {
                            _employeesState.update {
                                it.copy(
                                    employees = employees.items,
                                    currentPage = employees.page,
                                    totalPages = calculateTotalPages(
                                        employees.totalCount,
                                        employees.pageSize
                                    ),
                                    hasNextPage = employees.hasNextPage,
                                    hasPreviousPage = employees.hasPreviousPage,
                                    errorMessage = null,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadNextPage() {
        if (!_employeesState.value.hasNextPage || _employeesState.value.isLoading) return

        _employeesState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            sharedRepository.getPagedEmployees(
                page = _employeesState.value.currentPage + 1,
                limit = pageSize,
                search = _employeesState.value.searchQuery,
                sort = _employeesState.value.sortOption,
                forceFetchFromRemote = false,
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _employeesState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _employeesState.update { current ->
                            current.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val employees = resource.data.data
                        if (employees != null) {
                            _employeesState.update {
                                it.copy(
                                    employees = employees.items,
                                    currentPage = employees.page,
                                    hasNextPage = employees.hasNextPage,
                                    hasPreviousPage = employees.hasPreviousPage,
                                    errorMessage = null,
                                )
                            }
                        } else {
                            _employeesState.update {
                                it.copy(
                                    errorMessage = "No employees found",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadPreviousPage() {
        if (!_employeesState.value.hasPreviousPage || _employeesState.value.isLoading) return

        _employeesState.update { it.copy(isLoading = true) }

        val previousPage = _employeesState.value.currentPage - 1
        if (previousPage < 1) return

        viewModelScope.launch {
            sharedRepository.getPagedEmployees(
                page = previousPage,
                limit = pageSize,
                search = _employeesState.value.searchQuery,
                sort = _employeesState.value.sortOption,
                forceFetchFromRemote = false,
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _employeesState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _employeesState.update { current ->
                            current.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val employees = resource.data.data
                        if (employees != null) {
                            _employeesState.update {
                                it.copy(
                                    employees = employees.items,
                                    currentPage = employees.page,
                                    hasNextPage = employees.hasNextPage,
                                    hasPreviousPage = employees.hasPreviousPage,
                                    errorMessage = null,
                                    isLoading = false
                                )
                            }
                        } else {
                            _employeesState.update {
                                it.copy(
                                    errorMessage = "No employees found",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun search(query: String) {
        if (_employeesState.value.isLoading) return
        _employeesState.update { state ->
            state.copy(searchQuery = query.takeIf { it.isNotBlank() })
        }
        loadEmployees(forceFetchFromRemote = true)
    }

    private fun calculateTotalPages(totalCount: Int, pageSize: Int): Int {
        return if (totalCount == 0 || pageSize == 0) {
            1
        } else {
            (totalCount + pageSize - 1) / pageSize
        }
    }
}