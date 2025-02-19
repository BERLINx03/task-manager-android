package com.example.taskmanager.admin.domain.repository

import com.example.taskmanager.admin.data.remote.dto.UpdateAdminRequestDto
import com.example.taskmanager.admin.domain.model.Admin
import com.example.taskmanager.admin.domain.model.Department
import com.example.taskmanager.admin.domain.model.PaginatedData
import com.example.taskmanager.admin.domain.model.Task
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.utils.Resource
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
interface AdminRepository {

    suspend fun getCurrentAdmin(): Resource<ResponseDto<Admin>>

    suspend fun getAdminById(adminId: UUID): Resource<ResponseDto<Admin>>

    suspend fun getAdmins(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean,
        isRefreshing: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<Admin>>>>

    suspend fun updateAdmin(admin: UpdateAdminRequestDto): Resource<String>


    suspend fun createDepartment(title: String): Resource<ResponseDto<Department>>

    suspend fun getDepartments(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Department>>>

    suspend fun getDepartmentById(departmentId: String): Resource<ResponseDto<Department>>
    suspend fun updateDepartment(departmentId: String, title: String): Resource<ResponseDto<String>>
    suspend fun deleteDepartment(departmentId: String): Resource<ResponseDto<String>>
    suspend fun getDepartmentEmployees(departmentId: String, page: Int, limit: Int, search: String?, sort: String?): Resource<ResponseDto<PaginatedData<Department>>>
    suspend fun getDepartmentManagers(departmentId: String, page: Int, limit: Int, search: String?, sort: String?): Resource<ResponseDto<PaginatedData<Department>>>

    suspend fun getAllTask(page: Int, limit: Int, category: String?, sort: String?): Resource<ResponseDto<PaginatedData<Task>>>
    suspend fun getTaskById(taskId: UUID): Resource<ResponseDto<Task>>
    suspend fun getEmployeeTasks(employeeId: UUID, page: Int, limit: Int, search: String?, sort: String?): Resource<ResponseDto<PaginatedData<Task>>>
    suspend fun getManagerTasks(managerId: UUID, page: Int, limit: Int, search: String?, sort: String?): Resource<ResponseDto<PaginatedData<Task>>>
}