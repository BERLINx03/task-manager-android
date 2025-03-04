package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.domain.model.User

data class TasksState(
    val sortOption: String = "title_asc",
    val title: String = "",
    val addSuccessfully: Boolean = false,
    val user: User? = null,
    val isRefreshing: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
