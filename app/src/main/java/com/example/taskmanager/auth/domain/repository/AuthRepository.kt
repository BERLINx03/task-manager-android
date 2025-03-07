package com.example.taskmanager.auth.domain.repository

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.auth.data.remote.requestmodels.AdminSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ForgotPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ResetPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto
import com.example.taskmanager.auth.utils.AuthResult


/**
 * @author Abdallah Elsokkary
 */
interface AuthRepository {

    suspend fun loginUser(loginRequest: LoginRequest): AuthResult<ResponseDto<ResponseDto.LoginData>> // Returns token
    suspend fun signupEmployee(employee: EmployeeSignupRequest): AuthResult<ResponseDto<String>>
    suspend fun signupManager(manager: ManagerSignupRequest): AuthResult<ResponseDto<String>>
    suspend fun signupAdmin(admin: AdminSignupRequest): AuthResult<ResponseDto<String>>
    suspend fun verifyEmail(verification: VerificationRequestDto): AuthResult<ResponseDto<String>>
    suspend fun forgetPassword(forgetPassword: ForgotPasswordRequestDto): AuthResult<ResponseDto<String>>
    suspend fun resetPassword(resetPassword: ResetPasswordRequestDto): AuthResult<ResponseDto<String>>


}