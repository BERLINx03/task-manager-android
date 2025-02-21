package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.core.data.remote.SharedApiService
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.presentation.state.DepartmentsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class DepartmentsViewModel @Inject constructor(
    private val sharedApiService: SharedApiService
) : ViewModel() {

    private val _departmentsState = MutableStateFlow(DepartmentsState())
    val departmentsState = _departmentsState.asStateFlow()


    fun updateSearchQuery(query: String) {
        _departmentsState.update { it.copy(searchQuery = query) }
    }

    fun loadDepartments() {

    }

    fun loadNextPage() {

    }

    fun loadPreviousPage() {

    }

    fun addDepartment(title: String) {

    }
}
