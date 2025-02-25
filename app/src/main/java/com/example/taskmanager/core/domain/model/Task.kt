package com.example.taskmanager.core.domain.model

import java.util.UUID
/**
 * Represents a task in the system
 * @property status Task status (1 = TO_DO, 2 = IN_PROGRESS, 3 = DONE)
 * @property priority Task priority (0 = HIGH, 1 = MEDIUM, 2 = LOW)
 */
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
