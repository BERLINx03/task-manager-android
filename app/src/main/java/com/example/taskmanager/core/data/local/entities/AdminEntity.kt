package com.example.taskmanager.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey val id: UUID,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val current: Boolean = false
)
