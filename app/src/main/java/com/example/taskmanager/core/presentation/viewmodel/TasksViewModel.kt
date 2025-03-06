package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.User
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.TasksIntents
import com.example.taskmanager.core.presentation.state.TasksState
import com.example.taskmanager.core.utils.Resource
import com.example.taskmanager.manager.domain.repository.ManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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
class TasksViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val sharedRepository: SharedRepository,
    private val managerRepository: ManagerRepository,
    userRoleDataStore: TokenDataStore,
    savedStateHandle: SavedStateHandle,
    private val userInfoDataStore: UserInfoDataStore
) : ViewModel() {
    private val managerId = savedStateHandle.get<String>("managerId") ?: ""

    private val _state = MutableStateFlow(TasksState())
    val tasksState = _state.asStateFlow()

    private val _addedSuccessfully = MutableSharedFlow<Unit>()
    val addedSuccessfully = _addedSuccessfully.asSharedFlow()

    val userRole = userRoleDataStore.userRole
    private val pageSize = 10

    private var currentDepartmentId: UUID = UUID.randomUUID()

    init {
        load(forceFetchFromRemote = false)
        viewModelScope.launch {
            userInfoDataStore.userInfoFlow.collectLatest { user ->
                _state.update {
                    it.copy(user = user)
                }
            }
            Timber.d("User Info: ${userInfoDataStore.userInfoFlow.first()}")
        }
    }

    fun onIntent(intent: TasksIntents) {
        when (intent) {
            is TasksIntents.AddTask -> addTask(
                intent.title,
                intent.description,
                intent.dueDate,
                intent.priority,
                intent.employeeId
            )

            is TasksIntents.LoadNextPage -> loadNextPage()
            is TasksIntents.LoadPreviousPage -> loadPreviousPage()
            is TasksIntents.LoadTasks -> load(intent.forceFetchFromRemote)
            is TasksIntents.OnSearchQueryChange -> updateSearchQuery(intent.query)
            is TasksIntents.Refresh -> {
                viewModelScope.launch {
                    _state.update { it.copy(isRefreshing = true) }
                    try {
                        userRole.collectLatest { role ->
                            if (role == "Manager") {
                                loadManagerTasks(true)
                            } else {
                                load(true)
                            }
                        }
                        delay(300)
                    } finally {
                        _state.update { it.copy(isRefreshing = false) }
                    }
                }
            }
            is TasksIntents.LoadManagerTasks -> loadManagerTasks(intent.forceFetchFromRemote)
            is TasksIntents.SetSortOption -> TODO()
        }
    }

    private fun addTask(
        title: String,
        description: String,
        dueDate: String,
        priority: Int,
        employeeId: UUID,
    ) {
        viewModelScope.launch {

            val managerResult = getCurrentManager()
            if (managerResult is Resource.Error) {
                _state.update {
                    it.copy(errorMessage = managerResult.message)
                }
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                managerRepository.addTask(
                    title,
                    description,
                    dueDate,
                    priority,
                    1,
                    currentDepartmentId,
                    employeeId,
                    UUID.fromString(managerId)
                )
            }
            when (result) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(errorMessage = result.message)
                    }
                }

                is Resource.Loading -> {
                    _state.update {
                        it.copy(isLoading = result.isLoading)
                    }
                }

                is Resource.Success -> {
                    _state.update {
                        it.copy(errorMessage = null)
                    }
                    Timber.d("About to emit addedSuccessfully")
                    _addedSuccessfully.emit(Unit)
                    Timber.d("Emitted addedSuccessfully")
                }
            }
        }
    }
    private fun ManagerAndEmployee.toUser(): User {
        return User(
            id = this.id,
            firstName = this.firstName,
            lastName = this.lastName,
            phoneNumber = this.phoneNumber,
            gender = this.gender,
            birthDate = birthDate
        )
    }
    private suspend fun getCurrentManager(): Resource<ManagerAndEmployee> {
        val managerUUID = try {
            UUID.fromString(managerId)
        } catch (e: IllegalArgumentException) {
            Timber.e("Invalid manager ID format: $managerId")
            return Resource.Error("Invalid manager ID format")
        }

        return withContext(Dispatchers.IO) {
            val result = sharedRepository.getManagerById(managerId = managerUUID)
            if (result is Resource.Success) {
                currentDepartmentId = result.data.departmentId
                _state.update { it.copy(user = result.data.toUser()) }
            }
            result
        }
    }

    private fun loadManagerTasks(forceFetchFromRemote: Boolean) {
        Timber.tag("Tasks for manager").d("Loading tasks for manager id: ($managerId) ok?")

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) {
                    sharedRepository.getTasksManagedByManager(
                        managerId = UUID.fromString(managerId),
                        page = 1,
                        limit = pageSize,
                        search = null,
                        sort = null,
                        forceFetchFromRemote = forceFetchFromRemote,
                    )
                }.collectLatest { resource ->
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

    private fun updateSearchQuery(query: String) {
        if (_state.value.isLoading) return
        _state.update { state ->
            state.copy(
                searchQuery = query.takeIf { query ->
                    query.isNotBlank()
                })
        }
        load(forceFetchFromRemote = false)
    }

    private fun load(forceFetchFromRemote: Boolean) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            adminRepository.getTasks(
                page = _state.value.currentPage,
                limit = pageSize,
                search = _state.value.searchQuery,
                sort = null,
                forceFetchFromRemote = forceFetchFromRemote,
                isRefreshing = forceFetchFromRemote
            ).collectLatest { paginatedTasks ->
                when (paginatedTasks) {
                    is Resource.Error -> {
                        _state.update {
                            it.copy(errorMessage = paginatedTasks.message, isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _state.update {
                            it.copy(isLoading = paginatedTasks.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        val tasks = paginatedTasks.data.data
                        if (tasks != null) {
                            _state.update {
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
        if (!_state.value.hasNextPage || _state.value.isLoading) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch(Dispatchers.IO) {
            adminRepository.getTasks(
                page = _state.value.currentPage + 1,
                limit = pageSize,
                search = _state.value.searchQuery,
                sort = null,
                forceFetchFromRemote = false,
                isRefreshing = false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _state.update {
                            it.copy(errorMessage = resource.message, isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _state.update {
                            it.copy(isLoading = resource.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        val tasks = resource.data.data
                        if (tasks != null) {
                            _state.update {
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
        if (!_state.value.hasPreviousPage || _state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            adminRepository.getTasks(
                page = _state.value.currentPage - 1,
                limit = pageSize,
                search = _state.value.searchQuery,
                sort = null,
                forceFetchFromRemote = false,
                isRefreshing = false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _state.update {
                            it.copy(errorMessage = resource.message, isLoading = false)
                        }
                    }

                    is Resource.Loading -> {
                        _state.update {
                            it.copy(isLoading = resource.isLoading)
                        }
                    }

                    is Resource.Success -> {
                        val tasks = resource.data.data
                        if (tasks != null) {
                            _state.update {
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