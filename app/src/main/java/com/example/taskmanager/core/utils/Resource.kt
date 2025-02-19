package com.example.taskmanager.core.utils

/**
 * @author Abdallah Elsokkary
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Resource<Nothing>()
    data class Loading(val isLoading: Boolean) : Resource<Nothing>()
}