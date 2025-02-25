package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.ProfileIntents
import com.example.taskmanager.core.presentation.state.ProfileState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sharedRepository: SharedRepository,
    private val adminRepository: AdminRepository,
    private val userRole: TokenDataStore,
    stateHandle: SavedStateHandle
) : ViewModel() {
    val userId = stateHandle.get<String>("userId") ?: ""
    val userType = stateHandle.get<String>("role") ?: ""


    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    val role = userRole.userRole
    private val pageSize = 10

    init {
        loadProfile()
        loadTasks(false)
    }

    fun onIntent(intent: ProfileIntents) {
        when (intent) {
            is ProfileIntents.Refresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    try {
                        loadProfile()
                        loadTasks(true)
                        delay(500)
                    } finally {
                        _state.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is ProfileIntents.DeleteManager -> deleteManager(intent.managerId)
            is ProfileIntents.DeleteEmployee -> deleteEmployee(intent.employeeId)
        }
    }

    private fun loadManagerTasks(forceFetchFromRemote: Boolean){

        Timber.d("loadTasks() has started execution")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                sharedRepository.getTasksManagedByManager(
                    managerId = UUID.fromString(userId),
                    page = 1,
                    limit = pageSize,
                    search = null,
                    sort = null,
                    forceFetchFromRemote = forceFetchFromRemote,
                ).collectLatest { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    errorMessage = resource.message,
                                    isLoading = false
                                )
                            }
                            Timber.e("Error loading manager tasks: ${resource.message}")
                        }

                        is Resource.Loading -> {
                            _state.update { current ->
                                current.copy(
                                    isLoading = resource.isLoading
                                )
                            }
                        }

                        is Resource.Success -> {
                            Timber.d("Manager tasks loaded successfully")
                            val tasks = resource.data.data
                            if (tasks != null) {
                                _state.update {
                                    it.copy(
                                        tasks = tasks.items,
                                        currentPage = tasks.page,
                                        totalPages = calculateTotalPages(
                                            tasks.totalCount,
                                            tasks.pageSize
                                        ),
                                        hasNextPage = tasks.hasNextPage,
                                        hasPreviousPage = tasks.hasPreviousPage,
                                        errorMessage = null,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = "Failed to load tasks: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Unexpected error in load tasks: ${e.message}")
            }
        }
    }
    private fun loadEmployeeTasks(forceFetchFromRemote: Boolean){

        Timber.d("loadTasks() has started execution")
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                sharedRepository.getTasksAssignedToEmployee(
                    employeeId = UUID.fromString(userId),
                    page = 1,
                    limit = pageSize,
                    search = null,
                    sort = null,
                    forceFetchFromRemote = forceFetchFromRemote,
                ).collectLatest { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            _state.update {
                                it.copy(
                                    errorMessage = resource.message,
                                    isLoading = false
                                )
                            }
                            Timber.e("Error loading employee tasks: ${resource.message}")
                        }

                        is Resource.Loading -> {
                            _state.update { current ->
                                current.copy(
                                    isLoading = resource.isLoading
                                )
                            }
                        }

                        is Resource.Success -> {
                            Timber.d("employee tasks loaded successfully")
                            val tasks = resource.data.data
                            if (tasks != null) {
                                _state.update {
                                    it.copy(
                                        tasks = tasks.items,
                                        currentPage = tasks.page,
                                        totalPages = calculateTotalPages(
                                            tasks.totalCount,
                                            tasks.pageSize
                                        ),
                                        hasNextPage = tasks.hasNextPage,
                                        hasPreviousPage = tasks.hasPreviousPage,
                                        errorMessage = null,
                                        isLoading = false
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = "Failed to load tasks: ${e.message}",
                        isLoading = false
                    )
                }
                Timber.e(e, "Unexpected error in load tasks: ${e.message}")
            }
        }
    }
    private fun loadTasks(forceFetchFromRemote: Boolean) {
        when (userType) {
            "Manager" -> loadManagerTasks(forceFetchFromRemote)
            "Employee" -> loadEmployeeTasks(forceFetchFromRemote)
        }
    }

    private fun loadProfile() {
        when (userType) {
            "Manager" -> loadManagerProfile()
            "Employee" -> loadEmployeeProfile()
        }
    }

    private fun loadManagerProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = sharedRepository.getManagerById(UUID.fromString(userId))) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update {
                        it.copy(isLoading = result.isLoading)
                    }
                }

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            firstName = result.data.firstName,
                            lastName = result.data.lastName,
                            phoneNumber = result.data.phoneNumber,
                            gender = result.data.gender,
                            birthDate = result.data.birthDate,
                        )
                    }
                }
            }
        }
    }

    private fun loadEmployeeProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = sharedRepository.getEmployeeById(UUID.fromString(userId))) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update {
                        it.copy(isLoading = result.isLoading)
                    }
                }

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            firstName = result.data.firstName,
                            lastName = result.data.lastName,
                            phoneNumber = result.data.phoneNumber,
                            gender = result.data.gender,
                            birthDate = result.data.birthDate,
                        )
                    }
                }
            }
        }
    }

    //Admin functionalities
    private fun deleteManager(managerId: UUID) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = adminRepository.deleteManager(managerId)) {
                is Resource.Error -> {
                    _state.update { state ->
                        state.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update { state ->
                        state.copy(
                            isLoading = result.isLoading
                        )
                    }
                }

                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            deletedSuccessfully = true,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
    private fun deleteEmployee(employeeId: UUID) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = adminRepository.deleteEmployee(employeeId)) {
                is Resource.Error -> {
                    _state.update { state ->
                        state.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }

                is Resource.Loading -> {
                    _state.update { state ->
                        state.copy(
                            isLoading = result.isLoading
                        )
                    }
                }

                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            deletedSuccessfully = true,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun calculateTotalPages(totalCount: Int, pageSize: Int): Int {
        return if (totalCount == 0 || pageSize == 0) {
            1
        } else {
            (totalCount + pageSize - 1) / pageSize
        }
    }
}