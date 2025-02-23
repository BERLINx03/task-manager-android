package com.example.taskmanager.core.presentation.intents

sealed class DepartmentIntents{
    data class LoadDepartments(val forceFetchFromRemote: Boolean): DepartmentIntents()
    data object LoadNextPage: DepartmentIntents()
    data object LoadPreviousPage: DepartmentIntents()
    data class OnSearchQueryChange(val query: String): DepartmentIntents()
    data class AddDepartment(val title: String): DepartmentIntents()
    data object Refresh: DepartmentIntents()
    data object OnTitleChanged: DepartmentIntents()
    data class Navigating(val departmentId: Int): DepartmentIntents()
}
