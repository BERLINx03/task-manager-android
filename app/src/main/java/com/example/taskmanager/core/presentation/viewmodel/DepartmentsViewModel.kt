package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.data.remote.SharedApiService
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.DepartmentIntents
import com.example.taskmanager.core.presentation.state.DepartmentsState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
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
            DepartmentIntents.OnTitleChanged ->{
                //TODO
            }
            is DepartmentIntents.AddDepartment -> {
                //TODO
            }
            is DepartmentIntents.OnSearchQueryChange -> {
                updateSearchQuery(intent.query)
            }
            is DepartmentIntents.Navigating -> {
                //TODO
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
        }
    }


    fun getDepartmentById(id: UUID) {
        _departmentsState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = sharedRepository.getDepartmentById(id)) {
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
                            selectedDepartment = result.data,
                            isLoading = false
                        )
                    }
                }
            }
        }
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
                adminRepository.getDepartments(
                    page = 1,
                    limit = pageSize,
                    search = currentState.searchQuery,
                    sort = null,
                    forceFetchFromRemote = forceFetchFromRemote,
                    isRefreshing = forceFetchFromRemote
                ).collectLatest { resource ->
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
                            val departments = resource.data.data
                            if (departments != null) {
                                _departmentsState.update {
                                    it.copy(
                                        departments = departments.items,
                                        currentPage = departments.page,
                                        totalPages = calculateTotalPages(departments.totalCount, departments.pageSize),
                                        hasNextPage = departments.hasNextPage,
                                        hasPreviousPage = departments.hasPreviousPage,
                                        errorMessage = null,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _departmentsState.update { it.copy(
                    errorMessage = "Failed to load departments: ${e.message}",
                    isLoading = false
                ) }
                Timber.e(e, "Unexpected error in loadDepartments")
            }
        }
    }

    fun loadNextPage() {
        if (!_departmentsState.value.hasNextPage || _departmentsState.value.isLoading) return

        _departmentsState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                adminRepository.getDepartments(
                    page = _departmentsState.value.currentPage + 1,
                    limit = pageSize,
                    search = _departmentsState.value.searchQuery,
                    sort = null,
                    forceFetchFromRemote = false,
                    isRefreshing = false
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
                            _departmentsState.update { current ->
                                current.copy(
                                    isLoading = resource.isLoading
                                )
                            }
                        }
                        is Resource.Success -> {
                            val departments = resource.data.data
                            if (departments != null) {
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
                            } else {
                                _departmentsState.update {
                                    it.copy(
                                        errorMessage = "No departments found",
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _departmentsState.update { it.copy(
                    errorMessage = "Failed to load next page: ${e.message}",
                    isLoading = false
                ) }
                Timber.e(e, "Unexpected error in loadNextPage")
            }
        }

    }

    fun loadPreviousPage() {

        if (!_departmentsState.value.hasPreviousPage || _departmentsState.value.isLoading) return

        _departmentsState.update { it.copy(isLoading = true) }


        val previousPage = _departmentsState.value.currentPage - 1
        if (previousPage < 1) return

        viewModelScope.launch {
            try {
                val response = adminRepository.getDepartments(
                    page = previousPage,
                    limit = pageSize,
                    search = _departmentsState.value.searchQuery,
                    sort = null,
                    forceFetchFromRemote = false,
                    isRefreshing = false
                ).collectLatest { resource ->
                    when (resource){
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
                            val departments = resource.data.data
                            if (departments != null) {
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
                }
            } catch (e: Exception) {
                _departmentsState.update { it.copy(
                    errorMessage = "Failed to load previous page: ${e.message}",
                    isLoading = false
                ) }
                Timber.e(e, "Unexpected error in loadPreviousPage")
            }
        }

    }

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
                }
            }
        }
    }

    private suspend fun addDepartmentOnIO(title: String): Resource<Department> =
        withContext(Dispatchers.IO){
            adminRepository.addDepartment(title)
        }

    private fun calculateTotalPages(totalCount: Int, pageSize: Int): Int {
        return if (totalCount == 0 || pageSize == 0) {
            1
        } else {
            (totalCount + pageSize - 1) / pageSize
        }
    }
}
