package com.example.taskmanager.auth.presentation.event

import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest

/**
 * @author Abdallah Elsokkary
 */
sealed class LoginUiEvent {
    data class Loading(val message: String?) : LoginUiEvent()
    data class Login(val loginRequest: LoginRequest) : LoginUiEvent()
    data class Error(val message: String) : LoginUiEvent()
}