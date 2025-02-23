package com.example.taskmanager.core.presentation.intents

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed class ManagersIntents {
    data class Load(val forceFetchFromRemote: Boolean) : ManagersIntents()
    data object LoadNextPage : ManagersIntents()
    data object LoadPreviousPage : ManagersIntents()
    data object Refresh : ManagersIntents()
    data class OnSearchQueryChange(val query: String) : ManagersIntents()
    data class OnDeleteManager(val managerId: UUID) : ManagersIntents()
}