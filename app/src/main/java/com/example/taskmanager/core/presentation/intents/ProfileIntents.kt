package com.example.taskmanager.core.presentation.intents

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed interface ProfileIntents {
    data class LoadProfile(val userId: UUID) : ProfileIntents
    data class LoadTasks(val forceFetchFromRemote: Boolean) : ProfileIntents
    data class DeleteUser(val userId: UUID) : ProfileIntents
    data object Refresh : ProfileIntents
}