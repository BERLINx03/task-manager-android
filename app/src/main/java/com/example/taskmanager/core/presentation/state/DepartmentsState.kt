package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.Department


data class DepartmentsState(
    val departments: List<Department> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String? = null
)
