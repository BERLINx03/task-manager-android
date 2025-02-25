package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.User

data class EmployeesState(
    val user: User? = null,
    val isRefreshing: Boolean = false,
    val employees: List<ManagerAndEmployee> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val sortOption: String = "name_asc",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String? = null
)
