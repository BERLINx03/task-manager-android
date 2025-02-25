package com.example.taskmanager.core.presentation.intents

import java.util.UUID

sealed class TaskDetailsIntents {
    data class LoadTaskDetails(val taskId: UUID) : TaskDetailsIntents()
    data class UpdateTaskStatus(val taskId: UUID, val newStatus: Int) : TaskDetailsIntents()
    data class UpdateTaskPriority(val taskId: UUID, val newPriority: Int) : TaskDetailsIntents()
    data class UpdateTaskDueDate(val taskId: UUID, val newDueDate: String) : TaskDetailsIntents()
    data class UpdateTaskDescription(val taskId: UUID, val newDescription: String) : TaskDetailsIntents()
    data class ReassignTask(
        val taskId: UUID,
        val newEmployeeId: UUID,
        val newManagerId: UUID,
        val newDepartmentId: UUID
    ) : TaskDetailsIntents()
    data class DownloadTaskPdf(val taskId: UUID) : TaskDetailsIntents()
    data object Refresh : TaskDetailsIntents()
}
