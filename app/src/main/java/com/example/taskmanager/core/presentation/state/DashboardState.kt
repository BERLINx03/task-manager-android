package com.example.taskmanager.core.presentation.state


import com.example.taskmanager.core.domain.model.User

data class DashboardState(
    val error: String? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val adminsCount: Int = 0,
    val departmentsCount: Int = 0,
    val tasksCount: Int = 0,
    val employeesCount: Int = 0,
    val managersCount: Int = 0,
    val user: User? = null
)