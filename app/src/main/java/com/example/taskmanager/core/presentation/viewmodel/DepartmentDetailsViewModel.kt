package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.core.data.remote.dto.DepartmentRequestDto
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.domain.model.PaginatedData
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.DepartmentDetailsIntents
import com.example.taskmanager.core.presentation.state.DepartmentDetailsState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class DepartmentDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val adminRepository: AdminRepository,
    private val sharedRepository: SharedRepository,
    userRoleDataStore: TokenDataStore
) : ViewModel() {
    val departmentId = savedStateHandle.get<String>("departmentId") ?: ""
    val userRole = userRoleDataStore.userRole

    private val _departmentDetailState = MutableStateFlow(DepartmentDetailsState())
    val departmentDetailState = _departmentDetailState.asStateFlow()

    private val _successAddedMessage = MutableSharedFlow<Unit>()
    val successAddedMessage = _successAddedMessage.asSharedFlow()

    private val _successDeletedMessage = MutableSharedFlow<Unit>()
    val successDeletedMessage = _successDeletedMessage.asSharedFlow()


    private val _departmentManagersState =
        MutableStateFlow(DepartmentDetailsState.DepartmentManagersState())
    val departmentManagersState = _departmentManagersState.asStateFlow()

    private val _departmentEmployeesState =
        MutableStateFlow(DepartmentDetailsState.DepartmentEmployeesState())
    val departmentEmployeesState = _departmentEmployeesState.asStateFlow()

    private val pageSize = 10

    init {
        loadDepartmentDetails()
        loadDepartmentManagers(false)
        loadDepartmentEmployees(false)
    }
    fun onIntent(intent: DepartmentDetailsIntents) {
        when (intent) {
            is DepartmentDetailsIntents.DeleteDepartment -> deleteDepartment()
            is DepartmentDetailsIntents.LoadDepartmentDetails -> loadDepartmentDetails()
            is DepartmentDetailsIntents.LoadDepartmentEmployees -> loadDepartmentEmployees(false)
            is DepartmentDetailsIntents.LoadNextDepartmentEmployees -> loadNextPageDepartmentEmployees()
            is DepartmentDetailsIntents.LoadPreviousDepartmentEmployees -> loadPreviousPageDepartmentEmployees()
            is DepartmentDetailsIntents.LoadDepartmentManagers -> loadDepartmentManagers(false)
            is DepartmentDetailsIntents.LoadNextDepartmentManagers -> loadNextPageDepartmentManagers()
            is DepartmentDetailsIntents.LoadPreviousDepartmentManagers -> loadPreviousPageDepartmentManagers()
            is DepartmentDetailsIntents.RefreshManagers -> {
                viewModelScope.launch {
                    _departmentDetailState.update { it.copy(isRefreshing = true) }
                    try {
                        loadDepartmentManagers(true)
                        delay(300)
                    } finally {
                        _departmentDetailState.update { it.copy(isRefreshing = false) }
                    }
                }
            }
            is DepartmentDetailsIntents.RefreshEmployees -> {
                viewModelScope.launch {
                    _departmentDetailState.update { it.copy(isRefreshing = true) }
                    try {
                        loadDepartmentEmployees(true)
                        delay(300)
                    } finally {
                        _departmentDetailState.update { it.copy(isRefreshing = false) }
                    }
                }
            }
            is DepartmentDetailsIntents.UpdateDepartment -> updateDepartment(intent.title)
        }
    }

    private fun loadDepartmentDetails() {
        viewModelScope.launch {
            when (val result = getDepartmentDetailsOnIO(departmentId)) {
                is Resource.Error -> {
                    _departmentDetailState.update {
                        it.copy(
                            errorMessage = result.message
                        )
                    }
                }

                is Resource.Loading -> {
                    _departmentDetailState.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                }

                is Resource.Success -> {
                    _departmentDetailState.update {
                        it.copy(
                            isLoading = false,
                            department = result.data
                        )
                    }
                }
            }
        }
    }

    private fun updateDepartment(title: String) {
        viewModelScope.launch {
            when (val result = updateDepartmentOnIO(departmentId,title)){
                is Resource.Error -> {
                    _departmentDetailState.update {
                        it.copy(
                            errorMessage = result.message
                        )
                    }
                }
                is Resource.Loading -> {
                    _departmentDetailState.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                }
                is Resource.Success -> {
                    _departmentDetailState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    _successAddedMessage.emit(Unit)
                }
            }
        }
    }
    private fun deleteDepartment() {
        viewModelScope.launch {
            when (val result = deleteDepartmentOnIO(departmentId)) {
                is Resource.Error -> {
                    _departmentDetailState.update {
                        it.copy(
                            errorMessage = result.message
                        )
                    }
                }

                is Resource.Loading -> {
                    _departmentDetailState.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                }

                is Resource.Success -> {
                    _departmentDetailState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    _successDeletedMessage.emit(Unit)
                }
            }
        }
    }

    private fun loadDepartmentManagers(forceFetchFromRemote: Boolean) {
        viewModelScope.launch {
            getDepartmentManagersOnIO(
                departmentId,
                1,
                pageSize,
                _departmentManagersState.value.searchQuery,
                _departmentManagersState.value.sortOption,
                forceFetchFromRemote
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _departmentManagersState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _departmentManagersState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val managers = resource.data
                        _departmentManagersState.update {
                            it.copy(
                                managers = managers.items,
                                currentPage = managers.page,
                                totalPages = calculateTotalPages(
                                    managers.totalCount,
                                    managers.pageSize
                                ),
                                hasNextPage = managers.hasNextPage,
                                hasPreviousPage = managers.hasPreviousPage,
                                errorMessage = null,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
    private fun loadNextPageDepartmentManagers() {
        if (!_departmentManagersState.value.hasNextPage || _departmentManagersState.value.isLoading) return
        viewModelScope.launch {
            getDepartmentManagersOnIO(
                departmentId,
                _departmentManagersState.value.currentPage + 1,
                pageSize,
                _departmentManagersState.value.searchQuery,
                _departmentManagersState.value.sortOption,
                false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _departmentManagersState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _departmentManagersState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val managers = resource.data
                        _departmentManagersState.update {
                            it.copy(
                                managers = managers.items,
                                currentPage = managers.page,
                                hasNextPage = managers.hasNextPage,
                                hasPreviousPage = managers.hasPreviousPage,
                                errorMessage = null,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
    private fun loadPreviousPageDepartmentManagers() {
        if (!_departmentManagersState.value.hasPreviousPage || _departmentManagersState.value.isLoading) return

        viewModelScope.launch {
            getDepartmentManagersOnIO(
                departmentId,
                _departmentManagersState.value.currentPage - 1,
                pageSize,
                _departmentManagersState.value.searchQuery,
                _departmentManagersState.value.sortOption,
                false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _departmentManagersState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _departmentManagersState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val managers = resource.data
                        _departmentManagersState.update {
                            it.copy(
                                managers = managers.items,
                                currentPage = managers.page,
                                hasNextPage = managers.hasNextPage,
                                hasPreviousPage = managers.hasPreviousPage,
                                errorMessage = null,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }


    private fun loadDepartmentEmployees(forceFetchFromRemote: Boolean) {
        viewModelScope.launch {
            getDepartmentEmployeesOnIO(
                departmentId,
                1,
                pageSize,
                _departmentEmployeesState.value.searchQuery,
                _departmentEmployeesState.value.sortOption,
                forceFetchFromRemote
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _departmentEmployeesState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _departmentEmployeesState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val employees = resource.data
                        _departmentEmployeesState.update {
                            it.copy(
                                employees = employees.items,
                                currentPage = employees.page,
                                totalPages = calculateTotalPages(
                                    employees.totalCount,
                                    employees.pageSize
                                ),
                                hasNextPage = employees.hasNextPage,
                                hasPreviousPage = employees.hasPreviousPage,
                                errorMessage = null,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
    private fun loadNextPageDepartmentEmployees() {
        if (!_departmentEmployeesState.value.hasNextPage || _departmentEmployeesState.value.isLoading) return

        viewModelScope.launch {
            getDepartmentEmployeesOnIO(
                departmentId,
                _departmentEmployeesState.value.currentPage + 1,
                pageSize,
                _departmentEmployeesState.value.searchQuery,
                _departmentEmployeesState.value.sortOption,
                false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _departmentEmployeesState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _departmentEmployeesState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val employees = resource.data
                        _departmentEmployeesState.update {
                            it.copy(
                                employees = employees.items,
                                currentPage = employees.page,
                                hasNextPage = employees.hasNextPage,
                                hasPreviousPage = employees.hasPreviousPage,
                                errorMessage = null,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
    private fun loadPreviousPageDepartmentEmployees() {
        if (!_departmentEmployeesState.value.hasPreviousPage || _departmentEmployeesState.value.isLoading) return

        viewModelScope.launch {
            getDepartmentEmployeesOnIO(
                departmentId,
                _departmentEmployeesState.value.currentPage - 1,
                pageSize,
                _departmentEmployeesState.value.searchQuery,
                _departmentEmployeesState.value.sortOption,
                false
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _departmentEmployeesState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _departmentEmployeesState.update {
                            it.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val employees = resource.data
                        _departmentEmployeesState.update {
                            it.copy(
                                employees = employees.items,
                                currentPage = employees.page,
                                hasNextPage = employees.hasNextPage,
                                hasPreviousPage = employees.hasPreviousPage,
                                errorMessage = null,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }












    private suspend fun getDepartmentManagersOnIO(
        departmentId: String,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<PaginatedData<ManagerAndEmployee>>> {
        return withContext(Dispatchers.IO) {
            sharedRepository.getManagersInDepartment(
                departmentId = UUID.fromString(departmentId),
                page = page,
                limit = limit,
                search = search,
                sort = sort,
                forceFetchFromRemote = forceFetchFromRemote
            )
        }
    }

    private suspend fun getDepartmentEmployeesOnIO(
        departmentId: String,
        page: Int,
        limit: Int,
        search: String?,
        sort: String?,
        forceFetchFromRemote: Boolean
    ): Flow<Resource<PaginatedData<ManagerAndEmployee>>> {
        return withContext(Dispatchers.IO) {
            sharedRepository.getEmployeesInDepartment(
                departmentId = UUID.fromString(departmentId),
                page = page,
                limit = limit,
                search = search,
                sort = sort,
                forceFetchFromRemote = forceFetchFromRemote
            )
        }
    }

    private suspend fun getDepartmentDetailsOnIO(departmentId: String): Resource<Department> {
        return withContext(Dispatchers.IO) {
            if (departmentId.isBlank()) {
                return@withContext Resource.Error("Invalid department ID")
            }
            sharedRepository.getDepartmentById(UUID.fromString(departmentId))
        }
    }

    private suspend fun updateDepartmentOnIO(departmentId: String,title: String): Resource<ResponseDto<String>> {
        return withContext(Dispatchers.IO) {
            adminRepository.updateDepartment(departmentId = UUID.fromString(departmentId), updateDepartmentRequestDto = DepartmentRequestDto(title = title))
        }
    }

    private suspend fun deleteDepartmentOnIO(departmentId: String): Resource<ResponseDto<String>> {
        return withContext(Dispatchers.IO) {
            adminRepository.deleteDepartment(UUID.fromString(departmentId))
        }
    }

    private fun calculateTotalPages(totalCount: Int, pageSize: Int): Int {
        return if (totalCount == 0 || pageSize == 0) {
            1
        } else {
            (totalCount + pageSize - 1) / pageSize
        }
    }
}