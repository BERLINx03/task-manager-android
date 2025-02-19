package com.example.taskmanager

import android.app.Application
import com.example.taskmanager.core.domain.repository.LanguageRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltAndroidApp
class TaskManagerApplication: Application() {

    @Inject
    lateinit var languageRepository: LanguageRepository

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            languageRepository.getSelectedLanguage().first()
                .let { languageCode ->
                    languageRepository.setLanguage(languageCode)
                }
        }

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
    }
}