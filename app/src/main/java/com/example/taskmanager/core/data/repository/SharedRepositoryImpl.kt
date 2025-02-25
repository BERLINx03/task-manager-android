package com.example.taskmanager.core.data.repository

import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.local.database.TaskManagerDatabase
import com.example.taskmanager.core.data.mappers.toDepartment
import com.example.taskmanager.core.data.mappers.toDepartmentEntity
import com.example.taskmanager.core.data.mappers.toEmployeeEntity
import com.example.taskmanager.core.data.mappers.toManagerAndEmployee
import com.example.taskmanager.core.data.mappers.toManagerEntity
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
import com.example.taskmanager.core.utils.getIOExceptionMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    taskManagerDatabase: TaskManagerDatabase,
    private val networkUtils: NetworkUtils
) : SharedRepository {

    private val hasNetwork = networkUtils.isNetworkAvailable()
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
                    createSuccessResponseForManagersAndEmployeesTasks(
                        task = cachedTasks.map { taskEntity -> taskEntity.toTask() },
                        page = page,
                        limit = limit,
                        totalCount = taskDao.getManagersTasksCount(managerId, search),
                        message = "Loading from cache",
                        hasNextPage = (page * limit) < taskDao.getManagersTasksCount(managerId, search),
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
                    createSuccessResponseForManagersAndEmployeesTasks(
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
                            managerAndEmployees = cachedManagers.map { managerEntity ->
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
                            managerAndEmployees = managerResponse.data?.items?.map { managers ->
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

    override suspend fun getManagersInDepartment(
        departmentId: UUID,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<PaginatedData<ManagerAndEmployee>>> {
        return flow {
            emit(Resource.Loading(true))
            if (hasNetwork) {
                try {
                    val response = sharedApiService.getManagersInDepartment(departmentId, page, limit, search, sort)
                    if (!response.isSuccess) {
                        emit(Resource.Error("Failed to fetch data from server: ${response.message}"))
                    } else if (response.data != null) {
                        val managers = response.data.items.map { it.toManagerAndEmployee() }
                        managerDao.upsertManagers(managers.map { it.toManagerEntity() })

                        emit(
                            Resource.Success(
                                PaginatedData(
                                    items = managers,
                                    page = page,
                                    pageSize = limit,
                                    totalCount = response.data.totalCount,
                                    hasNextPage = response.data.hasNextPage,
                                    hasPreviousPage = response.data.hasPreviousPage
                                )
                            )
                        )
                        Timber.d("Pagination debug: page=$page, limit=$limit, count=${response.data.totalCount}, hasNext=${response.data.hasNextPage}, hasPrev=${response.data.hasPreviousPage}")
                        return@flow
                    }
                } catch (e: IOException) {
                    emit(Resource.Error(getIOExceptionMessage(e)))
                } catch (e: Exception) {
                    emit(Resource.Error("Something went wrong. Try again later"))
                }
            } else {
                if (forceFetchFromRemote){
                    emit(Resource.Error("No internet connection. Try again later."))
                    return@flow
                }
                managerDao.getPagedManagersByDepartment(departmentId, search, page, limit)
                    .combine(managerDao.countManagersByDepartment(departmentId)) { managers, count ->
                        Pair(managers, count)
                    }.collect { (managers, count) ->
                        emit(
                            Resource.Success(
                                data = PaginatedData(
                                    items = managers.map{ manager -> manager.toManagerAndEmployee() },
                                    page = page,
                                    pageSize = limit,
                                    totalCount = count,
                                    hasNextPage = (page * limit) < count,
                                    hasPreviousPage = page > 1
                                )
                            )
                        )
                        emit(Resource.Loading(false))
                        Timber.d("Pagination debug: page=$page, limit=$limit, count=$count, hasNext=${(page * limit) < count}, hasPrev=${page > 1}")
                    }
            }

        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getEmployeesInDepartment(
        departmentId: UUID,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<PaginatedData<ManagerAndEmployee>>> {
        return flow {
            emit(Resource.Loading(true))
            if (hasNetwork) {
                try {
                    val response = sharedApiService.getEmployeesInDepartment(departmentId, page, limit, search, sort)
                    if (!response.isSuccess) {
                        emit(Resource.Error("Failed to fetch data from server: ${response.message}"))
                    } else if (response.data != null) {
                        val employees = response.data.items.map { it.toManagerAndEmployee() }
                        employeeDao.upsertEmployees(employees.map { it.toEmployeeEntity() })

                        emit(
                            Resource.Success(
                                PaginatedData(
                                    items = employees,
                                    page = page,
                                    pageSize = limit,
                                    totalCount = response.data.totalCount,
                                    hasNextPage = response.data.hasNextPage,
                                    hasPreviousPage = response.data.hasPreviousPage
                                )
                            )
                        )
                        Timber.d("Pagination debug: page=$page, limit=$limit, count=${response.data.totalCount}, hasNext=${response.data.hasNextPage}, hasPrev=${response.data.hasPreviousPage}")
                        return@flow
                    }
                } catch (e: IOException) {
                    emit(Resource.Error(getIOExceptionMessage(e)))
                } catch (e: Exception) {
                    emit(Resource.Error("Something went wrong. Try again later"))
                }
            } else {
                if (forceFetchFromRemote){
                    emit(Resource.Error("No internet connection. Try again later."))
                    return@flow
                }
                employeeDao.getPagedEmployeesByDepartment(departmentId,search, page, limit)
                    .combine(employeeDao.countEmployeesByDepartment(departmentId)) { employeeEntities, count ->
                        Pair(employeeEntities, count)
                    }.collect { (managers, count) ->
                        emit(
                            Resource.Success(
                                data = PaginatedData(
                                    items = managers.map{ employeeEntity -> employeeEntity.toManagerAndEmployee() },
                                    page = page,
                                    pageSize = limit,
                                    totalCount = count,
                                    hasNextPage = (page * limit) < count,
                                    hasPreviousPage = page > 1
                                )
                            )
                        )
                        emit(Resource.Loading(false))
                        Timber.d("Pagination debug: page=$page, limit=$limit, count=$count, hasNext=${(page * limit) < count}, hasPrev=${page > 1}")
                    }
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

    override suspend fun getTasksAssignedToEmployee(
        employeeId: UUID,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<Task>>>> {
        return flow {

            Timber.d("Fetching tasks managed by manager $employeeId From Repo")

            emit(Resource.Loading(true))

            try {
                if (!forceFetchFromRemote) {
                    val cachedTasksFlow = taskDao.getEmployeeTasks(employeeId, page, limit, search, sort)
                    emitAll(cachedTasksFlow.map { cachedTasks ->
                        Timber.d("Cached Tasks: $cachedTasks")
                        createSuccessResponseForManagersAndEmployeesTasks(
                            task = cachedTasks.map { taskEntity -> taskEntity.toTask() },
                            page = page,
                            limit = limit,
                            totalCount = taskDao.getEmployeesTasksCount(employeeId, search),
                            message = "Loading from cache",
                            hasNextPage = (page * limit) < taskDao.getEmployeesTasksCount(employeeId, search),
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
                        sharedApiService.getTasksAssignedToEmployee(employeeId, page, limit, search, sort)

                    if (tasksResponse.isSuccess && tasksResponse.data != null) {
                        val taskEntities = tasksResponse.data.items.map { task ->
                            task.toTaskEntity()
                        }
                        taskDao.upsertTasks(taskEntities)
                    } else {
                        emit(Resource.Error(tasksResponse.message))
                    }
                    emit(
                        createSuccessResponseForManagersAndEmployeesTasks(
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
    }

    override suspend fun getPagedEmployees(
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
                    val cachedEmployeesFlow = employeeDao.getPagedEmployees(page, limit, search, sort)
                    emitAll(cachedEmployeesFlow.map { cachedEmployees ->
                        Timber.d("Cached Employees: $cachedEmployees")
                        createSuccessResponseForManagersAndEmployees(
                            managerAndEmployees = cachedEmployees.map { employeeEntity ->
                                employeeEntity.toManagerAndEmployee()
                            },
                            page = page,
                            limit = limit,
                            totalCount = employeeDao.getTotalCount(search),
                            message = "Loading from cache",
                            hasNextPage = (page * limit) < employeeDao.getTotalCount(search),
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

                    val employeeResponse = sharedApiService.getEmployees(page, limit, search, sort)
                    if (employeeResponse.isSuccess && employeeResponse.data != null) {
                        val employeeEntities = employeeResponse.data.items.map { employees ->
                            employees.toEmployeeEntity()
                        }
                        employeeDao.upsertEmployees(employeeEntities)
                    }
                    emit(
                        createSuccessResponseForManagersAndEmployees(
                            managerAndEmployees = employeeResponse.data?.items?.map { employees ->
                                employees.toManagerAndEmployee()
                            } ?: emptyList(),
                            page = page,
                            limit = limit,
                            totalCount = employeeResponse.data?.totalCount ?: 0,
                            message = "Loading from network",
                            hasNextPage = employeeResponse.data?.hasNextPage ?: false,
                            hasPreviousPage = employeeResponse.data?.hasPreviousPage ?: false,
                            errors = employeeResponse.errors,
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

    override suspend fun getTaskById(taskId: UUID): Resource<Task> {
        try {
            val cachedTask = taskDao.getTaskById(taskId)
            if (cachedTask != null) {
                return Resource.Success(cachedTask.toTask())
            } else if (!networkUtils.isNetworkAvailable()) {
                return Resource.Error("No internet connection. Try again later.")
            } else {
                val taskResponse = sharedApiService.getTaskById(taskId)
                if (taskResponse.isSuccess && taskResponse.data != null) {
                    val taskEntity = taskResponse.data.toTaskEntity()
                    taskDao.upsertTask(taskEntity)
                    return Resource.Success(taskEntity.toTask())
                } else {
                    return Resource.Error(taskResponse.message)
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


    private fun createSuccessResponseForManagersAndEmployeesTasks(
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
        managerAndEmployees: List<ManagerAndEmployee>,
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
                    items = managerAndEmployees,
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