package com.example.taskmanager.core.presentation.intents

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed interface ProfileIntents {
    data class DeleteEmployee(val employeeId: UUID) : ProfileIntents
    data object Refresh : ProfileIntents
    data class DeleteManager(val managerId: UUID) : ProfileIntents
    data class UpdateProfile(val firstName: String, val lastName: String, val phoneNumber: String,val gender: Int, val birthDate: String) : ProfileIntents
    data object LoadCurrentUser : ProfileIntents
}