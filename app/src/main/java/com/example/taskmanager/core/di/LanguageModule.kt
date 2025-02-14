package com.example.taskmanager.core.di

import android.content.Context
import com.example.taskmanager.core.data.local.LanguageDataStore
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
object LanguageModule {

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
}