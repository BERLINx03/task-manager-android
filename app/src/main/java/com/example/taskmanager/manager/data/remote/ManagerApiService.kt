package com.example.taskmanager.manager.data.remote

import com.example.taskmanager.manager.data.remote.dto.CreateTaskRequestDto
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
interface ManagerApiService {

    @POST("/Tasks")
    suspend fun addTask(
        @Body task: CreateTaskRequestDto
    ): ResponseDto<ResponseDto.TaskResponse>

    @PUT("/Tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: UUID,
        @Body task: CreateTaskRequestDto
    ): ResponseDto<UUID>

    @DELETE("/Tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: UUID
    ): ResponseDto<String>


}