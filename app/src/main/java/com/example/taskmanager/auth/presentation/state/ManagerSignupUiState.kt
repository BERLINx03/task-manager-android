package com.example.taskmanager.auth.presentation.state

import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest
import java.util.UUID

data class ManagerSignupUiState(
    val managerSignupRequest: ManagerSignupRequest = ManagerSignupRequest(
        firstName = "",
        lastName = "",
        phoneNumber = "",
        gender = 0,
        birthDate = "",
        departmentId = UUID(0L, 0L),
        username = "",
        password = "",
        otpEmailVerifyCode = ""
    ),
    val departmentIdText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
