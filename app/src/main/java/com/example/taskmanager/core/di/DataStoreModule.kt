package com.example.taskmanager.core.di

import android.content.Context
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.LanguageDataStore
import com.example.taskmanager.core.data.local.datastore.StatisticsDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.data.local.repository.LanguageRepositoryImpl
import com.example.taskmanager.core.domain.repository.LanguageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideLanguageDataStore(@ApplicationContext context: Context): LanguageDataStore {
        return LanguageDataStore(context)
    }

    @Provides
    @Singleton
    fun provideLanguageRepository(
        languageDataStore: LanguageDataStore,
        @ApplicationContext context: Context
    ): LanguageRepository {
        return LanguageRepositoryImpl(languageDataStore, context)
    }

    @Provides
    @Singleton
    fun provideTokenDataStore(@ApplicationContext context: Context): TokenDataStore {
        return TokenDataStore(context)
    }

    @Provides
    @Singleton
    fun provideStatisticsDataStore(@ApplicationContext context: Context): StatisticsDataStore {
        return StatisticsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideUserInfoDataStore(@ApplicationContext context: Context): UserInfoDataStore {
        return UserInfoDataStore(context)
    }

}