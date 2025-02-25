package com.example.taskmanager.core.presentation.intents

import com.example.taskmanager.core.domain.model.Department

/**
 * @author Abdallah Elsokkary
 */
sealed interface DepartmentDetailsIntents {
    data object RefreshManagers : DepartmentDetailsIntents
    data object RefreshEmployees : DepartmentDetailsIntents
    data class LoadDepartmentDetails(val departmentId: String) : DepartmentDetailsIntents
    data class LoadDepartmentManagers(val departmentId: String) : DepartmentDetailsIntents
    data class LoadNextDepartmentManagers(val departmentId: String) : DepartmentDetailsIntents
    data class LoadPreviousDepartmentManagers(val departmentId: String) : DepartmentDetailsIntents
    data class LoadDepartmentEmployees(val departmentId: String) : DepartmentDetailsIntents
    data class LoadNextDepartmentEmployees(val departmentId: String) : DepartmentDetailsIntents
    data class LoadPreviousDepartmentEmployees(val departmentId: String) : DepartmentDetailsIntents
    data class UpdateDepartment(val title: String) : DepartmentDetailsIntents
    data class DeleteDepartment(val departmentId: String) : DepartmentDetailsIntents
}