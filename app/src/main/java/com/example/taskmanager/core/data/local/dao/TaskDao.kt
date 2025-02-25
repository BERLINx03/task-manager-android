package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@Dao
interface TaskDao {

    @Query("""
    SELECT * FROM tasks 
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
    fun getPagedTasks(
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<TaskEntity>>

    @Query("""
    SELECT * FROM tasks 
    WHERE managerId = :managerId AND
          (:search IS NULL OR 
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
    fun getManagerTasks(
        managerId: UUID,
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<TaskEntity>>

    @Query("""
    SELECT * FROM tasks 
    WHERE employeeId = :employeeId AND
          (:search IS NULL OR 
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
    fun getEmployeeTasks(
        employeeId: UUID,
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<TaskEntity>>

    @Query("""
        SELECT COUNT(*) FROM tasks
        WHERE managerId = :managerId AND
            (:search IS NULL OR
            title LIKE '%' || :search || '%')
    """)
    suspend fun getManagersTasksCount(managerId: UUID, search: String? = null): Int

    @Query("""
        SELECT COUNT(*) FROM tasks
        WHERE employeeId = :employeeId AND
            (:search IS NULL OR
            title LIKE '%' || :search || '%')
    """)
    suspend fun getEmployeesTasksCount(employeeId: UUID, search: String? = null): Int


    @Query("""
    SELECT COUNT(*) FROM tasks
    WHERE (:search IS NULL OR 
          title LIKE '%' || :search || '%' )
""")
    fun getTasksCountFlow(search: String? = null): Flow<Int>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: UUID): TaskEntity?

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM tasks WHERE managerId = :managerId")
    suspend fun deleteTasksByManagerId(managerId: UUID)
}