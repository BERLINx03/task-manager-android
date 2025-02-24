package com.example.taskmanager.core.data.repository

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.local.database.TaskManagerDatabase
import com.example.taskmanager.core.data.mappers.toDepartment
import com.example.taskmanager.core.data.mappers.toDepartmentEntity
import com.example.taskmanager.core.data.mappers.toEmployeeEntity
import com.example.taskmanager.core.data.mappers.toManagerEntity
import com.example.taskmanager.core.data.mappers.toManagerAndEmployee
import com.example.taskmanager.core.data.mappers.toTask
import com.example.taskmanager.core.data.mappers.toTaskEntity
import com.example.taskmanager.core.data.remote.SharedApiService
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.PaginatedData
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.utils.HttpStatusCodes
import com.example.taskmanager.core.utils.NetworkUtils
import com.example.taskmanager.core.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class SharedRepositoryImpl @Inject constructor(
    private val sharedApiService: SharedApiService,
    private val taskManagerDatabase: TaskManagerDatabase,
    private val networkUtils: NetworkUtils
) : SharedRepository {

    private val managerDao = taskManagerDatabase.managerDao
    private val employeeDao = taskManagerDatabase.employeeDao
    private val departmentDao = taskManagerDatabase.departmentDao
    private val taskDao = taskManagerDatabase.taskDao

    override suspend fun getDepartmentById(departmentId: UUID): Resource<Department> {
        try {
            val cachedDepartment = departmentDao.getDepartmentById(departmentId)
            if (cachedDepartment != null) {
                return Resource.Success(cachedDepartment.toDepartment())
            } else if (!networkUtils.isNetworkAvailable()) {
                return Resource.Error("No internet connection. Try again later.")
            } else {
                val departmentResponse = sharedApiService.getDepartmentById(departmentId)
                if (departmentResponse.isSuccess && departmentResponse.data != null) {
                    val departmentEntity = departmentResponse.data.toDepartmentEntity()
                    departmentDao.upsertDepartment(departmentEntity)
                    return Resource.Success(departmentEntity.toDepartment())
                } else {
                    return Resource.Error(departmentResponse.message)
                }
            }
        } catch (e: IOException) {
            Timber.d("Network Failure: ${e.localizedMessage}")
            return Resource.Error("Connection error. Please check your network and try again")
        } catch (e: HttpException) {
            Timber.d("HTTP Exception: ${e.localizedMessage}")
            return Resource.Error("Server error occurred. Please try again later")
        } catch (e: Exception) {
            Timber.d("Unknown Exception: ${e.localizedMessage}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }

    override suspend fun getTasksManagedByManager(
        managerId: UUID,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<Task>>>> = flow {

        Timber.d("Fetching tasks managed by manager $managerId From Repo")

        emit(Resource.Loading(true))

        try {
            if (!forceFetchFromRemote) {
                val cachedTasksFlow = taskDao.getManagerTasks(managerId, page, limit, search, sort)
                emitAll(cachedTasksFlow.map { cachedTasks ->
                    Timber.d("Cached Tasks: $cachedTasks")
                    createSuccessResponseForManagersTasks(
                        task = cachedTasks.map { taskEntity -> taskEntity.toTask() },
                        page = page,
                        limit = limit,
                        totalCount = taskDao.getTasksCount(managerId, search),
                        message = "Loading from cache",
                        hasNextPage = (page * limit) < taskDao.getTasksCount(managerId, search),
                        hasPreviousPage = page > 1,
                        errors = null
                    )
                })
            }

            if (forceFetchFromRemote) {
                if (!networkUtils.isNetworkAvailable()) {
                    Timber.d("No internet connection. Using cached data.")
                    emit(Resource.Error("No internet connection. Using cached data."))
                    return@flow
                }

                val tasksResponse =
                    sharedApiService.getTasksManagedByManager(managerId, page, limit, search, sort)

                if (tasksResponse.isSuccess && tasksResponse.data != null) {
                    val taskEntities = tasksResponse.data.items.map { task ->
                        task.toTaskEntity()
                    }
                    taskDao.upsertTasks(taskEntities)
                } else {
                    emit(Resource.Error(tasksResponse.message))
                }
                emit(
                    createSuccessResponseForManagersTasks(
                        task = tasksResponse.data?.items?.map { task ->
                            task.toTask()
                        } ?: emptyList(),
                        page = page,
                        limit = limit,
                        totalCount = tasksResponse.data?.totalCount ?: 0,
                        message = "Loading from network",
                        hasNextPage = tasksResponse.data?.hasNextPage ?: false,
                        hasPreviousPage = tasksResponse.data?.hasPreviousPage ?: false,
                        errors = tasksResponse.errors,
                    )
                )
                Timber.d("Tasks Response: $tasksResponse")
            }
        } catch (e: IOException) {
            Timber.d("Network Failure: ${e.localizedMessage}")
            emit(Resource.Error("Connection error. Please check your network and try again"))
        } catch (e: HttpException) {
            Timber.d("HTTP Exception: ${e.localizedMessage}")
            emit(Resource.Error("Server error occurred. Please try again later"))
        } catch (e: Exception) {
            Timber.d("Unknown Exception: ${e.localizedMessage}")
            emit(Resource.Error("Something went wrong. Try again later"))
        } finally {
            emit(Resource.Loading(false))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getPagedManagers(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<ManagerAndEmployee>>>> {
        return flow {

            emit(Resource.Loading(true))

            try {
                if (!forceFetchFromRemote) {
                    val cachedManagersFlow = managerDao.getPagedManagers(page, limit, search, sort)
                    emitAll(cachedManagersFlow.map { cachedManagers ->
                        Timber.d("Cached Managers: $cachedManagers")
                        createSuccessResponseForManagersAndEmployees(
                            managers = cachedManagers.map { managerEntity ->
                                managerEntity.toManagerAndEmployee()
                            },
                            page = page,
                            limit = limit,
                            totalCount = managerDao.getTotalCount(search),
                            message = "Loading from cache",
                            hasNextPage = (page * limit) < managerDao.getTotalCount(search),
                            hasPreviousPage = page > 1,
                            errors = null
                        )
                    })
                }

                if (forceFetchFromRemote) {
                    if (!networkUtils.isNetworkAvailable()) {
                        emit(Resource.Error("No internet connection. Try again later."))
                        return@flow
                    }

                    val managerResponse = sharedApiService.getManagers(page, limit, search, sort)
                    if (managerResponse.isSuccess && managerResponse.data != null) {
                        val managerEntities = managerResponse.data.items.map { managers ->
                            managers.toManagerEntity()
                        }
                        managerDao.upsertManagers(managerEntities)
                    }
                    emit(
                        createSuccessResponseForManagersAndEmployees(
                            managers = managerResponse.data?.items?.map { managers ->
                                managers.toManagerAndEmployee()
                            } ?: emptyList(),
                            page = page,
                            limit = limit,
                            totalCount = managerResponse.data?.totalCount ?: 0,
                            message = "Loading from network",
                            hasNextPage = managerResponse.data?.hasNextPage ?: false,
                            hasPreviousPage = managerResponse.data?.hasPreviousPage ?: false,
                            errors = managerResponse.errors,
                        )
                    )
                }
            } catch (e: IOException) {
                Timber.d("Network Failure: ${e.localizedMessage}")
                emit(Resource.Error("Connection error. Please check your network and try again"))
            } catch (e: HttpException) {
                Timber.d("HTTP Exception: ${e.localizedMessage}")
                emit(Resource.Error("Server error occurred. Please try again later"))
            } catch (e: Exception) {
                Timber.d("Unknown Exception: ${e.localizedMessage}")
                emit(Resource.Error("Something went wrong. Try again later"))
            } finally {
                emit(Resource.Loading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getManagerById(managerId: UUID): Resource<ManagerAndEmployee> {
        try {
            val cachedManager = managerDao.getManagerById(managerId)
            if (cachedManager != null) {
                return Resource.Success(cachedManager.toManagerAndEmployee())
            } else if (!networkUtils.isNetworkAvailable()) {
                return Resource.Error("No internet connection. Try again later.")
            } else {
                val managerResponse = sharedApiService.getManagerById(managerId)
                if (managerResponse.isSuccess && managerResponse.data != null) {
                    val managerEntity = managerResponse.data.toManagerEntity()
                    managerDao.upsertManager(managerEntity)
                    return Resource.Success(managerEntity.toManagerAndEmployee())
                } else {
                    return Resource.Error(managerResponse.message)
                }
            }
        } catch (e: IOException) {
            Timber.d("Network Failure: ${e.localizedMessage}")
            return Resource.Error("Connection error. Please check your network and try again")
        } catch (e: HttpException) {
            Timber.d("HTTP Exception: ${e.localizedMessage}")
            return Resource.Error("Server error occurred. Please try again later")
        } catch (e: Exception) {
            Timber.d("Unknown Exception: ${e.localizedMessage}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }

    override suspend fun getEmployeeById(employeeId: UUID): Resource<ManagerAndEmployee> {
        try {
            val cachedEmployee = employeeDao.getEmployeeById(employeeId)
            if (cachedEmployee != null) {
                return Resource.Success(cachedEmployee.toManagerAndEmployee())
            } else if (!networkUtils.isNetworkAvailable()) {
                return Resource.Error("No internet connection. Try again later.")
            } else {
                val employeeResponse = sharedApiService.getEmployeeById(employeeId)
                if (employeeResponse.isSuccess && employeeResponse.data != null) {
                    val employeeEntity = employeeResponse.data.toEmployeeEntity()
                    employeeDao.upsertEmployee(employeeEntity)
                    return Resource.Success(employeeEntity.toManagerAndEmployee())
                } else {
                    return Resource.Error(employeeResponse.message)
                }
            }
        } catch (e: IOException) {
            Timber.d("Network Failure: ${e.localizedMessage}")
            return Resource.Error("Connection error. Please check your network and try again")
        } catch (e: HttpException) {
            Timber.d("HTTP Exception: ${e.localizedMessage}")
            return Resource.Error("Server error occurred. Please try again later")
        } catch (e: Exception) {
            Timber.d("Unknown Exception: ${e.localizedMessage}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }


    private fun createSuccessResponseForManagersTasks(
        task: List<Task>,
        page: Int,
        limit: Int,
        totalCount: Int,
        message: String,
        hasNextPage: Boolean = (page * limit) < totalCount,
        hasPreviousPage: Boolean = page > 1,
        errors: List<String>? = null
    ): Resource<ResponseDto<PaginatedData<Task>>> {
        return Resource.Success(
            ResponseDto(
                isSuccess = true,
                statusCode = HttpStatusCodes.OK,
                message = message,
                data = PaginatedData(
                    items = task,
                    page = page,
                    pageSize = limit,
                    totalCount = totalCount,
                    hasNextPage = hasNextPage,
                    hasPreviousPage = hasPreviousPage
                ),
                errors = errors
            )
        )
    }

    private fun createSuccessResponseForManagersAndEmployees(
        managers: List<ManagerAndEmployee>,
        page: Int,
        limit: Int,
        totalCount: Int,
        message: String,
        hasNextPage: Boolean = (page * limit) < totalCount,
        hasPreviousPage: Boolean = page > 1,
        errors: List<String>? = null
    ): Resource<ResponseDto<PaginatedData<ManagerAndEmployee>>> {
        return Resource.Success(
            ResponseDto(
                isSuccess = true,
                statusCode = HttpStatusCodes.OK,
                message = message,
                data = PaginatedData(
                    items = managers,
                    page = page,
                    pageSize = limit,
                    totalCount = totalCount,
                    hasNextPage = hasNextPage,
                    hasPreviousPage = hasPreviousPage
                ),
                errors = errors
            )
        )
    }
}