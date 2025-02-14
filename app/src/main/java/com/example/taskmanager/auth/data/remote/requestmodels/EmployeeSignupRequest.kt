package com.example.taskmanager.auth.data.remote.requestmodels

import java.util.UUID

data class EmployeeSignupRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
    val departmentId: UUID,
    val username: String,
    val password: String,
    val otpEmailVerifyCode: String
)
