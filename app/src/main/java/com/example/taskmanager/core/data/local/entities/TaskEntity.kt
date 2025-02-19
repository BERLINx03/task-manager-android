package com.example.taskmanager.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: UUID,
    val title: String,
    val description: String,
    val dueDate: String,
    val priority: Int,
    val status: Int,
    val departmentId: UUID,
    val employeeId: UUID,
    val managerId: UUID,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
