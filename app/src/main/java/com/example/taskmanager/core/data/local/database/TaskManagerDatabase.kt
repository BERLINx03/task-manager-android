package com.example.taskmanager.core.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskmanager.core.data.local.dao.AdminDao
import com.example.taskmanager.core.data.local.dao.DepartmentDao
import com.example.taskmanager.core.data.local.dao.TaskDao
import com.example.taskmanager.core.data.local.entities.AdminEntity
import com.example.taskmanager.core.data.local.entities.DepartmentEntity
import com.example.taskmanager.core.data.local.entities.TaskEntity
import com.example.taskmanager.admin.data.mapper.UUIDConverter
import com.example.taskmanager.core.data.local.dao.EmployeeDao
import com.example.taskmanager.core.data.local.dao.ManagerDao
import com.example.taskmanager.core.data.local.entities.EmployeeEntity
import com.example.taskmanager.core.data.local.entities.ManagerEntity

/**
 * @author Abdallah Elsokkary
 */
@Database(
    entities = [
        AdminEntity::class,
        ManagerEntity::class,
        EmployeeEntity::class,
        DepartmentEntity::class,
        TaskEntity::class,
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(UUIDConverter::class)
abstract class TaskManagerDatabase : RoomDatabase() {
    abstract val adminDao: AdminDao
    abstract val managerDao: ManagerDao
    abstract val employeeDao: EmployeeDao
    abstract val departmentDao: DepartmentDao
    abstract val taskDao: TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskManagerDatabase? = null

        fun getInstance(context: Context): TaskManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskManagerDatabase::class.java,
                    "task_manager_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}