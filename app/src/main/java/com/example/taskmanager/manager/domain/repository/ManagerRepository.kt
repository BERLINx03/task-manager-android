package com.example.taskmanager.manager.domain.repository

import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.utils.Resource
import com.example.taskmanager.manager.data.remote.dto.CreateTaskRequestDto
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
interface ManagerRepository {
    suspend fun addTask(
        title: String,
        description: String,
        dueDate: String,
        priority: Int,
        status: Int,
        departmentId: UUID,
        employeeId: UUID,
        managerId: UUID
    ): Resource<Task>

    suspend fun updateTask(
        taskId: UUID,
        task: CreateTaskRequestDto
    ): Resource<String>

    suspend fun deleteTask(
        taskId: UUID
    ): Resource<String>

}