package com.example.taskmanager.auth.presentation.state

import com.example.taskmanager.auth.data.remote.requestmodels.AdminSignupRequest

data class AdminSignupUiState(
    val adminSignupRequest: AdminSignupRequest = AdminSignupRequest(
        firstName = "",
        lastName = "",
        phoneNumber = "",
        gender = 0,
        birthDate = "",
        username = "",
        password = ""
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
)
