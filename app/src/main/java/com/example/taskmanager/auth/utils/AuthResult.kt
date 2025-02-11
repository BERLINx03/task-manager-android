package com.example.taskmanager.auth.utils

/**
 * @author Abdallah Elsokkary
 */
sealed class AuthResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Authenticated<T>(data: T? = null) : AuthResult<T>(data)
    class UnAuthenticated<T>(message: String? = null) : AuthResult<T>(message = message)
    class UnknownError<T>(message: String? = null) : AuthResult<T>(message = message)
    class Loading<T>(data: T? = null) : AuthResult<T>(data)
}