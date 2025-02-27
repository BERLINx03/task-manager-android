package com.example.taskmanager.auth.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.admin.domain.model.Admin
import com.example.taskmanager.admin.domain.repository.AdminRepository
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.auth.data.remote.requestmodels.ForgotPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ResetPasswordRequestDto
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.presentation.event.LoginUiEvent
import com.example.taskmanager.auth.presentation.state.LoginUiState
import com.example.taskmanager.auth.utils.AuthResult
import com.example.taskmanager.core.data.local.database.TaskManagerDatabase
import com.example.taskmanager.core.data.local.datastore.StatisticsDataStore
import com.example.taskmanager.core.data.local.datastore.UserInfoDataStore
import com.example.taskmanager.core.domain.model.User
import com.example.taskmanager.core.domain.repository.SharedRepository
import com.example.taskmanager.core.utils.Resource
import com.example.taskmanager.core.utils.Screens
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val adminRepository: AdminRepository,
    private val tokenDataStore: TokenDataStore,
    private val userInfoDataStore: UserInfoDataStore,
    private val taskManagerDatabase: TaskManagerDatabase,
    private val sharedRepository: SharedRepository,
    private val statisticsDataStore: StatisticsDataStore
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState = _loginState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    var isCheckingAuth = mutableStateOf(true)
        private set

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
                        val admin = adminRepository.getCurrentAdmin()
                        if (admin is Resource.Success) {
                            Timber.i("${admin.data.firstName} has been added to datastore")
                            userInfoDataStore.saveUserInfo(admin.data.toUser())
                        } else if (admin is Resource.Error) {
                            Timber.e("admin data didn't get saved")
                        }

                    }

                    "Manager" -> {
                        _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                        val manager = sharedRepository.getCurrentManager()
                        if (manager is Resource.Success) {
                            Timber.i("${manager.data.firstName} has been added to datastore")
                            userInfoDataStore.saveUserInfo(manager.data)
                        } else if (manager is Resource.Error) {
                            Timber.e("manager data didn't get saved")
                        }
                    }

                    "Employee" -> {
                        _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                        val employee = sharedRepository.getCurrentEmployee()
                        if (employee is Resource.Success) {
                            Timber.i("${employee.data.firstName} has been added to datastore")
                            userInfoDataStore.saveUserInfo(employee.data)
                        }
                    }
                }
            } else {
                _navigationEvent.emit(Screens.AuthScreens.Login.route)
            }

            delay(400)
            isCheckingAuth.value = false
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

            when (val result = withContext(Dispatchers.IO) {
                repository.resetPassword(resetPasswordRequest)
            }) {
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
            when (val result = withContext(Dispatchers.IO) {
                repository.forgetPassword(email)
            }) {
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
            when (val result = withContext(Dispatchers.IO) {
                repository.loginUser(loginRequest)
            }) {
                is AuthResult.Authenticated -> {
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = true,
                            error = null,
                            role = result.data?.data?.role ?: ""
                        )
                    }

                    when (result.data?.data?.role) {
                        "Admin" -> _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                        "Manager" -> _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                        "Employee" -> _navigationEvent.emit(Screens.AppScreens.Dashboard.route)
                    }
                }

                is AuthResult.UnAuthenticated -> {
                    Timber.d("UnAuthenticated" + result.message)
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            isAuthenticated = false,
                            error = result.message ?: "Authentication failed"
                        )
                    }
                }

                is AuthResult.UnknownError -> {
                    Timber.d("Unknown Error" + result.message)
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
        viewModelScope.launch(Dispatchers.IO) {
            taskManagerDatabase.clearAllTables()
            statisticsDataStore.clear()
            userInfoDataStore.clearUserInfo()
            tokenDataStore.clearToken()
            tokenDataStore.saveAuthState(false)
            tokenDataStore.saveUserRole("")
        }
    }
}

fun Admin.toUser(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        birthDate = birthDate,
        phoneNumber = phoneNumber,
        gender = gender
    )
}