package com.example.taskmanager.core.data.remote.dto

import java.util.UUID

data class UpdateManagerAndEmployeeRequestDto(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
    val departmentId: UUID
)
