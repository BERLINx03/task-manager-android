package com.example.taskmanager.admin.domain.model

import java.util.UUID

data class Admin(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
)
