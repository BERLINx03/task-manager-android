package com.example.taskmanager.auth.presentation.state

import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest

data class LoginUiState(
    val loginRequest: LoginRequest = LoginRequest(
        username = "",
        password = ""
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)
