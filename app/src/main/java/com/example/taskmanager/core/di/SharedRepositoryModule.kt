package com.example.taskmanager.core.di

import com.example.taskmanager.core.data.repository.SharedRepositoryImpl
import com.example.taskmanager.core.domain.repository.SharedRepository
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
abstract class SharedRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSharedRepository(
        sharedRepositoryImpl: SharedRepositoryImpl
    ): SharedRepository
}