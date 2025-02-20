package com.example.taskmanager.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.auth.data.remote.requestmodels.ForgotPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ResetPasswordRequestDto
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.presentation.event.LoginUiEvent
import com.example.taskmanager.auth.presentation.state.LoginUiState
import com.example.taskmanager.auth.utils.AuthResult
import com.example.taskmanager.auth.utils.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _isCheckingAuth = MutableStateFlow(true)
    val isCheckingAuth= _isCheckingAuth.asStateFlow()
    init {
        viewModelScope.launch {
            val authState = tokenDataStore.authState.firstOrNull() ?: false
            val role = tokenDataStore.userRole.firstOrNull() ?: ""
            _loginState.update {
                it.copy(
                    isAuthenticated = authState,
                    role = role
                )
            }

            if (authState) {
                when (role) {
                    "Admin" -> {
                        _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                    }

                    "Manager" -> {
                        //TODO
                    }

                    "Employee" -> {
                        //TODO
                    }
                }
            } else {
                _navigationEvent.emit(Screens.AuthScreens.Login.route)
            }

            delay(400)
            _isCheckingAuth.update { false }
        }
    }

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.Error -> {
                _loginState.update {
                    it.copy(
                        error = event.message,
                        isLoading = false
                    )
                }
            }

            is LoginUiEvent.Loading -> {
                _loginState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }

            is LoginUiEvent.OnUsernameChange -> {
                _loginState.update { currentState ->
                    currentState.copy(
                        loginRequest = currentState.loginRequest.copy(
                            username = event.username
                        )
                    )
                }

            }

            is LoginUiEvent.OnPasswordChange -> {
                _loginState.update { currentState ->
                    currentState.copy(
                        loginRequest = currentState.loginRequest.copy(
                            password = event.password
                        )
                    )
                }
            }

            is LoginUiEvent.Login -> {
                login(event.loginRequest)
            }

            is LoginUiEvent.ForgotPassword -> {
                forgetPassword(event.forgotPasswordRequest)
            }

            is LoginUiEvent.ResetPassword -> {
                resetPassword(event.resetPasswordRequest)
            }

            is LoginUiEvent.OnOtpChange -> {
                _loginState.update { currentState ->
                    currentState.copy(
                        otp = event.otp
                    )
                }
            }

            is LoginUiEvent.OnNewPasswordChange -> {
                _loginState.update { currentState ->
                    currentState.copy(
                        newPassword = event.newPassword
                    )
                }
            }

            LoginUiEvent.Logout -> {
                logout()
            }
        }
    }


    private fun resetPassword(resetPasswordRequest: ResetPasswordRequestDto) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(
                isLoading = true,
                error = null
            )

            when (val result = repository.resetPassword(resetPasswordRequest)) {
                is AuthResult.Authenticated -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
                        )
                    }
                }

                is AuthResult.SignedOut -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = null
                        )
                    }
                }

                is AuthResult.UnAuthenticated -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "Authentication failed"
                        )
                    }
                }

                is AuthResult.UnknownError -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "An unknown error occurred"
                        )
                    }
                }
            }
        }
    }

    private fun forgetPassword(email: ForgotPasswordRequestDto) {
        viewModelScope.launch {
            _loginState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }
            when (val result = repository.forgetPassword(email)) {
                is AuthResult.Authenticated -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null
                        )
                    }
                }

                is AuthResult.SignedOut -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = null
                        )
                    }
                }

                is AuthResult.UnAuthenticated -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "Authentication failed"
                        )
                    }
                }

                is AuthResult.UnknownError -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "An unknown error occurred"
                        )
                    }
                }
            }
        }
    }

    private fun login(loginRequest: LoginRequest) {
        viewModelScope.launch {

            _loginState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            when (val result = repository.loginUser(loginRequest)) {
                is AuthResult.Authenticated -> {
                    tokenDataStore.saveAuthState(true)
                    tokenDataStore.saveUserRole(result.data?.data?.role ?: "")
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null,
                            role = result.data?.data?.role ?: ""
                        )
                    }

                    if (result.data?.data?.role == "Admin") {
                        _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                    } else if (result.data?.data?.role == "Manager") {
                        //TODO
                    } else if (result.data?.data?.role == "Employee") {
                        //TODO
                    }
                }

                is AuthResult.UnAuthenticated -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "Authentication failed"
                        )
                    }
                }

                is AuthResult.UnknownError -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "An unknown error occurred"
                        )
                    }
                }

                is AuthResult.SignedOut -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = null,
                            role = ""
                        )
                    }
                }
            }
        }
    }

    private fun logout() {
        _loginState.update {
            it.copy(
                isAuthenticated = false
            )
        }
        viewModelScope.launch {
            tokenDataStore.clearToken()
            tokenDataStore.saveAuthState(false)
            tokenDataStore.saveUserRole("")
        }
    }
}