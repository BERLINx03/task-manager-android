package com.example.taskmanager.manager.data.repository

import com.example.taskmanager.core.data.local.database.TaskManagerDatabase
import com.example.taskmanager.core.data.mappers.toTask
import com.example.taskmanager.core.data.mappers.toTaskEntity
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.utils.HttpStatusCodes
import com.example.taskmanager.core.utils.NetworkUtils
import com.example.taskmanager.core.utils.Resource
import com.example.taskmanager.core.utils.getIOExceptionMessage
import com.example.taskmanager.core.utils.getUserFriendlyMessage
import com.example.taskmanager.manager.data.remote.ManagerApiService
import com.example.taskmanager.manager.data.remote.dto.CreateTaskRequestDto
import com.example.taskmanager.manager.domain.repository.ManagerRepository
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class ManagerRepositoryImpl @Inject constructor(
    private val managerApiService: ManagerApiService,
    networkUtils: NetworkUtils,
    db : TaskManagerDatabase
): ManagerRepository {
    private val taskDao = db.taskDao
    private val hasNetwork = networkUtils.isNetworkAvailable()

    override suspend fun addTask(
        title: String,
        description: String,
        dueDate: String,
        priority: Int,
        status: Int,
        departmentId: UUID,
        employeeId: UUID,
        managerId: UUID
    ): Resource<Task> {
        try {
            if (!hasNetwork){
                return Resource.Error("No internet connection, please try again later")
            }
            val response = managerApiService.addTask(
                CreateTaskRequestDto(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    priority = priority,
                    status = status,
                    departmentId = departmentId,
                    employeeId = employeeId,
                    managerId = managerId
                )
            )
            when (response.statusCode){
                HttpStatusCodes.OK, HttpStatusCodes.CREATED -> {
                    val taskResponse = response.data
                    if (taskResponse != null){
                        val task = response.data.toTask()
                        response.data.toTaskEntity().let { taskDao.upsertTask(it) }
                        return Resource.Success(task)
                    } else {
                        return Resource.Error("No data found for adding task response")
                    }
                }
                HttpStatusCodes.BAD_REQUEST, HttpStatusCodes.UNAUTHORIZED, HttpStatusCodes.FORBIDDEN, HttpStatusCodes.NOT_FOUND, HttpStatusCodes.HTTP_CONFLICT -> {
                    return Resource.Error(getUserFriendlyMessage(statusCode = response.statusCode))
                }

                else -> { return Resource.Error("Something went wrong, please try again later") }
            }
        } catch (e: IOException){
            return Resource.Error(getIOExceptionMessage(e))
        } catch (e: Exception){
            return Resource.Error("Something went wrong, please try again later")
        }
    }

    override suspend fun updateTask(
        taskId: UUID,
        task: CreateTaskRequestDto
    ): Resource<String> {
        try {
            if (!hasNetwork){
                return Resource.Error("No internet connection, please try again later")
            }
            val response = managerApiService.updateTask(taskId, task)
            return when (response.statusCode){
                HttpStatusCodes.OK, HttpStatusCodes.CREATED -> {
                    if (response.data != null){
                        Resource.Success(response.data.toString())
                    } else {
                        Resource.Error("No data found for updating task response")
                    }
                }

                HttpStatusCodes.BAD_REQUEST, HttpStatusCodes.UNAUTHORIZED, HttpStatusCodes.FORBIDDEN, HttpStatusCodes.NOT_FOUND, HttpStatusCodes.HTTP_CONFLICT -> {
                    Resource.Error(getUserFriendlyMessage(statusCode = response.statusCode))
                }

                else -> {
                    Resource.Error("Something went wrong, please try again later")
                }
            }
        } catch (e: IOException){
            return Resource.Error(getIOExceptionMessage(e))
        } catch (e: Exception){
            return Resource.Error("Something went wrong, please try again later")
        }
    }

    override suspend fun deleteTask(taskId: UUID): Resource<String> {
        try {
            if (!hasNetwork){
                return Resource.Error("No internet connection, please try again later")
            }
            val response = managerApiService.deleteTask(taskId)
            return when (response.statusCode){
                HttpStatusCodes.OK, HttpStatusCodes.CREATED -> {
                     Resource.Success("Task deleted successfully")
                }

                HttpStatusCodes.BAD_REQUEST, HttpStatusCodes.UNAUTHORIZED, HttpStatusCodes.FORBIDDEN, HttpStatusCodes.NOT_FOUND, HttpStatusCodes.HTTP_CONFLICT -> {
                    Resource.Error(getUserFriendlyMessage(statusCode = response.statusCode))
                }

                else -> {
                    Resource.Error("Something went wrong, please try again later")
                }
            }
        } catch (e: IOException){
            return Resource.Error(getIOExceptionMessage(e))
        } catch (e: Exception){
            return Resource.Error("Something went wrong, please try again later")
        }
    }
}