package com.example.taskmanager.core.presentation.intents

/**
 * @author Abdallah Elsokkary
 */
sealed interface EmployeesIntents {
    data class Load(val forceFetchFromRemote: Boolean) : EmployeesIntents
    data object LoadNextPage : EmployeesIntents
    data object LoadPreviousPage : EmployeesIntents
    data object Refresh : EmployeesIntents
    data class SetSortOption(val sortOption: String) : EmployeesIntents
    data class OnSearchQueryChange(val query: String) : EmployeesIntents
}