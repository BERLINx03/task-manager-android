package com.example.taskmanager.auth.presentation.event

import com.example.taskmanager.auth.data.remote.requestmodels.ForgotPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ResetPasswordRequestDto

/**
 * @author Abdallah Elsokkary
 */
sealed class LoginUiEvent {
    data class Loading(val message: String?) : LoginUiEvent()
    data class Login(val loginRequest: LoginRequest) : LoginUiEvent()
    data object Logout : LoginUiEvent()
    data class OnUsernameChange(val username: String) : LoginUiEvent()
    data class OnPasswordChange(val password: String) : LoginUiEvent()
    data class ForgotPassword(val forgotPasswordRequest: ForgotPasswordRequestDto) : LoginUiEvent()
    data class ResetPassword(val resetPasswordRequest: ResetPasswordRequestDto) : LoginUiEvent()
    data class OnOtpChange(val otp: String) : LoginUiEvent()
    data class OnNewPasswordChange(val newPassword: String) : LoginUiEvent()
    data class Error(val message: String) : LoginUiEvent()
}