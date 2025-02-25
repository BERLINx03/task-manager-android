package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee

data class DepartmentDetailsState(
    val department: Department? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    data class DepartmentManagersState(
        val managers: List<ManagerAndEmployee> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val sortOption: String = "firstName_asc",
        val currentPage: Int = 1,
        val totalPages: Int = 1,
        val hasNextPage: Boolean = false,
        val hasPreviousPage: Boolean = false,
        val searchQuery: String? = null
    )

    data class DepartmentEmployeesState(
        val employees: List<ManagerAndEmployee> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val sortOption: String = "firstName_asc",
        val currentPage: Int = 1,
        val totalPages: Int = 1,
        val hasNextPage: Boolean = false,
        val hasPreviousPage: Boolean = false,
        val searchQuery: String? = null
    )
}



