package com.example.taskmanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.taskmanager.core.data.local.entities.AdminEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@Dao
interface AdminDao {

    @Query("SELECT * FROM admins WHERE current = :current")
    suspend fun getCurrentAdmin(current: Boolean): AdminEntity?

    @Query("UPDATE admins SET current = 0")
    suspend fun resetAllAdmins()

    @Transaction
    suspend fun resetAndUpsertAdmin(admin: AdminEntity) {
        resetAllAdmins()
        upsertAdmin(admin.copy(current = true))
    }

    @Query("SELECT * FROM admins WHERE id = :adminId")
    suspend fun getAdminById(adminId: UUID): AdminEntity?

    @Upsert
    suspend fun upsertAdmins(admins: List<AdminEntity>)

    @Upsert
    suspend fun upsertAdmin(admin: AdminEntity)


    @Query("SELECT * FROM admins WHERE gender = :gender")
    fun filterAdminsByGender(gender: Int): Flow<List<AdminEntity>>


    @Query("DELETE FROM admins WHERE id = :adminId")
    suspend fun deleteAdmin(adminId: UUID)

    @Query("DELETE FROM admins")
    suspend fun deleteAllAdmins()

    @Query("""
    SELECT * FROM admins 
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
    fun getPagedAdmins(
        page: Int,
        limit: Int,
        search: String? = null,
        sort: String? = null
    ): Flow<List<AdminEntity>>

    @Query("""
    SELECT COUNT(*) FROM admins
    WHERE (:search IS NULL OR 
          firstName LIKE '%' || :search || '%' OR 
          lastName LIKE '%' || :search || '%')
""")
    fun getAdminsCountFlow(search: String? = null): Flow<Int>

}