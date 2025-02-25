package com.example.taskmanager.admin.data.remote

import com.example.taskmanager.admin.data.remote.dto.UpdateAdminRequestDto
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.remote.dto.DepartmentRequestDto
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

    @DELETE("/Managers/{managerId}")
    suspend fun deleteManager(
        @Path("managerId") managerId: UUID
    ): ResponseDto<String>

    @DELETE("/Employees/{employeeId}")
    suspend fun deleteEmployee(
        @Path("employeeId") employeeId: UUID
    ): ResponseDto<String>

    @POST("/Department")
    suspend fun addDepartment(
        @Body departmentRequestDto: DepartmentRequestDto
    ): ResponseDto<UUID>


    @GET("/Tasks")
    suspend fun getTasks(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>>

    @PUT("/Department/{DepartmentId}")
    suspend fun updateDepartment(
        @Path("DepartmentId") departmentId: UUID,
        @Body updateDepartmentRequestDto: DepartmentRequestDto
    ): ResponseDto<String>

    @DELETE("/Department/{DepartmentId}")
    suspend fun deleteDepartment(
        @Path("DepartmentId") departmentId: UUID
    ): ResponseDto<String>


}