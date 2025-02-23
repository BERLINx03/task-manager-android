package com.example.taskmanager.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.presentation.intents.ManagersIntents
import com.example.taskmanager.core.presentation.state.ManagersState
import com.example.taskmanager.core.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class ManagersViewModel @Inject constructor(
    private val sharedRepository: SharedRepository,
    private val adminRepository: AdminRepository,
    private val userInfoDataStore: UserInfoDataStore,
    userRoleDataStore: TokenDataStore,
) : ViewModel() {

    private val _managersState = MutableStateFlow(ManagersState())
    val managersState = _managersState.asStateFlow()

    val userRole = userRoleDataStore.userRole

    private val pageSize = 10

    init {
        loadManager(forceFetchFromRemote = false)
        viewModelScope.launch {
            userInfoDataStore.userInfoFlow.collect { user ->
                _managersState.update {
                    it.copy(user = user)
                }
            }
        }
    }

    fun onIntent(intent: ManagersIntents) {
        when (intent) {
            is ManagersIntents.Load -> loadManager(intent.forceFetchFromRemote)
            ManagersIntents.LoadNextPage -> loadNextPage()
            ManagersIntents.LoadPreviousPage -> loadPreviousPage()
            is ManagersIntents.OnSearchQueryChange -> search(query = intent.query)
            ManagersIntents.Refresh -> {
                _managersState.update { it.copy(isRefreshing = true) }
                viewModelScope.launch {
                    try {
                        loadManager(forceFetchFromRemote = true)
                        delay(300)
                    } finally {
                        _managersState.update { it.copy(isRefreshing = false) }
                    }
                }
            }

            is ManagersIntents.OnDeleteManager -> deleteManager(intent.managerId)
        }
    }

    private fun loadManager(forceFetchFromRemote: Boolean) {
        if (_managersState.value.isLoading) return

        _managersState.update {
            it.copy(isLoading = true)
        }
        viewModelScope.launch {
            sharedRepository.getPagedManagers(
                page = 1,
                limit = pageSize,
                search = _managersState.value.searchQuery,
                sort = null,
                forceFetchFromRemote = forceFetchFromRemote,
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _managersState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                        Timber.e("Error loading managers: ${resource.message}")
                    }

                    is Resource.Loading -> {
                        _managersState.update { current ->
                            current.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val managers = resource.data.data
                        if (managers != null) {
                            _managersState.update {
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
    }

    private fun loadNextPage() {
        if (!_managersState.value.hasNextPage || _managersState.value.isLoading) return

        _managersState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            sharedRepository.getPagedManagers(
                page = _managersState.value.currentPage + 1,
                limit = pageSize,
                search = _managersState.value.searchQuery,
                sort = null,
                forceFetchFromRemote = false,
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _managersState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _managersState.update { current ->
                            current.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val managers = resource.data.data
                        if (managers != null) {
                            _managersState.update {
                                it.copy(
                                    managers = managers.items,
                                    currentPage = managers.page,
                                    hasNextPage = managers.hasNextPage,
                                    hasPreviousPage = managers.hasPreviousPage,
                                    errorMessage = null,
                                )
                            }
                        } else {
                            _managersState.update {
                                it.copy(
                                    errorMessage = "No managers found",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadPreviousPage() {
        if (!_managersState.value.hasPreviousPage || _managersState.value.isLoading) return

        _managersState.update { it.copy(isLoading = true) }

        val previousPage = _managersState.value.currentPage - 1
        if (previousPage < 1) return

        viewModelScope.launch {
            sharedRepository.getPagedManagers(
                page = previousPage,
                limit = pageSize,
                search = _managersState.value.searchQuery,
                sort = null,
                forceFetchFromRemote = false,
            ).collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        _managersState.update {
                            it.copy(
                                errorMessage = resource.message,
                                isLoading = false
                            )
                        }
                    }

                    is Resource.Loading -> {
                        _managersState.update { current ->
                            current.copy(
                                isLoading = resource.isLoading
                            )
                        }
                    }

                    is Resource.Success -> {
                        val managers = resource.data.data
                        if (managers != null) {
                            _managersState.update {
                                it.copy(
                                    managers = managers.items,
                                    currentPage = managers.page,
                                    hasNextPage = managers.hasNextPage,
                                    hasPreviousPage = managers.hasPreviousPage,
                                    errorMessage = null,
                                    isLoading = false
                                )
                            }
                        } else {
                            _managersState.update {
                                it.copy(
                                    errorMessage = "No managers found",
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun search(query: String) {
        if (_managersState.value.isLoading) return
        _managersState.update { state ->
            state.copy(searchQuery = query.takeIf { it.isNotBlank() })
        }
        loadManager(forceFetchFromRemote = true)
    }

    //Admin functionality
    private fun deleteManager(managerId: UUID){
        _managersState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            when (val result = adminRepository.deleteManager(managerId)){
                is Resource.Error -> {
                    _managersState.update { state ->
                        state.copy(
                            errorMessage = result.message,
                            isLoading = false
                        )
                    }
                }
                is Resource.Loading -> {
                    _managersState.update { state ->
                        state.copy(
                            isLoading = result.isLoading
                        )
                    }
                }

                is Resource.Success -> {
                    _managersState.update { state ->
                        state.copy(
                            managers = state.managers.filter { it.id != managerId },
                            isLoading = false
                        )
                    }
                }
            }
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