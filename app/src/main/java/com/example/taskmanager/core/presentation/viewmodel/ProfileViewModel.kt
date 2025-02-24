package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRole: TokenDataStore,
    stateHandle: SavedStateHandle
) : ViewModel() {
    private val userId = stateHandle.get<String>("userId") ?: ""
    val userType = stateHandle.get<String>("role") ?: ""


    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()

    val role = userRole.userRole
    private val pageSize = 10

    init {
        Timber.d("User ID: $userId")
        Timber.d("User Role: $userType")
        Timber.d("the tasks should loud")
        loadManagerProfile()
        if (userType == "Manager" || userType == "Admin") {
            loadTasks(false)
            Timber.d("loadtasks() has finished execution")
        }
    }

    fun onIntent(intent: ProfileIntents){
        when (intent){
            is ProfileIntents.LoadProfile -> loadManagerProfile()
            is ProfileIntents.LoadTasks -> loadTasks(intent.forceFetchFromRemote)
            is ProfileIntents.DeleteUser -> TODO()
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
        }
    }

    private fun loadTasks(forceFetchFromRemote: Boolean) {

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
    private fun loadProfile(){
        if (userType == "Manager"){
            loadManagerProfile()
        } else {
            //TODO (loadEmployeeProfile())
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

    private fun calculateTotalPages(totalCount: Int, pageSize: Int): Int {
        return if (totalCount == 0 || pageSize == 0) {
            1
        } else {
            (totalCount + pageSize - 1) / pageSize
        }
    }
}