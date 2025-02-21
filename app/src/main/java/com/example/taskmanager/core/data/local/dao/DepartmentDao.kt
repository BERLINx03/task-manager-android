package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.DepartmentEntity
import com.example.taskmanager.core.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Abdallah Elsokkary
 */

@Dao
interface DepartmentDao {
    @Query("""
    SELECT * FROM departments 
    WHERE (:search IS NULL OR 
          title LIKE '%' || :search || '%' )
    ORDER BY 
    CASE :sort
        WHEN 'name_asc' THEN title 
        WHEN 'date_asc' THEN lastSyncTimestamp 
        WHEN 'date_desc' THEN lastSyncTimestamp
        ELSE title
    END,
    CASE :sort
        WHEN 'name_desc' THEN 'DESC'
        WHEN 'date_desc' THEN 'DESC'
        ELSE 'ASC'
    END
    LIMIT :limit OFFSET ((:page - 1) * :limit)  
""")
    fun getPagedDepartments(
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<DepartmentEntity>>

    @Query("""
    SELECT COUNT(*) FROM departments
    WHERE (:search IS NULL OR 
          title LIKE '%' || :search || '%' )
""")
    fun getDepartmentsCountFlow(search: String? = null): Flow<Int>

    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id")
    fun getDepartmentById(id: String): Flow<DepartmentEntity?>

    @Upsert
    suspend fun upsertDepartments(departments: List<DepartmentEntity>)

    @Upsert
    suspend fun upsertDepartment(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartment(id: String)

    @Query("DELETE FROM departments")
    suspend fun deleteAllDepartments()
}