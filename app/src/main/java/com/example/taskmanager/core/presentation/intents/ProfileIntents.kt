package com.example.taskmanager.core.presentation.intents

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed interface ProfileIntents {
    data class DeleteEmployee(val employeeId: UUID) : ProfileIntents
    data object Refresh : ProfileIntents
    data class DeleteManager(val managerId: UUID) : ProfileIntents
}