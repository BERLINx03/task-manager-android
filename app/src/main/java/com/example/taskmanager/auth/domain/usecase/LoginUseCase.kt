package com.example.taskmanager.auth.domain.usecase

import com.example.taskmanager.auth.domain.repository.AuthRepository

/**
 * @author Abdallah Elsokkary
 */
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<String> {
        return authRepository.loginUser(username, password)
    }
}