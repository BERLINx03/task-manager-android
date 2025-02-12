package com.example.taskmanager.auth.presentation.state

import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import java.util.UUID

data class EmployeeSignupUiState(
    val employeeSignupRequest: EmployeeSignupRequest = EmployeeSignupRequest(
        firstName = "",
        lastName = "",
        phoneNumber = "",
        gender = 0,
        birthDate = "",
        departmentId = UUID(0L, 0L) ,
        username = "",
        password = "",
    ),
    val departmentIdText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
