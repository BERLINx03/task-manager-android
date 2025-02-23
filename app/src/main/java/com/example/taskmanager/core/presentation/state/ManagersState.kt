package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.User

data class ManagersState(
    val user: User? = null,
    val isRefreshing: Boolean = false,
    val managers: List<ManagerAndEmployee> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String? = null
)
