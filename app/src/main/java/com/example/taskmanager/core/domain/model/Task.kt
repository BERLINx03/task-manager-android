package com.example.taskmanager.core.domain.model

import java.util.UUID

data class Task(
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: Int,
    val status: Int,
    val departmentId: UUID,
    val employeeId: UUID,
    val managerId: UUID,
    val id: UUID
)
