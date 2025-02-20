package com.example.taskmanager.admin.di

import com.example.taskmanager.admin.data.repository.AdminRepositoryImpl
import com.example.taskmanager.admin.domain.repository.AdminRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Abdallah Elsokkary
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AdminRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAdminRepository(
        adminRepositoryImpl: AdminRepositoryImpl
    ): AdminRepository

}