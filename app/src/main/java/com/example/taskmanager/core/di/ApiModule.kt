package com.example.taskmanager.core.di

import com.example.taskmanager.admin.data.remote.AdminServiceApi
import com.example.taskmanager.core.data.remote.SharedApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAdminApiService(@AdminRetrofit retrofit: Retrofit): AdminServiceApi {
        return retrofit.create(AdminServiceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSharedApiService(@SharedRetrofit retrofit: Retrofit): SharedApiService {
        return retrofit.create(SharedApiService::class.java)
    }
}