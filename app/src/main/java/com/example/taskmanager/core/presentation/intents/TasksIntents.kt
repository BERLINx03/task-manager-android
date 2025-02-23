package com.example.taskmanager.core.presentation.intents

/**
 * @author Abdallah Elsokkary
 */
sealed class TasksIntents {
    data class LoadTasks(val forceFetchFromRemote: Boolean = false): TasksIntents()
    data object LoadNextPage: TasksIntents()
    data object LoadPreviousPage: TasksIntents()
    data class OnSearchQueryChange(val query: String): TasksIntents()
    data class AddTask(val title: String): TasksIntents()
    data object Refresh: TasksIntents()
    data object OnTitleChanged: TasksIntents()
    data class Navigating(val taskId: Int): TasksIntents()
    data class DeleteTask(val taskId: Int): TasksIntents()
    data class UpdateTask(val taskId: Int, val title: String): TasksIntents()
    data class UpdateTaskStatus(val taskId: Int, val status: String): TasksIntents()
    data class UpdateTaskPriority(val taskId: Int, val priority: String): TasksIntents()
    data class UpdateTaskDescription(val taskId: Int, val description: String): TasksIntents()
    data class UpdateTaskDepartment(val taskId: Int, val departmentId: Int): TasksIntents()
    data class UpdateTaskManager(val taskId: Int, val managerId: Int): TasksIntents()
    data class UpdateTaskEmployee(val taskId: Int, val employeeId: Int): TasksIntents()
    data class UpdateTaskDueDate(val taskId: Int, val dueDate: String): TasksIntents()
    data class UpdateTaskEndDate(val taskId: Int, val endDate: String): TasksIntents()
    data class UpdateTaskStartDate(val taskId: Int, val startDate: String): TasksIntents()
}
