package com.example.taskmanager.core.presentation.state

import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.domain.model.User
import java.util.UUID

data class TaskDetailsState(
    val user: User? = null,
    val employeesInDepartment: List<ManagerAndEmployee> = emptyList(),
    val task: Task? = Task(
        title = "",
        description = "",
        dueDate = "",
        priority = 2,
        status = 1,
        departmentId = UUID.randomUUID(),
        employeeId = UUID.randomUUID(),
        managerId = UUID.randomUUID(),
        id = UUID.randomUUID()
    ),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pdfDownloadProgress: Float? = null
)
