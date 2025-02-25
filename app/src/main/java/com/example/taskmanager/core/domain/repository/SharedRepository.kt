package com.example.taskmanager.core.domain.repository

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.PaginatedData
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
interface SharedRepository {
    // Admins, Managers
    suspend fun getPagedManagers(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<ManagerAndEmployee>>>>

    // admin, manager
    suspend fun getManagerById(managerId: UUID): Resource<ManagerAndEmployee>

    // admin, manager
    suspend fun getTasksManagedByManager(managerId: UUID, page: Int, limit: Int, search: String?, sort: String?, forceFetchFromRemote: Boolean): Flow<Resource<ResponseDto<PaginatedData<Task>>>>

    // All
    suspend fun getPagedEmployees(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<ManagerAndEmployee>>>>

    // all
    suspend fun getEmployeeById(employeeId: UUID): Resource<ManagerAndEmployee>

    // all but employee only his own tasks
    suspend fun getTasksAssignedToEmployee(employeeId: UUID, page: Int, limit: Int, search: String?, sort: String?, forceFetchFromRemote: Boolean): Flow<Resource<ResponseDto<PaginatedData<Task>>>>


    suspend fun getTaskById(taskId: UUID): Resource<Task>
    // All
    suspend fun getDepartmentById(departmentId: UUID): Resource<Department>
}