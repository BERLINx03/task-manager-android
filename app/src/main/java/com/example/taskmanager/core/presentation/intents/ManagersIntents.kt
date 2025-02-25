package com.example.taskmanager.core.presentation.intents

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed interface ManagersIntents {
    data class Load(val forceFetchFromRemote: Boolean) : ManagersIntents
    data object LoadNextPage : ManagersIntents
    data object LoadPreviousPage : ManagersIntents
    data object Refresh : ManagersIntents
    data class OnSearchQueryChange(val query: String) : ManagersIntents
}