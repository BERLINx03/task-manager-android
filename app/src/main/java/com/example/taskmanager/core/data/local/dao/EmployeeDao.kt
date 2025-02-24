package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.EmployeeEntity
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE id = :employeeId")
    suspend fun getEmployeeById(employeeId: UUID): EmployeeEntity?

    @Upsert
    suspend fun upsertEmployee(employeeEntity: EmployeeEntity)

    @Upsert
    suspend fun upsertEmployees(employeeEntities: List<EmployeeEntity>)

}