package com.example.taskmanager.admin.presentation.state

data class DashboardState(
    val error: String? = null,
    val isLoading: Boolean = false,
    val adminsCount: Int = 0,
    val departmentsCount: Int = 0,
    val tasksCount: Int = 0,
    val employeesCount: Int = 0,
    val managersCount: Int = 0,
)
