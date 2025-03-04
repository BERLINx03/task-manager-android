package com.example.taskmanager.manager.data.remote.dto

import java.util.UUID

data class CreateTaskRequestDto(
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: Int,
    val status: Int,
    val departmentId: UUID,
    val employeeId: UUID,
    val managerId: UUID
)
