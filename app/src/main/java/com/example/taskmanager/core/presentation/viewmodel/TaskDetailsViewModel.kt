package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.domain.model.User
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.TaskDetailsIntents
import com.example.taskmanager.core.presentation.state.TaskDetailsState
import com.example.taskmanager.core.utils.Resource
import com.example.taskmanager.manager.data.remote.dto.CreateTaskRequestDto
import com.example.taskmanager.manager.domain.repository.ManagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
class TaskDetailsViewModel @Inject constructor(
    private val sharedRepository: SharedRepository,
    private val managerRepository: ManagerRepository,
    savedStateHandle: SavedStateHandle,
    userRoleDataStore: TokenDataStore,
    userInfo: UserInfoDataStore
) : ViewModel() {
    val managerId = savedStateHandle.get<String>("managerId") ?: ""
    val taskId = savedStateHandle.get<String>("taskId") ?: ""
    val userType = savedStateHandle.get<String>("role") ?: "" //for employees so they're the only one to change
    private val _state = MutableStateFlow(TaskDetailsState())
    val state = _state

    val role = userRoleDataStore.userRole

    private var currentDepartmentId: UUID = UUID.randomUUID()

    init {
        Timber.d("Manager ID: $managerId")
        viewModelScope.launch {
            userInfo.userInfoFlow.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
        Timber.d("init block from task details has been called and Task ID: $taskId")
        load()
    }

    fun onIntent(intent: TaskDetailsIntents) {
        when (intent) {
            is TaskDetailsIntents.LoadTaskDetails -> load()
            is TaskDetailsIntents.Refresh -> load(true)
            is TaskDetailsIntents.UpdateTask -> {
                if (taskId.isBlank()) {
                    _state.update { it.copy(errorMessage = "No task ID provided for update") }
                    return
                }
                try {
                    updateTask(UUID.fromString(taskId), intent.task)
                } catch (e: IllegalArgumentException) {
                    _state.update { it.copy(errorMessage = "Invalid task ID format: ${e.message}") }
                }
            }
            is TaskDetailsIntents.DeleteTask -> {
                if (taskId.isBlank()) {
                    _state.update { it.copy(errorMessage = "No task ID provided for deletion") }
                    return
                }
                try {
                    deleteTask(UUID.fromString(taskId))
                } catch (e: IllegalArgumentException) {
                    _state.update { it.copy(errorMessage = "Invalid task ID format: ${e.message}") }
                }
            }
            is TaskDetailsIntents.LoadEmployeesInDepartment -> getAllEmployeeInDepartment(intent.forceFetchFromRemote)
        }
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
    private fun getAllEmployeeInDepartment(forceFetchFromRemote: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val managerResult = getCurrentManager()
            if (managerResult is Resource.Error) {
                _state.update {
                    it.copy(errorMessage = managerResult.message)
                }
                return@launch
            }
            withContext(Dispatchers.IO) {
                sharedRepository.getAllEmployeesInDepartment(
                    departmentId = currentDepartmentId,
                    forceFetchFromRemote = forceFetchFromRemote,
                    search = null,
                    sort = null
                ).collect { resource ->
                    when (resource) {
                        is Resource.Error -> {
                            _state.update {
                                it.copy(errorMessage = resource.message)
                            }
                        }

                        is Resource.Loading -> {
                            if (!resource.isLoading) {
                                _state.update { it.copy(isLoading = false) }
                            }
                        }

                        is Resource.Success -> {
                            _state.update {
                                it.copy(
                                    employeesInDepartment = resource.data,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun load(forceFetchFromRemote: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (taskId.isBlank()) {
                getAllEmployeeInDepartment(forceFetchFromRemote)
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            when (val result = loadTaskFromIO(forceFetchFromRemote)) {
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
                    Timber.d("Task Details: ${result.data}")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            task = result.data
                        )
                    }
                }
            }
        }
    }

    private fun updateTask(taskId: UUID, task: CreateTaskRequestDto) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            Timber.d("Task Details: $task and $taskId and $userType and ${_state.value.user?.id ?: "not found mananger"}" +
                    "$currentDepartmentId")
            when (val result = updateTaskFromIO(taskId, task)) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                is Resource.Loading -> {}

                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                        )
                    }
                    load(true)
                }
            }
        }
    }

    private fun deleteTask(taskId: UUID){
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = deleteTaskFromIO(taskId)) {
                is Resource.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            task = null
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadTaskFromIO(forceFetchFromRemote: Boolean): Resource<Task> = withContext(Dispatchers.IO) {
        if (taskId.isBlank()) {
            return@withContext Resource.Error("No task ID provided")
        }
        try {
            sharedRepository.getTaskById(UUID.fromString(taskId), forceFetchFromRemote)
        } catch (e: IllegalArgumentException) {
            Resource.Error("Invalid task ID format: ${e.message}")
        }
    }

    private suspend fun updateTaskFromIO(taskId: UUID, task: CreateTaskRequestDto) = withContext(Dispatchers.IO) {
        managerRepository.updateTask(taskId,task)
    }

    private suspend fun deleteTaskFromIO(taskId: UUID) = withContext(Dispatchers.IO) {
        managerRepository.deleteTask(taskId)
    }
}