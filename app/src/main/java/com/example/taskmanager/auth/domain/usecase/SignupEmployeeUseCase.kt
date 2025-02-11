package com.example.taskmanager.auth.domain.usecase

import com.example.taskmanager.auth.domain.model.Employee
import com.example.taskmanager.auth.domain.repository.AuthRepository

/**
 * @author Abdallah Elsokkary
 */
class SignupEmployeeUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(employee: Employee): Result<Unit> {
        return authRepository.registerEmployee(employee)
    }
}