package com.example.taskmanager.admin.data.mapper

import androidx.room.TypeConverter
import com.example.taskmanager.core.data.local.entities.AdminEntity
import com.example.taskmanager.admin.domain.model.Admin
import com.example.taskmanager.admin.domain.model.Department
import com.example.taskmanager.admin.domain.model.ManagerAndEmployee
import com.example.taskmanager.admin.domain.model.PaginatedData
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
class AdminMapper @Inject constructor() {

    fun mapToDomain(response: ResponseDto.AdminResponse): Admin {
        return Admin(
            id = response.id,
            firstName = response.firstName,
            lastName = response.lastName,
            phoneNumber = response.phoneNumber,
            gender = response.gender,
            birthDate = response.birthDate
        )
    }

    fun mapToDomain(response: ResponseDto.DepartmentResponse): Department {
        return Department(
            id = response.id,
            title = response.title
        )
    }

    fun mapPaginatedAdminsToDomain(response: ResponseDto.PaginatedResponse<ResponseDto.AdminResponse>): PaginatedData<Admin> {
        return PaginatedData(
            items = response.items.map { mapToDomain(it) },
            page = response.page,
            pageSize = response.pageSize,
            totalCount = response.totalCount,
            hasNextPage = response.hasNextPage,
            hasPreviousPage = response.hasPreviousPage
        )
    }

    fun mapToDomain(response: ResponseDto.ManagerAndEmployeeResponse): ManagerAndEmployee {
        return ManagerAndEmployee(
            firstName = response.firstName,
            lastName = response.lastName,
            phoneNumber = response.phoneNumber,
            gender = response.gender,
            birthDate = response.birthDate,
            departmentId = response.departmentId,
            id = response.id
        )
    }

    fun mapPaginatedDepartmentsToDomain(response: ResponseDto.PaginatedResponse<ResponseDto.DepartmentResponse>): PaginatedData<Department> {
        return PaginatedData(
            items = response.items.map { mapToDomain(it) },
            page = response.page,
            pageSize = response.pageSize,
            totalCount = response.totalCount,
            hasNextPage = response.hasNextPage,
            hasPreviousPage = response.hasPreviousPage
        )
    }

    fun mapPaginatedManagersAndEmployeesToDomain(response: ResponseDto.PaginatedResponse<ResponseDto.ManagerAndEmployeeResponse>): PaginatedData<ManagerAndEmployee> {
        return PaginatedData(
            items = response.items.map { mapToDomain(it) },
            page = response.page,
            pageSize = response.pageSize,
            totalCount = response.totalCount,
            hasNextPage = response.hasNextPage,
            hasPreviousPage = response.hasPreviousPage
        )
    }
}

fun AdminEntity.toDomain(): Admin {
    return Admin(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        gender = gender,
        birthDate = birthDate
    )
}

fun Admin.toEntity(): AdminEntity {
    return AdminEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phoneNumber = phoneNumber,
        gender = gender,
        birthDate = birthDate
    )
}

class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID): String {
        return uuid.toString()
    }

    @TypeConverter
    fun toUUID(uuidString: String): UUID {
        return UUID.fromString(uuidString)
    }
}
