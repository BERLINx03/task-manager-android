package com.example.taskmanager.core.presentation.intents

import com.example.taskmanager.manager.data.remote.dto.CreateTaskRequestDto
import java.util.UUID

sealed interface TaskDetailsIntents {
    data class LoadTaskDetails(val taskId: UUID) : TaskDetailsIntents
    data class UpdateTask(val task: CreateTaskRequestDto) : TaskDetailsIntents
    data object DeleteTask : TaskDetailsIntents
    data class ReassignTask(
        val taskId: UUID,
        val newEmployeeId: UUID,
        val newManagerId: UUID,
        val newDepartmentId: UUID
    ) : TaskDetailsIntents
    data class DownloadTaskPdf(val taskId: UUID) : TaskDetailsIntents
    data object Refresh : TaskDetailsIntents
    data class LoadEmployeesInDepartment(val forceFetchFromRemote: Boolean = false): TaskDetailsIntents

}
