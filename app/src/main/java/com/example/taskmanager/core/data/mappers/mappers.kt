package com.example.taskmanager.core.data.mappers

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.local.entities.DepartmentEntity
import com.example.taskmanager.core.data.local.entities.ManagerEntity
import com.example.taskmanager.core.data.local.entities.TaskEntity
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.PaginatedData
import com.example.taskmanager.core.domain.model.Task

/**
 * @author Abdallah Elsokkary
 */

fun ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>.toPaginatedTasks(): PaginatedData<Task> {
    return PaginatedData(
        items = items.map { it.toTask() },
        page = page,
        pageSize = pageSize,
        totalCount = totalCount,
        hasNextPage = hasNextPage,
        hasPreviousPage = hasPreviousPage
    )
}

fun ResponseDto.TaskResponse.toTask(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        status = status,
        priority = priority,
        departmentId = departmentId,
        employeeId = employeeId,
        managerId = managerId
    )
}

fun ResponseDto.TaskResponse.toTaskEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        status = status,
        priority = priority,
        departmentId = departmentId,
        employeeId = employeeId,
        managerId = managerId
    )
}

fun TaskEntity.toTask(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        status = status,
        priority = priority,
        departmentId = departmentId,
        employeeId = employeeId,
        managerId = managerId
    )
}

fun ResponseDto.DepartmentResponse.toDepartmentEntity(): DepartmentEntity{
    return DepartmentEntity(
        title = title,
        id = id
    )
}

fun ResponseDto.DepartmentResponse.toDepartment(): Department{
    return Department(
        title = title,
        id = id
    )
}

fun DepartmentEntity.toDepartment(): Department{
    return Department(
        title = title,
        id = id
    )
}

fun ResponseDto.ManagerAndEmployeeResponse.toManagerAndEmployee(): ManagerAndEmployee{
    return ManagerAndEmployee(
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        gender = gender,
        birthDate = birthDate,
        departmentId = departmentId,
        id = id
    )
}

fun ManagerEntity.toManagerAndEmployee(): ManagerAndEmployee{
    return ManagerAndEmployee(
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        gender = gender,
        birthDate = birthDate,
        departmentId = departmentId,
        id = id
    )
}

fun ResponseDto.ManagerAndEmployeeResponse.toEntity(): ManagerEntity{
    return ManagerEntity(
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        gender = gender,
        birthDate = birthDate,
        departmentId = departmentId,
        id = id
    )
}