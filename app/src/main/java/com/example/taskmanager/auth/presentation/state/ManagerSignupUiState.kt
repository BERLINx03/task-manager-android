package com.example.taskmanager.auth.presentation.state

import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest

data class ManagerSignupUiState(
    val managerSignupUiState: ManagerSignupRequest = ManagerSignupRequest(
        firstName = "",
        lastName = "",
        phoneNumber = "",
        gender = 0,
        birthDate = "",
        departmentId = "",
        username = "",
        password = "",
        otpEmailVerifyCode = ""
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
