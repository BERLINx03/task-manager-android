package com.example.taskmanager.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "departments")
data class DepartmentEntity(
    @PrimaryKey val id: UUID,
    val title: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
