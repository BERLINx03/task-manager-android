package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.DepartmentEntity
import com.example.taskmanager.core.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */

@Dao
interface DepartmentDao {
    @Query(
        """
    SELECT * FROM departments 
    WHERE (:search IS NULL OR title LIKE '%' || :search || '%' COLLATE NOCASE)
    ORDER BY
    CASE WHEN :sort = 'title_asc' THEN title END COLLATE NOCASE ASC,
    CASE WHEN :sort = 'title_desc' THEN title END COLLATE NOCASE DESC,
    CASE WHEN :sort = 'date_asc' THEN lastSyncTimestamp END ASC,
    CASE WHEN :sort = 'date_desc' THEN lastSyncTimestamp END DESC,
    CASE WHEN :sort NOT IN ('title_asc', 'title_desc', 'date_asc', 'date_desc') THEN title END COLLATE NOCASE ASC
    LIMIT :limit OFFSET ((:page - 1) * :limit)
    """
    )
    fun getPagedDepartments(
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<DepartmentEntity>>

    @Query(
        """SELECT COUNT(*) FROM departments
            WHERE (:search IS NULL OR 
            title LIKE '%' || :search || '%' COLLATE NOCASE)"""
    )
    fun getDepartmentsCountFlow(search: String? = null): Flow<Int>

    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id")
    fun getDepartmentById(id: UUID): DepartmentEntity?

    @Upsert
    suspend fun upsertDepartments(departments: List<DepartmentEntity>)

    @Upsert
    suspend fun upsertDepartment(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartment(id: UUID)

    @Query("DELETE FROM departments")
    suspend fun deleteAllDepartments()
}