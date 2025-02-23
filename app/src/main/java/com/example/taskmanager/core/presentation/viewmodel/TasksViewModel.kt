package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.presentation.intents.TasksIntents
import com.example.taskmanager.core.presentation.state.TasksState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
class TasksViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val userRoleDataStore: TokenDataStore,
    private val userInfoDataStore: UserInfoDataStore
) : ViewModel() {
    private val _tasksState = MutableStateFlow(TasksState())
    val tasksState = _tasksState.asStateFlow()

    val userRole = userRoleDataStore.userRole
    private val pageSize = 10

    init {
        load(forceFetchFromRemote = false)
        viewModelScope.launch {
            userInfoDataStore.userInfoFlow.collectLatest { userRole ->
                _tasksState.update {
                    it.copy(user = userRole)
                }
            }
        }
    }

    fun onIntent(intent: TasksIntents) {
        when (intent) {
            is TasksIntents.AddTask -> TODO()
            is TasksIntents.DeleteTask -> TODO()
            TasksIntents.LoadNextPage -> loadNextPage()
            TasksIntents.LoadPreviousPage -> loadPreviousPage()
            is TasksIntents.LoadTasks -> load(intent.forceFetchFromRemote)
            is TasksIntents.Navigating -> TODO()
            is TasksIntents.OnSearchQueryChange -> updateSearchQuery(intent.query)
            TasksIntents.OnTitleChanged -> TODO()
            TasksIntents.Refresh -> {
                viewModelScope.launch {
                    _tasksState.update { it.copy(isRefreshing = true) }
                    try {
                        load(forceFetchFromRemote = true)
                        delay(300)
                    } finally {
                        _tasksState.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is TasksIntents.UpdateTask -> TODO()
            is TasksIntents.UpdateTaskDepartment -> TODO()
            is TasksIntents.UpdateTaskDescription -> TODO()
            is TasksIntents.UpdateTaskDueDate -> TODO()
            is TasksIntents.UpdateTaskEmployee -> TODO()
            is TasksIntents.UpdateTaskEndDate -> TODO()
            is TasksIntents.UpdateTaskManager -> TODO()
            is TasksIntents.UpdateTaskPriority -> TODO()
            is TasksIntents.UpdateTaskStartDate -> TODO()
            is TasksIntents.UpdateTaskStatus -> TODO()
        }
    }

    private fun updateSearchQuery(query: String) {
        if (_tasksState.value.isLoading) return
        _tasksState.update { state ->
            state.copy(
                searchQuery = query.takeIf { query ->
                    query.isNotBlank()
                })
        }
        load(forceFetchFromRemote = false)
    }

    private fun load(forceFetchFromRemote: Boolean) {
        if (_tasksState.value.isLoading) return
        _tasksState.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            adminRepository.getTasks(
                page = _tasksState.value.currentPage,
                limit = pageSize,
                search = _tasksState.value.searchQuery,
                sort = null,
                forceFetchFromRemote = forceFetchFromRemote,
                isRefreshing = forceFetchFromRemote
            ).collectLatest { paginatedTasks ->
                when (paginatedTasks) {
                    is Resource.Error -> {
                        _tasksState.update {
                            it.copy(errorMessage = paginatedTasks.message, isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _tasksState.update {
                            it.copy(isLoading = paginatedTasks.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        val tasks = paginatedTasks.data.data
                        if (tasks != null) {
                            _tasksState.update {
                                it.copy(
                                    tasks = tasks.items,
                                    currentPage = tasks.page,
                                    hasNextPage = tasks.hasNextPage,
                                    totalPages = calculateTotalPages(
                                        tasks.totalCount,
                                        tasks.pageSize
                                    ),
                                    hasPreviousPage = tasks.hasPreviousPage,
                                    errorMessage = null,
                                    isLoading = false,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadNextPage() {
        if (!_tasksState.value.hasNextPage || _tasksState.value.isLoading) return

        _tasksState.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            adminRepository.getTasks(
                page = _tasksState.value.currentPage + 1,
                limit = pageSize,
                search = _tasksState.value.searchQuery,
                sort = null,
                forceFetchFromRemote = false,
                isRefreshing = false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _tasksState.update {
                            it.copy(errorMessage = resource.message, isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _tasksState.update {
                            it.copy(isLoading = resource.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        val tasks = resource.data.data
                        if (tasks != null) {
                            _tasksState.update {
                                it.copy(
                                    tasks = tasks.items,
                                    currentPage = tasks.page,
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
        }
    }

    private fun loadPreviousPage() {
        if (!_tasksState.value.hasPreviousPage || _tasksState.value.isLoading) return
        _tasksState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            adminRepository.getTasks(
                page = _tasksState.value.currentPage - 1,
                limit = pageSize,
                search = _tasksState.value.searchQuery,
                sort = null,
                forceFetchFromRemote = false,
                isRefreshing = false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _tasksState.update {
                            it.copy(errorMessage = resource.message, isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _tasksState.update {
                            it.copy(isLoading = resource.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        val tasks = resource.data.data
                        if (tasks != null) {
                            _tasksState.update {
                                it.copy(
                                    tasks = tasks.items,
                                    currentPage = tasks.page,
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