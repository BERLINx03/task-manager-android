package com.example.taskmanager.manager.di

import com.example.taskmanager.manager.data.repository.ManagerRepositoryImpl
import com.example.taskmanager.manager.domain.repository.ManagerRepository
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
abstract class ManagerRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindManagerRepository(
        managerRepositoryImpl: ManagerRepositoryImpl
    ): ManagerRepository
}