package com.example.taskmanager.auth.data.remote.reponsemodels

import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
data class ResponseDto<T>(
    val isSuccess: Boolean,
    val message: String,
    val errors: List<String>?,
    val data: T?,
    val statusCode: Int
){
    data class LoginData(
        val token: TokenInfo,
        val role: String
    )

    data class AdminResponse(
        val firstName: String,
        val lastName: String,
        val phoneNumber: String,
        val gender: Int,
        val birthDate: String,
        val id: UUID
    )

    data class ManagerAndEmployeeResponse(
        val firstName: String,
        val lastName: String,
        val phoneNumber: String,
        val gender: Int,
        val birthDate: String,
        val departmentId: UUID,
        val id: UUID
    )


    data class PaginatedResponse<T>(
        val items: List<T>,
        val page: Int,
        val pageSize: Int,
        val totalCount: Int,
        val hasNextPage: Boolean,
        val hasPreviousPage: Boolean
    )

    data class TaskResponse(
        val title: String,
        val description: String,
        val dueDate: String,
        val priority: Int,
        val status: Int,
        val departmentId: UUID,
        val employeeId: UUID,
        val managerId: UUID,
        val id: UUID
    )

    data class DepartmentResponse(
        val title: String,
        val id: UUID
    )

    data class TokenInfo(
        val accessToken: String
    )
}