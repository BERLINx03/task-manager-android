package com.example.taskmanager.admin.data.repository

import com.example.taskmanager.admin.data.mapper.AdminMapper
import com.example.taskmanager.admin.data.mapper.toDomain
import com.example.taskmanager.admin.data.mapper.toEntity
import com.example.taskmanager.admin.data.remote.AdminServiceApi
import com.example.taskmanager.admin.data.remote.dto.UpdateAdminRequestDto
import com.example.taskmanager.admin.domain.model.Admin
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.PaginatedData
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.local.database.TaskManagerDatabase
import com.example.taskmanager.core.data.local.datastore.StatisticsDataStore
import com.example.taskmanager.core.data.local.entities.DepartmentEntity
import com.example.taskmanager.core.data.mappers.toDepartment
import com.example.taskmanager.core.data.mappers.toDepartmentEntity
import com.example.taskmanager.core.data.mappers.toTask
import com.example.taskmanager.core.data.mappers.toTaskEntity
import com.example.taskmanager.core.data.remote.SharedApiService
import com.example.taskmanager.core.data.remote.dto.DepartmentRequestDto
import com.example.taskmanager.core.utils.HttpStatusCodes.BAD_REQUEST
import com.example.taskmanager.core.utils.HttpStatusCodes.CREATED
import com.example.taskmanager.core.utils.HttpStatusCodes.FORBIDDEN
import com.example.taskmanager.core.utils.HttpStatusCodes.HTTP_CONFLICT
import com.example.taskmanager.core.utils.HttpStatusCodes.NOT_FOUND
import com.example.taskmanager.core.utils.HttpStatusCodes.OK
import com.example.taskmanager.core.utils.HttpStatusCodes.UNAUTHORIZED
import com.example.taskmanager.core.utils.NetworkUtils
import com.example.taskmanager.core.utils.Resource
import com.example.taskmanager.core.utils.getIOExceptionMessage
import com.example.taskmanager.core.utils.getUserFriendlyMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.UUID
import javax.inject.Inject


/**
 * @author Abdallah Elsokkary
 */
class AdminRepositoryImpl @Inject constructor(
    private val adminServiceApi: AdminServiceApi,
    private val sharedApiService: SharedApiService,
    private val mapper: AdminMapper,
    private val networkUtils: NetworkUtils,
    private val statisticsDataStore: StatisticsDataStore,
    private val database: TaskManagerDatabase
) : AdminRepository {

    private val managerDao = database.managerDao
    private val employeeDao = database.employeeDao
    private val departmentDao = database.departmentDao
    private val taskDao = database.taskDao
    private val adminDao = database.adminDao
    private val hasNetwork = networkUtils.isNetworkAvailable()


    override suspend fun getCurrentAdmin(): Resource<Admin> {
        try {
            val cachedAdmin = adminDao.getCurrentAdmin(current = true)
            if (cachedAdmin != null) {
                Timber.d("Returning admin from cache")
                return Resource.Success(data = cachedAdmin.toDomain())
            }
            Timber.d("No cached admin found, fetching from network")

            if (!networkUtils.isNetworkAvailable()) {
                Timber.w("Network is not available")
                return Resource.Error("No internet connection. Try again later.")
            }

            val response = adminServiceApi.getCurrentAdmin()
            when (response.statusCode) {
                OK, CREATED -> {
                    Timber.i("Admin fetched successfully from network")

                    val admin = response.data?.let { mapper.mapToDomain(it) }
                    if (admin != null) {
                        adminDao.resetAndUpsertAdmin(admin.toEntity())

                        return Resource.Success(data = admin)

                    } else {
                        Timber.w("Network response successful but no admin data available")
                        return Resource.Error("No admin data available")
                    }
                }

                BAD_REQUEST -> {
                    Timber.e("Bad request error: ${response.message}")
                    return Resource.Error("Bad Request: ${response.message}")
                }

                UNAUTHORIZED -> {
                    Timber.e("Unauthorized access: ${response.message}")
                    return Resource.Error("Unauthorized: ${response.message}")
                }

                FORBIDDEN -> {
                    Timber.e("Forbidden access: ${response.message}")
                    return Resource.Error("Forbidden: ${response.message}")
                }

                NOT_FOUND -> {
                    Timber.e("Resource not found: ${response.message}")
                    return Resource.Error("Not Found: ${response.message}")
                }

                HTTP_CONFLICT -> {
                    Timber.e("Conflict error: ${response.message}")
                    return Resource.Error("Conflict: ${response.message}")
                }

                else -> {
                    Timber.e("Unknown error status code: ${response.statusCode} - ${response.message}")
                    return Resource.Error("Error: ${response.message}")
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Network failure occurred")
            return Resource.Error("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Timber.e(e, "HTTP exception occurred: ${e.code()}")
            return Resource.Error("Http Error: ${e.localizedMessage}")
        } catch (e: Exception) {
            Timber.e(e, "Unknown exception occurred")
            return Resource.Error("Unknown Error: ${e.localizedMessage}")
        }
    }


    override suspend fun getAdminById(adminId: UUID): Resource<ResponseDto<Admin>> {
        return try {
            val cachedAdmin = adminDao.getAdminById(adminId)

            if (cachedAdmin != null) {
                Timber.i("Returning admin from cache")
                Resource.Success(
                    ResponseDto(
                        isSuccess = true,
                        statusCode = OK,
                        message = "Success from cached data",
                        data = cachedAdmin.toDomain(),
                        errors = null
                    )
                )
            }

            Timber.i("No cached admin found, fetching from network")
            if (!networkUtils.isNetworkAvailable()) {
                Timber.w("Network is not available")
                return Resource.Error("No internet connection. Try again later.")
            }

            Timber.i("Fetching admin from network")
            val response = adminServiceApi.getAdminById(adminId)
            when (response.statusCode) {
                OK, CREATED -> {
                    Timber.i("Admin fetched successfully from network")
                    val admin = response.data?.let { mapper.mapToDomain(it) }
                    if (admin != null) {
                        adminDao.upsertAdmin(admin.toEntity())
                        Resource.Success(
                            ResponseDto(
                                isSuccess = true,
                                statusCode = OK,
                                message = "Success from network",
                                data = admin,
                                errors = null
                            )
                        )
                    } else {
                        Timber.w("Network response successful but no admin data available")
                        return Resource.Error("No admin data available")
                    }
                }

                BAD_REQUEST -> {
                    Timber.e("Bad request error: ${response.message}")
                    return Resource.Error("Bad Request: ${response.message}")
                }

                UNAUTHORIZED -> {
                    Timber.e("Unauthorized access: ${response.message}")
                    return Resource.Error("Unauthorized: ${response.message}")
                }

                FORBIDDEN -> {
                    Timber.e("Forbidden access: ${response.message}")
                    return Resource.Error("Forbidden: ${response.message}")
                }

                NOT_FOUND -> {
                    Timber.e("Resource not found: ${response.message}")
                    return Resource.Error("Not Found: ${response.message}")
                }

                HTTP_CONFLICT -> {
                    Timber.e("Conflict error: ${response.message}")
                    return Resource.Error("Conflict: ${response.message}")
                }

                else -> {
                    Timber.e("Unknown error status code: ${response.statusCode} - ${response.message}")
                    return Resource.Error("Error: ${response.message}")
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Network failure occurred")
            return Resource.Error("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Timber.e(e, "HTTP exception occurred: ${e.code()}")
            return Resource.Error("Http Error: ${e.localizedMessage}")
        } catch (e: Exception) {
            Timber.e(e, "Unknown exception occurred")
            return Resource.Error("Unknown Error: ${e.localizedMessage}")
        }
    }


    override suspend fun getAdmins(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean,
        isRefreshing: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<Admin>>>> {
        return flow {
            emit(Resource.Loading(true))

            try {

                if (forceFetchFromRemote || isRefreshing) {
                    if (!networkUtils.isNetworkAvailable()) {
                        emit(Resource.Error("No internet connection. Using cached data."))
                    } else {
                        try {
                            if (isRefreshing) {
                                adminDao.deleteAllAdmins()
                            }

                            val response = adminServiceApi.getAdmins(page, limit, search, sort)

                            if (!response.isSuccess) {
                                emit(Resource.Error("Failed to fetch data from server: ${response.message}"))
                            } else if (response.data != null) {
                                val remoteData = response.data.items.map {
                                    mapper.mapToDomain(it).toEntity()
                                }
                                adminDao.upsertAdmins(remoteData)
                            }
                        } catch (e: IOException) {
                            emit(Resource.Error("Network error: ${e.message}. Using cached data."))
                        } catch (e: Exception) {
                            emit(Resource.Error("Unexpected error: ${e.message}"))
                        }
                    }
                }

                adminDao.getPagedAdmins(page, limit, search, sort)
                    .combine(adminDao.getAdminsCountFlow(search)) { admins, count ->
                        Pair(admins, count)
                    }.collect { (admins, count) ->
                        emit(
                            Resource.Success(
                                ResponseDto(
                                    isSuccess = true,
                                    statusCode = OK,
                                    message = if (forceFetchFromRemote || isRefreshing)
                                        "Updated from network" else "Loading from cache",
                                    data = PaginatedData(
                                        items = admins.map { it.toDomain() },
                                        page = page,
                                        pageSize = limit,
                                        totalCount = count,
                                        hasNextPage = (page * limit) < count,
                                        hasPreviousPage = page > 1
                                    ),
                                    errors = null
                                )
                            )
                        )
                    }
            } catch (e: IOException) {
                Timber.e(e, "Database error and network error")
                emit(Resource.Error("Database error: ${e.message}"))
            } catch (e: Exception) {
                emit(Resource.Error("Unexpected error: ${e.message}"))
            } finally {
                emit(Resource.Loading(false))
            }
        }.flowOn(Dispatchers.IO)
    }

    // Done no need to review later
    override suspend fun updateAdmin(admin: UpdateAdminRequestDto): Resource<UUID> {
        if (!hasNetwork) {
            return Resource.Error("No internet connection. Try again later.")
        }
        try {
            val adminResponse = adminServiceApi.updateAdmin(admin)
            return when (adminResponse.statusCode) {
                OK, CREATED -> {
                    if (adminResponse.data != null) {
                        val adminEntity = adminDao.getAdminById(adminResponse.data)
                        if (adminEntity != null) {
                            adminDao.upsertAdmin(adminEntity.copy(lastSyncTimestamp = System.currentTimeMillis()))
                        } else {
                            Timber.w("Admin not found in local database: ${adminResponse.data}")
                        }
                        Resource.Success(adminResponse.data)
                    } else {
                        Timber.e("Response successful but data is null")
                        Resource.Error("Server returned success but no data")
                    }
                }

                BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, HTTP_CONFLICT ->
                    Resource.Error(getUserFriendlyMessage(adminResponse.statusCode))

                else -> Resource.Error(getUserFriendlyMessage(adminResponse.statusCode))
            }
        } catch (e: IOException) {
            Timber.e(e, "Network failure occurred")
            return Resource.Error("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Timber.e(e, "HTTP exception occurred: ${e.code()}")
            return Resource.Error("Http Error: ${e.localizedMessage}")
        } catch (e: Exception) {
            Timber.e(e, "Unknown exception occurred")
            return Resource.Error("Unknown Error: ${e.localizedMessage}")
        }
    }

    // Done no need to review later
    override suspend fun addDepartment(title: DepartmentRequestDto): Resource<Department> {
        if (!networkUtils.isNetworkAvailable())
            return Resource.Error("No internet connection. Try again later.")

        try {
            val response = adminServiceApi.addDepartment(title)
            val departmentEntity =
                response.data?.let { DepartmentEntity(title = title.title, id = it) }
            when (response.statusCode) {
                OK, CREATED -> {
                    if (departmentEntity != null) {
                        Timber.tag("AdminRepositoryImpl").d(getUserFriendlyMessage(OK))
                        departmentDao.upsertDepartment(departmentEntity)
                        return Resource.Success(departmentEntity.toDepartment())
                    } else {
                        Timber.tag("AdminRepositoryImpl").d(getUserFriendlyMessage(OK))
                        return Resource.Error("No department data available")
                    }
                }

                BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, HTTP_CONFLICT -> {
                    return Resource.Error(getUserFriendlyMessage(statusCode = response.statusCode))
                }

                else -> {
                    Timber.e("Unknown error status code: ${response.statusCode} - ${response.message}")
                    return Resource.Error(getUserFriendlyMessage(statusCode = response.statusCode))
                }
            }
        } catch (e: IOException) {
            Timber.e(e, "Network failure occurred")
            return Resource.Error(getIOExceptionMessage(e))
        } catch (e: HttpException) {
            Timber.e(e, "HTTP exception occurred: ${e.code()}")
            return Resource.Error(getUserFriendlyMessage(e.code()))
        } catch (e: Exception) {
            Timber.e(e, "Unknown exception occurred")
            return Resource.Error("Something went wrong. Try again later")
        }
    }

    override suspend fun getCachedAdminsCount(): Resource<Int> {
        try {
            val count = statisticsDataStore.adminCountFlow.first()
            return Resource.Success(count)
        } catch (e: Exception) {
            return Resource.Error("Failed to get admin count: ${e.message}")
        }
    }

    override suspend fun getAdminsCountFromNetwork(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading(true))

        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection. Try again later."))
            return@flow
        }

        try {
            val adminsCountResponse = adminServiceApi.getAdmins(1, 1).data?.totalCount

            when {
                adminsCountResponse == null ->
                    emit(Resource.Error("Failed to get admin count"))

                else -> {
                    statisticsDataStore.saveAdminCount(adminsCountResponse)
                    emit(Resource.Success(adminsCountResponse))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    override suspend fun getCachedDepartmentsCount(): Resource<Int> {
        try {
            val count = statisticsDataStore.departmentCountFlow.first()
            Timber.d("Cached department count: $count")
            return Resource.Success(count)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get department count: ${e.message}")
            return Resource.Error("Failed to get department count: ${e.message}")
        }
    }

    override suspend fun getDepartmentsCountFromNetwork(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading(true))

        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection. Try again later."))
            return@flow
        }

        try {
            val departmentsCountResponse = sharedApiService.getDepartments(1, 1).data?.totalCount

            when {
                departmentsCountResponse == null ->
                    emit(Resource.Error("Failed to get departments count"))

                else -> {
                    statisticsDataStore.saveDepartmentCount(departmentsCountResponse)
                    emit(Resource.Success(departmentsCountResponse))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }

    override suspend fun getCachedTasksCount(): Resource<Int> {
        try {
            val count = statisticsDataStore.taskCountFlow.first()
            return Resource.Success(count)
        } catch (e: Exception) {
            return Resource.Error("Failed to get task count: ${e.message}")
        }
    }

    override suspend fun getTasksCountFromNetwork(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading(true))

        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection. Try again later."))
            return@flow
        }

        try {
            val tasksCountResponse = adminServiceApi.getTasks(1, 1).data?.totalCount
            Timber.d("Tasks total count in try block: $tasksCountResponse")
            when {
                tasksCountResponse == null -> {
                    Timber.d("Tasks total count in null block: $tasksCountResponse")
                    emit(Resource.Error("Failed to get task count"))
                }

                else -> {
                    Timber.d("Tasks total count in else block: $tasksCountResponse")
                    statisticsDataStore.saveTaskCount(tasksCountResponse)
                    emit(Resource.Success(tasksCountResponse))
                }
            }
        } catch (e: IOException) {
            Timber.d("Tasks total count in catch block: ${e.message}")
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: HttpException) {
            Timber.d("Tasks total count in catch block: ${e.message}")
            emit(Resource.Error("HTTP error: ${e.message}"))
        } catch (e: Exception) {
            Timber.d("Tasks total count in catch block: ${e.message}")
            emit(Resource.Error("Unknown error: ${e.message}"))
        } finally {
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getCachedManagersCount(): Resource<Int> {
        try {
            val count = statisticsDataStore.managerCountFlow.first()
            Timber.d("Cached manager count: $count")
            return Resource.Success(count)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get manager count: ${e.message}")
            return Resource.Error("Failed to get manager count: ${e.message}")
        }
    }

    override suspend fun getManagersCountFromNetwork(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading(true))

        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection. Try again later."))
            return@flow
        }

        try {
            val managersTotalCount = sharedApiService.getManagers(1, 1).data?.totalCount
            Timber.d("$managersTotalCount manager has been fetched")
            when {
                managersTotalCount == null ->
                    emit(Resource.Error("Failed to get managers count"))

                else -> {
                    statisticsDataStore.saveManagerCount(managersTotalCount)
                    Timber.d("Managers total count: $managersTotalCount")
                    emit(Resource.Success(managersTotalCount))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        } finally {
            emit(Resource.Loading(false))
        }
    }

    override suspend fun getCachedEmployeesCount(): Resource<Int> {
        try {
            val count = statisticsDataStore.employeeCountFlow.first()
            Timber.d("Cached employee count: $count")
            return Resource.Success(count)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get employee count: ${e.message}")
            return Resource.Error("Failed to get employee count: ${e.message}")
        }
    }

    override suspend fun getEmployeesCountFromNetwork(): Flow<Resource<Int>> = flow {
        emit(Resource.Loading(true))

        if (!networkUtils.isNetworkAvailable()) {
            emit(Resource.Error("No internet connection. Try again later."))
            return@flow
        }

        try {
            val employeeCount = sharedApiService.getEmployees(1, 1).data?.totalCount

            when {
                employeeCount == null ->
                    emit(Resource.Error("Failed to get employee count"))

                else -> {
                    statisticsDataStore.saveEmployeeCount(employeeCount)
                    emit(Resource.Success(employeeCount))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: HttpException) {
            emit(Resource.Error("HTTP error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }


    override suspend fun getDepartments(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean,
        isRefreshing: Boolean
    ): Flow<Resource<PaginatedData<Department>>> {
        return flow {
            emit(Resource.Loading(true))
            if (hasNetwork) {
                try {
                    val response = sharedApiService.getDepartments(page, limit, search, sort)
                    if (!response.isSuccess) {
                        emit(Resource.Error("Failed to fetch data from server: ${response.message}"))
                    } else if (response.data != null) {
                        val departments = response.data.items.map { it.toDepartment() }
                        departmentDao.upsertDepartments(departments.map { it.toDepartmentEntity() })

                        emit(
                            Resource.Success(
                                PaginatedData(
                                    items = departments,
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
                departmentDao.getPagedDepartments(page, limit, search, sort)
                    .combine(departmentDao.getDepartmentsCountFlow(search)) { departments, count ->
                        Pair(departments, count)
                    }.collect { (departments, count) ->
                        emit(
                            Resource.Success(
                                data = PaginatedData(
                                    items = departments.map { it.toDepartment() },
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


    override suspend fun deleteManager(managerId: UUID): Resource<ResponseDto<String>> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection. Try again later.")
        }
        try {
            val response = adminServiceApi.deleteManager(managerId)
            return if (response.isSuccess) {
                if (managerDao.getManagerById(managerId) != null) {
                    managerDao.deleteManagerById(managerId)
                }
                Resource.Success(
                    ResponseDto(
                        isSuccess = response.isSuccess,
                        statusCode = OK,
                        message = response.message,
                        data = response.data,
                        errors = response.errors
                    )
                )
            } else {
                Resource.Error(response.message)
            }
        } catch (e: IOException) {
            Timber.e("Network error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        } catch (e: Exception) {
            Timber.e("Unknown error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }

    override suspend fun deleteEmployee(employeeId: UUID): Resource<ResponseDto<String>> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection. Try again later.")
        }
        try {
            val response = adminServiceApi.deleteEmployee(employeeId)
            return if (response.isSuccess) {
                if (employeeDao.getEmployeeById(employeeId) != null) {
                    employeeDao.deleteEmployeeById(employeeId)
                }
                Resource.Success(
                    ResponseDto(
                        isSuccess = response.isSuccess,
                        statusCode = OK,
                        message = response.message,
                        data = response.data,
                        errors = response.errors
                    )
                )
            } else {
                Resource.Error(response.message)
            }
        } catch (e: IOException) {
            Timber.e("Network error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        } catch (e: Exception) {
            Timber.e("Unknown error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }


    override suspend fun updateDepartment(
        departmentId: UUID,
        updateDepartmentRequestDto: DepartmentRequestDto
    ): Resource<ResponseDto<String>> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection. Try again later.")
        }
        try {
            val response = adminServiceApi.updateDepartment(departmentId,updateDepartmentRequestDto)
            return if (response.isSuccess) {
                if (departmentDao.getDepartmentById(departmentId) != null) {
                    departmentDao.upsertDepartment(DepartmentEntity(title = updateDepartmentRequestDto.title, id = departmentId))
                }
                Resource.Success(
                    ResponseDto(
                        isSuccess = response.isSuccess,
                        statusCode = OK,
                        message = response.message,
                        data = response.data,
                        errors = response.errors
                    )
                )
            } else {
                Resource.Error(response.message)
            }
        } catch (e: IOException) {
            Timber.e("Network error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        } catch (e: Exception) {
            Timber.e("Unknown error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }

    override suspend fun deleteDepartment(departmentId: UUID): Resource<ResponseDto<String>> {
        if (!networkUtils.isNetworkAvailable()) {
            return Resource.Error("No internet connection. Try again later.")
        }
        try {
            val response = adminServiceApi.deleteDepartment(departmentId)
            return if (response.isSuccess) {
                if (departmentDao.getDepartmentById(departmentId) != null) {
                    departmentDao.deleteDepartment(departmentId)
                }
                Resource.Success(
                    ResponseDto(
                        isSuccess = response.isSuccess,
                        statusCode = OK,
                        message = response.message,
                        data = response.data,
                        errors = response.errors
                    )
                )
            } else {
                Resource.Error(response.message)
            }
        } catch (e: IOException) {
            Timber.e("Network error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        } catch (e: Exception) {
            Timber.e("Unknown error: ${e.message}")
            return Resource.Error("Something went wrong. Try again later")
        }
    }

    override suspend fun getTasks(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean,
        isRefreshing: Boolean
    ): Flow<Resource<ResponseDto<PaginatedData<Task>>>> {
        return flow {
            emit(Resource.Loading(true))
            try {

                if (forceFetchFromRemote || isRefreshing) {
                    if (!networkUtils.isNetworkAvailable()) {
                        emit(Resource.Error("No internet connection. Using cached data."))
                    } else {
                        try {
                            if (isRefreshing) {
                                taskDao.deleteAllTasks()
                            }

                            val response = adminServiceApi.getTasks(page, limit, search, sort)

                            if (!response.isSuccess) {
                                emit(Resource.Error("Failed to fetch data from server: ${response.message}"))
                            } else if (response.data != null) {
                                val remoteData = response.data.items.map { taskResponse ->
                                    taskResponse.toTaskEntity()
                                }
                                taskDao.upsertTasks(remoteData)
                            }
                        } catch (e: IOException) {
                            emit(Resource.Error("Network error: ${e.message}. Using cached data."))
                        } catch (e: Exception) {
                            emit(Resource.Error("Unexpected error: ${e.message}"))
                        }
                    }
                }

                taskDao.getPagedTasks(page, limit, search, sort)
                    .combine(taskDao.getTasksCountFlow(search)) { tasks, count ->
                        Pair(tasks, count)
                    }.collect { (tasks, count) ->
                        emit(
                            Resource.Success(
                                ResponseDto(
                                    isSuccess = true,
                                    statusCode = OK,
                                    message = if (forceFetchFromRemote || isRefreshing)
                                        "Updated from network" else "Loading from cache",
                                    data = PaginatedData(
                                        items = tasks.map { it.toTask() },
                                        page = page,
                                        pageSize = limit,
                                        totalCount = count,
                                        hasNextPage = (page * limit) < count,
                                        hasPreviousPage = page > 1
                                    ),
                                    errors = null
                                )
                            )
                        )
                    }
            } catch (e: IOException) {
                Timber.e(e, "Database error and network error")
                emit(Resource.Error("Database error: ${e.message}"))
            } catch (e: Exception) {
                emit(Resource.Error("Unexpected error: ${e.message}"))
            } finally {
                emit(Resource.Loading(false))
            }
        }.flowOn(Dispatchers.IO)
    }


}