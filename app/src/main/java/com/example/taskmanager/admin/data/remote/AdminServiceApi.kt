package com.example.taskmanager.admin.data.remote

import com.example.taskmanager.admin.data.remote.dto.UpdateAdminRequestDto
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
interface AdminServiceApi {

    @GET("/Admin/Current")
    suspend fun getCurrentAdmin(): ResponseDto<ResponseDto.AdminResponse>

    @GET("Admin/{adminId}")
    suspend fun getAdminById(@Path("adminId") adminId: UUID): ResponseDto<ResponseDto.AdminResponse>

    @GET("/Admin")
    suspend fun getAdmins(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.AdminResponse>>

    @PUT("/Admin")
    suspend fun updateAdmin(
        @Body admin: UpdateAdminRequestDto
    ) : ResponseDto<UUID>

    @POST("/Department")
    suspend fun createDepartment(
        @Body title: String
    ): ResponseDto<ResponseDto.DepartmentResponse>

    @GET("/Department")
    suspend fun getDepartments(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.DepartmentResponse>>

    @GET("/Department/{DepartmentId}")
    suspend fun getDepartmentById(
        @Path("DepartmentId") departmentId: UUID
    ): ResponseDto<ResponseDto.DepartmentResponse>

    @PUT("/Department/{DepartmentId}")
    suspend fun updateDepartment(
        @Path("DepartmentId") departmentId: UUID,
        @Body title: String
    ): ResponseDto<UUID>

    @DELETE("/Department/{DepartmentId}")
    suspend fun deleteDepartment(
        @Path("DepartmentId") departmentId: UUID
    ): ResponseDto<String>

    @GET("/Department/{DepartmentId}/Employees")
    suspend fun getDepartmentEmployees(
        @Path("DepartmentId") departmentId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>>

    @GET("/Department/{DepartmentId}/Managers")
    suspend fun getDepartmentManagers(
        @Path("DepartmentId") departmentId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>>

    @GET("/Tasks")
    suspend fun getAllTask(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>>

    @GET("/Tasks/{taskId}")
    suspend fun getTaskById(
        @Path("taskId") taskId: UUID
    ): ResponseDto<ResponseDto.TaskResponse>

    @GET("/Tasks/Employee/{employeeId}")
    suspend fun getEmployeeTasks(
        @Path("employeeId") employeeId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>>

    @GET("/Tasks/Manager/{managerId}")
    suspend fun getManagerTasks(
        @Path("managerId") managerId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>>
}