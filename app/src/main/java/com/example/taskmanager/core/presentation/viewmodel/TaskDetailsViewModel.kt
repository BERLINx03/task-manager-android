package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.TaskDetailsIntents
import com.example.taskmanager.core.presentation.state.TaskDetailsState
import com.example.taskmanager.core.utils.Resource
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
    savedStateHandle: SavedStateHandle,
    userRoleDataStore: TokenDataStore
) : ViewModel() {
    val taskId = savedStateHandle.get<String>("taskId") ?: ""
    val userType = savedStateHandle.get<String>("role") ?: "" //for employees so they're the only one to change
    private val _state = MutableStateFlow(TaskDetailsState())
    val state = _state

    val role = userRoleDataStore.userRole

    init {
        Timber.d("init block from task details has been called and Task ID: $taskId")
        load()
    }

    fun onIntent(intent: TaskDetailsIntents) {
        when (intent) {
            is TaskDetailsIntents.DownloadTaskPdf -> TODO()
            is TaskDetailsIntents.LoadTaskDetails -> load()
            is TaskDetailsIntents.ReassignTask -> TODO()
            TaskDetailsIntents.Refresh -> TODO()
            is TaskDetailsIntents.UpdateTaskDescription -> TODO()
            is TaskDetailsIntents.UpdateTaskDueDate -> TODO()
            is TaskDetailsIntents.UpdateTaskPriority -> TODO()
            is TaskDetailsIntents.UpdateTaskStatus -> TODO()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = loadTaskFromIO()) {
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

    private suspend fun loadTaskFromIO(): Resource<Task> = withContext(Dispatchers.IO) {
        sharedRepository.getTaskById(UUID.fromString(taskId))
    }
}
