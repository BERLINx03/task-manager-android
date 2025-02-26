package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.Task

data class ProfileState(
    val role: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val department: String = "",
    val tasksCompleted: Int = 0,
    val gender: Int = 0,
    val isRefreshing: Boolean = false,
    val birthDate: String = "",
    val tasks: List<Task> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = "",
    val deletedSuccessfully: Boolean = false
)
