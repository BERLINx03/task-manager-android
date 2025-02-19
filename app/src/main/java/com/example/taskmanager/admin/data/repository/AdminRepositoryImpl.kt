package com.example.taskmanager.admin.data.repository

import com.example.taskmanager.admin.data.mapper.AdminMapper
import com.example.taskmanager.admin.data.mapper.toDomain
import com.example.taskmanager.admin.data.mapper.toEntity
import com.example.taskmanager.admin.data.remote.AdminServiceApi
import com.example.taskmanager.admin.data.remote.dto.UpdateAdminRequestDto
import com.example.taskmanager.admin.domain.model.Admin
import com.example.taskmanager.admin.domain.model.Department
import com.example.taskmanager.admin.domain.model.PaginatedData
import com.example.taskmanager.admin.domain.model.Task
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.local.dao.AdminDao
import com.example.taskmanager.core.data.local.dao.DepartmentDao
import com.example.taskmanager.core.data.local.dao.TaskDao
import com.example.taskmanager.core.utils.NetworkUtils
import com.example.taskmanager.core.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.util.UUID
import javax.inject.Inject


/**
 * @author Abdallah Elsokkary
 */
class AdminRepositoryImpl @Inject constructor(
    private val api: AdminServiceApi,
    private val mapper: AdminMapper,
    private val networkUtils: NetworkUtils,
    private val adminDao: AdminDao,
    private val departmentDao: DepartmentDao,
    private val taskDao: TaskDao
) : AdminRepository {

    override suspend fun getCurrentAdmin(): Resource<ResponseDto<Admin>> {
        try {
            val cachedAdmin = adminDao.getCurrentAdmin(current = true)
            if (cachedAdmin != null) {
                Timber.d("Returning admin from cache")
                return Resource.Success(
                    ResponseDto(
                        isSuccess = true,
                        statusCode = OK,
                        message = "Success from cached data",
                        data = cachedAdmin.toDomain(),
                        errors = null
                    )
                )
            }
            Timber.d("No cached admin found, fetching from network")

            if (!networkUtils.isNetworkAvailable()) {
                Timber.w("Network is not available")
                return Resource.Error("No internet connection. Try again later.")
            }

            val response = api.getCurrentAdmin()
            when (response.statusCode) {
                OK, CREATED -> {
                    Timber.i("Admin fetched successfully from network")

                    val admin = response.data?.let { mapper.mapToDomain(it) }
                    if (admin != null) {
                        adminDao.resetAndUpsertAdmin(admin.toEntity())

                        return Resource.Success(
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
            val response = api.getAdminById(adminId)
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

                val localAdminsFlow = adminDao.getPagedAdmins(page, limit, search, sort)
                    .combine(adminDao.getAdminsCountFlow(search)) { admins, count ->
                        Pair(admins, count)
                    }


                if (forceFetchFromRemote || isRefreshing) {
                    if (!networkUtils.isNetworkAvailable()) {
                        emit(Resource.Error("No internet connection. Using cached data."))
                    } else {
                        try {
                            if (isRefreshing) {
                                adminDao.deleteAllAdmins()
                            }

                            val response = api.getAdmins(page, limit, search, sort)

                            if (!response.isSuccess) {
                                emit(Resource.Error("Failed to fetch data from server: ${response.message}"))
                            } else if (response.data != null) {
                                val remoteData = response.data.items.map {
                                    mapper.mapToDomain(it).toEntity()
                                }
                                remoteData.forEach { adminEntity ->
                                    adminDao.upsertAdmin(adminEntity)
                                }
                            }
                        } catch (e: IOException) {
                            emit(Resource.Error("Network error: ${e.message}. Using cached data."))
                        } catch (e: Exception) {
                            emit(Resource.Error("Unexpected error: ${e.message}"))
                        }
                    }
                }

                localAdminsFlow.collect { (admins, count) ->
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

    override suspend fun updateAdmin(admin: UpdateAdminRequestDto): Resource<String> {
        val hasNetwork = networkUtils.isNetworkAvailable()
        if (!hasNetwork) {
            return Resource.Error("No internet connection. Try again later.")
        }
        try {
            val adminResponse = api.updateAdmin(admin)
            when (adminResponse.statusCode) {
                OK, CREATED -> {
                    val currentAdmin = adminResponse.data?.let { adminDao.getAdminById(it) }
                    currentAdmin?.let {
                        adminDao.resetAndUpsertAdmin(it)
                    }
                    return Resource.Success(adminResponse.message)
                }

                BAD_REQUEST -> return Resource.Error(adminResponse.message)
                UNAUTHORIZED -> return Resource.Error(adminResponse.message)
                FORBIDDEN -> return Resource.Error(adminResponse.message)
                NOT_FOUND -> return Resource.Error(adminResponse.message)
                HTTP_CONFLICT -> return Resource.Error(adminResponse.message)
                else -> return Resource.Error(adminResponse.message)
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

    override suspend fun createDepartment(title: String): Resource<ResponseDto<Department>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDepartments(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Department>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDepartmentById(departmentId: String): Resource<ResponseDto<Department>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateDepartment(
        departmentId: String,
        title: String
    ): Resource<ResponseDto<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteDepartment(departmentId: String): Resource<ResponseDto<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDepartmentEmployees(
        departmentId: String,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Department>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getDepartmentManagers(
        departmentId: String,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Department>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllTask(
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Task>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getTaskById(taskId: UUID): Resource<ResponseDto<Task>> {
        TODO("Not yet implemented")
    }

    override suspend fun getEmployeeTasks(
        employeeId: UUID,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Task>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getManagerTasks(
        managerId: UUID,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?
    ): Resource<ResponseDto<PaginatedData<Task>>> {
        TODO("Not yet implemented")
    }


    companion object {
        const val OK = HttpURLConnection.HTTP_OK
        const val CREATED = HttpURLConnection.HTTP_CREATED
        const val BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST
        const val HTTP_CONFLICT = HttpURLConnection.HTTP_CONFLICT
        const val UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED
        const val FORBIDDEN = HttpURLConnection.HTTP_FORBIDDEN
        const val NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND
    }
}