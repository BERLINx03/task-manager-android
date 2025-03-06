package com.example.taskmanager.core.presentation.intents

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed interface TasksIntents {
    data class LoadTasks(val forceFetchFromRemote: Boolean = false): TasksIntents
    data object LoadNextPage: TasksIntents
    data object LoadPreviousPage: TasksIntents
    data class LoadManagerTasks(val forceFetchFromRemote: Boolean = false): TasksIntents
    data class OnSearchQueryChange(val query: String): TasksIntents
    data class AddTask(
        val title: String,
        val description: String,
        val dueDate: String,
        val priority: Int,
        val employeeId: UUID,
    ): TasksIntents
    data object Refresh: TasksIntents
    data class SetSortOption(val sortOption: String) : TasksIntents
}
