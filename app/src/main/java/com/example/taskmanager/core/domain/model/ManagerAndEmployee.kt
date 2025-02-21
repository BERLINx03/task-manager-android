package com.example.taskmanager.core.domain.model

import java.util.UUID

data class ManagerAndEmployee(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
    val departmentId: UUID,
    val id: UUID
)
