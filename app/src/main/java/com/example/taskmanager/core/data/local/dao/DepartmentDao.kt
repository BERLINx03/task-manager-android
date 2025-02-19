package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taskmanager.core.data.local.entities.DepartmentEntity
import kotlinx.coroutines.flow.Flow

/**
 * @author Abdallah Elsokkary
 */

@Dao
interface DepartmentDao {
    @Query("SELECT * FROM departments")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Query("SELECT * FROM departments WHERE id = :id")
    fun getDepartmentById(id: String): Flow<DepartmentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<DepartmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartment(id: String)
}