package com.example.taskmanager.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.presentation.event.LoginUiEvent
import com.example.taskmanager.auth.presentation.state.LoginUiState
import com.example.taskmanager.auth.utils.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()


    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.Error -> {
                _loginState.value = _loginState.value.copy(
                    isLoading = false,
                    error = event.message
                )
            }

            is LoginUiEvent.Loading -> {
                _loginState.value = _loginState.value.copy(
                    isLoading = true,
                    error = event.message
                )
            }

            is LoginUiEvent.Login -> {
                login(event.loginRequest)
            }

        }
    }

    private fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {
            val result = repository.loginUser(loginRequest)
            _loginState.value = _loginState.value.copy(
                isLoading = false,
                error = null
            )

            when (result) {
                is AuthResult.Authenticated -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        error = null
                    )
                }

                is AuthResult.UnAuthenticated -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = result.message ?: "Authentication failed"
                    )
                }

                is AuthResult.UnknownError -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isAuthenticated = false,
                        error = result.message ?: "An unknown error occurred"
                    )
                }

                is AuthResult.Loading -> {
                    _loginState.value = _loginState.value.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
        }
    }

}