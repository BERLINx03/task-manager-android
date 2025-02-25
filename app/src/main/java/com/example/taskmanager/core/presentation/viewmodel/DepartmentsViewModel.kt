package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.data.remote.dto.DepartmentRequestDto
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.PaginatedData
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.DepartmentIntents
import com.example.taskmanager.core.presentation.state.DepartmentsState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class DepartmentsViewModel @Inject constructor(
    private val userInfoDataStore: UserInfoDataStore,
    userRoleDataStore: TokenDataStore,
    private val adminRepository: AdminRepository,
    private val sharedRepository: SharedRepository
) : ViewModel() {

    private val _departmentsState = MutableStateFlow(DepartmentsState())
    val departmentsState = _departmentsState.asStateFlow()

    val userRole = userRoleDataStore.userRole
    private val pageSize = 10

    private val _addedSuccessfully = MutableSharedFlow<Unit>()
    val addedSuccessfully = _addedSuccessfully.asSharedFlow()

    init {
        loadDepartments(false)
        viewModelScope.launch {
            userInfoDataStore.userInfoFlow.collectLatest { user ->
                _departmentsState.update {
                    it.copy(
                        user = user
                    )
                }
            }
        }
    }

    fun onIntent(intent: DepartmentIntents) {
        when (intent) {
            is DepartmentIntents.OnTitleChanged -> {
                updateTitle(intent.title)
            }

            is DepartmentIntents.AddDepartment -> {
                addDepartment(intent.title)
            }

            is DepartmentIntents.OnSearchQueryChange -> {
                updateSearchQuery(intent.query)
            }

            is DepartmentIntents.LoadDepartments -> loadDepartments(forceFetchFromRemote = intent.forceFetchFromRemote)
            DepartmentIntents.LoadNextPage -> loadNextPage()
            DepartmentIntents.LoadPreviousPage -> loadPreviousPage()
            DepartmentIntents.Refresh -> {
                viewModelScope.launch {
                    _departmentsState.update { it.copy(isRefreshing = true) }
                    try {
                        loadDepartments(forceFetchFromRemote = true)
                        delay(300)
                    } finally {
                        _departmentsState.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is DepartmentIntents.SetSortOption -> setSortOption(intent.sortOption)
        }
    }


    private fun setSortOption(sortOption: String) {
        if (_departmentsState.value.sortOption == sortOption) return

        _departmentsState.update {
            it.copy(sortOption = sortOption)
        }
        loadDepartments(forceFetchFromRemote = false)
    }

    private fun updateTitle(title: String) {
        _departmentsState.update { it.copy(title = title) }
    }

    private fun updateSearchQuery(query: String) {
        if (_departmentsState.value.isLoading) return
        _departmentsState.update { state ->
            state.copy(
                searchQuery = query.takeIf { query ->
                    query.isNotBlank()
                })
        }
        loadDepartments(forceFetchFromRemote = false)
    }

    private fun loadDepartments(forceFetchFromRemote: Boolean) {
        if (_departmentsState.value.isLoading) return

        _departmentsState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val currentState = _departmentsState.value
            try {
                getDepartmentsOnIO(
                    1,
                    pageSize,
                    currentState.searchQuery,
                    _departmentsState.value.sortOption,
                    forceFetchFromRemote,
                    forceFetchFromRemote
                )
                    .collectLatest { resource ->
                        when (resource) {
                            is Resource.Error -> {
                                _departmentsState.update {
                                    it.copy(
                                        errorMessage = resource.message,
                                        isLoading = false
                                    )
                                }
                                Timber.e("Error loading departments: ${resource.message}")
                            }

                            is Resource.Loading -> {
                                _departmentsState.update { current ->
                                    current.copy(
                                        isLoading = resource.isLoading
                                    )
                                }
                            }

                            is Resource.Success -> {
                                val departments = resource.data
                                _departmentsState.update {
                                    it.copy(
                                        departments = departments.items,
                                        currentPage = departments.page,
                                        totalPages = calculateTotalPages(
                                            departments.totalCount,
                                            departments.pageSize
                                        ),
                                        hasNextPage = departments.hasNextPage,
                                        hasPreviousPage = departments.hasPreviousPage,
                                        errorMessage = null,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                _departmentsState.update {
                    it.copy(
                        errorMessage = "Failed to load departments: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Unexpected error in loadDepartments")
            }
        }
    }

    private fun loadNextPage() {
        if (!_departmentsState.value.hasNextPage || _departmentsState.value.isLoading) return

        _departmentsState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                getDepartmentsOnIO(
                    _departmentsState.value.currentPage + 1,
                    pageSize,
                    _departmentsState.value.searchQuery,
                    _departmentsState.value.sortOption,
                    false,
                    false
                ).collectLatest { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            _departmentsState.update {
                                it.copy(
                                    errorMessage = resource.message,
                                    isLoading = false
                                )
                            }
                            Timber.e("Error loading next page of departments: ${resource.message}")
                        }

                        is Resource.Loading -> {
                            _departmentsState.update {
                                it.copy(
                                    isLoading = resource.isLoading
                                )
                            }
                        }

                        is Resource.Success -> {
                            val departments = resource.data
                            _departmentsState.update {
                                it.copy(
                                    departments = departments.items,
                                    currentPage = departments.page,
                                    hasNextPage = departments.hasNextPage,
                                    hasPreviousPage = departments.hasPreviousPage,
                                    errorMessage = null,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _departmentsState.update {
                    it.copy(
                        errorMessage = "Failed to load next page: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Unexpected error in loadNextPage")
            }
        }

    }

    private fun loadPreviousPage() {

        if (!_departmentsState.value.hasPreviousPage || _departmentsState.value.isLoading) return

        _departmentsState.update { it.copy(isLoading = true) }


        val previousPage = _departmentsState.value.currentPage - 1
        if (previousPage < 1) return

        viewModelScope.launch {
            try {
                getDepartmentsOnIO(
                    previousPage,
                    pageSize,
                    _departmentsState.value.searchQuery,
                    _departmentsState.value.sortOption,
                    false,
                    false
                )
                    .collectLatest { resource ->
                        when (resource) {
                            is Resource.Error -> {
                                _departmentsState.update {
                                    it.copy(
                                        errorMessage = resource.message,
                                        isLoading = false
                                    )
                                }
                                Timber.e("Error loading previous page of departments: ${resource.message}")
                            }

                            is Resource.Loading -> {
                                _departmentsState.update { current ->
                                    current.copy(
                                        isLoading = resource.isLoading
                                    )
                                }
                            }

                            is Resource.Success -> {
                                val departments = resource.data
                                _departmentsState.update {
                                    it.copy(
                                        departments = departments.items,
                                        currentPage = departments.page,
                                        hasNextPage = departments.hasNextPage,
                                        hasPreviousPage = departments.hasPreviousPage,
                                        errorMessage = null,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                _departmentsState.update {
                    it.copy(
                        errorMessage = "Failed to load previous page: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Unexpected error in loadPreviousPage")
            }
        }

    }
    // Done 100%
    fun addDepartment(title: String) {
        viewModelScope.launch {
            when (val result = addDepartmentOnIO(title)) {
                is Resource.Error -> {
                    _departmentsState.update {
                        it.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }

                is Resource.Loading -> {
                    _departmentsState.update { current ->
                        current.copy(
                            isLoading = result.isLoading
                        )
                    }
                }

                is Resource.Success -> {
                    _departmentsState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    _addedSuccessfully.emit(Unit)
                }
            }
        }
    }


    // helpers to prevent memory lacking
    private suspend fun getDepartmentsOnIO(page: Int, limit: Int, search: String?, sort: String?, forceFetchFromRemote: Boolean, isRefreshing: Boolean): Flow<Resource<PaginatedData<Department>>> {
        return withContext(Dispatchers.IO) {
            adminRepository.getDepartments(
                page,
                limit,
                search,
                sort,
                forceFetchFromRemote,
                isRefreshing
            )
        }
    }
    private suspend fun getDepartmentOnIO(departmentId: UUID): Resource<Department> {
        return withContext(Dispatchers.IO) {
            sharedRepository.getDepartmentById(departmentId)
        }
    }
    private suspend fun addDepartmentOnIO(title: String): Resource<Department> =
        withContext(Dispatchers.IO) {
            adminRepository.addDepartment(DepartmentRequestDto(title))
        }

    private fun calculateTotalPages(totalCount: Int, pageSize: Int): Int {
        return if (totalCount == 0 || pageSize == 0) {
            1
        } else {
            (totalCount + pageSize - 1) / pageSize
        }
    }
}
