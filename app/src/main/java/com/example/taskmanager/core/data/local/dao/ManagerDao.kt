package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.ManagerEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@Dao
interface ManagerDao {

    @Query("""
    SELECT * FROM managers 
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
    fun getPagedManagers(
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<ManagerEntity>>

    @Query("""
    SELECT COUNT(*) FROM managers 
    WHERE (:search IS NULL OR 
          firstName LIKE '%' || :search || '%' OR 
          lastName LIKE '%' || :search || '%')
""")
    fun getTotalCount(search: String?): Int

    @Query("""
    SELECT COUNT(*) FROM managers 
    WHERE (:departmentId IS NULL OR departmentId = :departmentId)
""")
    fun countManagersByDepartment(
        departmentId: UUID
    ): Flow<Int>

    @Query("""
    SELECT * FROM managers 
    WHERE (:departmentId IS NULL OR departmentId = :departmentId)
      AND (:search IS NULL OR
           firstName LIKE '%' || :search || '%' OR
           lastName LIKE '%' || :search || '%')
    LIMIT :limit OFFSET ((:page - 1) * :limit)
""")
    fun getPagedManagersByDepartment(
        departmentId: UUID,
        search: String?,
        page: Int,
        limit: Int,
    ): Flow<List<ManagerEntity>>

    @Query("SELECT * FROM managers WHERE id = :managerId")
    suspend fun getAllManagers(managerId: UUID): ManagerEntity

    @Upsert
    suspend fun upsertManager(manager: ManagerEntity)

    @Upsert
    suspend fun upsertManagers(managers: List<ManagerEntity>)

    @Query("DELETE FROM managers")
    suspend fun deleteAllManagers()

    @Query("SELECT * FROM managers WHERE id = :managerId")
    suspend fun getManagerById(managerId: UUID): ManagerEntity?

    @Query("DELETE FROM managers WHERE id = :managerId")
    suspend fun deleteManagerById(managerId: UUID)
}