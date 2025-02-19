package com.example.taskmanager.core.di

import android.content.Context
import com.example.taskmanager.core.data.local.dao.AdminDao
import com.example.taskmanager.core.data.local.dao.DepartmentDao
import com.example.taskmanager.core.data.local.dao.TaskDao
import com.example.taskmanager.core.data.local.database.TaskManagerDatabase
import com.example.taskmanager.core.utils.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskManagerDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskManagerDatabase {
        return TaskManagerDatabase.getInstance(context)
    }

    @Provides
    fun provideAdminDao(database: TaskManagerDatabase): AdminDao {
        return database.adminDao
    }

    @Provides
    fun provideDepartmentDao(database: TaskManagerDatabase): DepartmentDao {
        return database.departmentDao
    }

    @Provides
    fun provideTaskDao(database: TaskManagerDatabase): TaskDao {
        return database.taskDao
    }

    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }
}
