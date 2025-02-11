package com.example.taskmanager.auth.data.remote

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.auth.data.remote.requestmodels.AdminSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * @author Abdallah Elsokkary
 */
interface AuthApi {

    @POST("/Auth/Login")
    suspend fun loginUser(
        @Body loginRequest: LoginRequest
    ): ResponseDto<ResponseDto.LoginData>

    @POST("/Auth/SignUp/Employee")
    suspend fun signUpEmployee(
        @Body employeeSignupRequest: EmployeeSignupRequest
    ): ResponseDto<String>

    @POST("/Auth/SignUp/Manager")
    suspend fun signUpManager(
        @Body managerSignupRequest: ManagerSignupRequest
    ): ResponseDto<String>

    @POST("/Auth/SignUp/Admin")
    suspend fun signUpAdmin(
        @Body adminSignupRequest: AdminSignupRequest
    ): ResponseDto<String>
}