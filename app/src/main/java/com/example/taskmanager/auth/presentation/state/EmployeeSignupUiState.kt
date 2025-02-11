package com.example.taskmanager.auth.presentation.state

import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest

data class EmployeeSignupUiState(
    val employeeSignupRequest: EmployeeSignupRequest = EmployeeSignupRequest(
        firstName = "",
        lastName = "",
        phoneNumber = "",
        gender = 0,
        birthDate = "",
        departmentId = "",
        username = "",
        password = "",
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
