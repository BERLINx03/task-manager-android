package com.example.taskmanager.auth.data.remote.requestmodels

data class ResetPasswordRequestDto(
    val email: String,
    val otpCode: String,
    val newPassword: String
)
