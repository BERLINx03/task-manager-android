package com.example.taskmanager.core.data.remote

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
interface SharedApiService{

    // All
    @GET("/Department")
    suspend fun getDepartments(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.DepartmentResponse>>


    // All
    @GET("/Department/{DepartmentId}")
    suspend fun getDepartmentById(
        @Path("DepartmentId") departmentId: UUID
    ): ResponseDto<ResponseDto.DepartmentResponse>


    //(all)
    @GET("/Department/{DepartmentId}/Employees")
    suspend fun getEmployeesInDepartment(
        @Path("DepartmentId") departmentId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>>

    //(All)
    @GET("/Department/{DepartmentId}/Managers")
    suspend fun getManagersInDepartment(
        @Path("DepartmentId") departmentId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>>


    //(Admin, Manager)
    @GET("/Managers")
    suspend fun getManagers(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>>

    //(Admin, Manager)
    @GET("/Managers/{managerId}")
    suspend fun getManagerById(
        @Path("managerId") managerId: UUID
    ): ResponseDto<ResponseDto.ManagerAndEmployeeResponse>

    //All
    @GET("/Employees/{employeeId}")
    suspend fun getEmployeeById(
        @Path("employeeId") employeeId: UUID
    ): ResponseDto<ResponseDto.ManagerAndEmployeeResponse>

    //(Admin, Manager)
    @GET("/Employees")
    suspend fun getEmployees(
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>>


    // All
    @GET("/Tasks/{taskId}")
    suspend fun getTaskById(
        @Path("taskId") taskId: UUID
    ): ResponseDto<ResponseDto.TaskResponse>


    // (Admin, Manager, Employee)
    @GET("/Tasks/Employee/{employeeId}")
    suspend fun getTasksAssignedToEmployee(
        @Path("employeeId") employeeId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>>


    //(Admin, Manager)
    @GET("/Tasks/Manager/{managerId}")
    suspend fun getTasksManagedByManager(
        @Path("managerId") managerId: UUID,
        @Query("Page") page: Int,
        @Query("Limit") limit: Int,
        @Query("Category") search: String? = null,
        @Query("Sort") sort: String? = null
    ): ResponseDto<ResponseDto.PaginatedResponse<ResponseDto.TaskResponse>>
}