package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.User


data class DepartmentsState(
    val sortOption: String = "title_asc",
    val title: String = "",
    val user: User? = null,
    val isRefreshing: Boolean = false,
    val departments: List<Department> = emptyList(),
    val selectedDepartment: Department? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String? = null
)
