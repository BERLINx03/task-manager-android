package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.EmployeeEntity
import com.example.taskmanager.core.data.local.entities.ManagerEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@Dao
interface EmployeeDao {

    @Query("""
    SELECT * FROM employees 
    WHERE (:search IS NULL OR 
          firstName LIKE '%' || :search || '%' OR 
          lastName LIKE '%' || :search || '%')
    ORDER BY 
    CASE :sort
        WHEN 'name_asc' THEN firstName 
        WHEN 'name_desc' THEN firstName 
        WHEN 'date_asc' THEN lastSyncTimestamp 
        WHEN 'date_desc' THEN lastSyncTimestamp
        ELSE firstName
    END,
    CASE :sort
        WHEN 'name_desc' THEN 'DESC'
        WHEN 'date_desc' THEN 'DESC'
        ELSE 'ASC'
    END
    LIMIT :limit OFFSET ((:page - 1) * :limit)  
""")
    fun getPagedEmployees(
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<EmployeeEntity>>

    @Query("""
    SELECT COUNT(*) FROM employees 
    WHERE (:search IS NULL OR 
          firstName LIKE '%' || :search || '%' OR 
          lastName LIKE '%' || :search || '%')
""")
    fun getTotalCount(search: String?): Int


    @Query("""
    SELECT * FROM employees 
    WHERE (:departmentId IS NULL OR departmentId = :departmentId)
      AND (:search IS NULL OR
           firstName LIKE '%' || :search || '%' OR
           lastName LIKE '%' || :search || '%')
    LIMIT :limit OFFSET ((:page - 1) * :limit)
""")
    fun getPagedEmployeesByDepartment(
        departmentId: UUID,
        search: String?,
        page: Int,
        limit: Int,
    ): Flow<List<EmployeeEntity>>

    @Query("""
    SELECT COUNT(*) FROM employees 
    WHERE (:departmentId IS NULL OR departmentId = :departmentId)
""")
    fun countEmployeesByDepartment(
        departmentId: UUID
    ): Flow<Int>

    @Query("SELECT * FROM employees WHERE id = :employeeId")
    suspend fun getEmployeeById(employeeId: UUID): EmployeeEntity?

    @Query("DELETE FROM employees WHERE id = :employeeId")
    suspend fun deleteEmployeeById(employeeId: UUID)

    @Upsert
    suspend fun upsertEmployee(employeeEntity: EmployeeEntity)


    @Upsert
    suspend fun upsertEmployees(employeeEntities: List<EmployeeEntity>)

    @Query("SELECT * FROM employees WHERE departmentId = :departmentId AND (firstName LIKE '%' || :search || '%' OR :search IS NULL) ORDER BY firstName ASC")
    fun getAllEmployeesByDepartment(departmentId: UUID, search: String?): Flow<List<EmployeeEntity>>

}