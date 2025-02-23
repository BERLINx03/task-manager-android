package com.example.taskmanager.core.domain.repository

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.PaginatedData
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

    suspend fun getEmployeeById(employeeId: UUID): Resource<ManagerAndEmployee>
    // All
    suspend fun getDepartmentById(departmentId: UUID): Resource<Department>
}