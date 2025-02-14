package com.example.taskmanager.auth.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.presentation.event.SignUpManagerUiEvent
import com.example.taskmanager.auth.presentation.state.ManagerSignupUiState
import com.example.taskmanager.auth.utils.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class SignUpManagerViewModel @Inject
constructor(private val repository: AuthRepository) : ViewModel() {

    private val _signUpState = MutableStateFlow(ManagerSignupUiState())
    val signUpState = _signUpState.asStateFlow()

    private var validationJob: Job? = null

    fun onEvent(event: SignUpManagerUiEvent){
        when(event){
            is SignUpManagerUiEvent.Error -> {
                _signUpState.update {
                    it.copy(
                        isLoading = false,
                        error = event.message
                    )
                }
            }
            is SignUpManagerUiEvent.Loading -> {
                _signUpState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }
            is SignUpManagerUiEvent.OnBirthDateChange -> {
                _signUpState.update {
                    it.copy(
                        managerSignupRequest = it.managerSignupRequest.copy(
                            birthDate = event.birthDate
                        )
                    )
                }
            }
            is SignUpManagerUiEvent.OnDepartmentIdChange -> {
                _signUpState.update {
                    it.copy(
                        departmentIdText = event.value,
                        managerSignupRequest = it.managerSignupRequest.copy(
                            departmentId = event.departmentId
                        )
                    )
                }
            }
            is SignUpManagerUiEvent.OnFirstNameChange -> {

                validateName(event.firstName) { isValid, error ->  // CHANGED: Added name validation
                    _signUpState.update {
                        it.copy(
                            managerSignupRequest = it.managerSignupRequest.copy(
                                firstName = event.firstName
                            ),
                            error = if (!isValid) error else null
                        )
                    }
                }
            }
            is SignUpManagerUiEvent.OnGenderChange -> {
                _signUpState.update {
                    it.copy(
                        managerSignupRequest = it.managerSignupRequest.copy(
                            gender = event.gender
                        )
                    )
                }
            }

            is SignUpManagerUiEvent.OnLastNameChange -> {
                validateName(event.lastName) { isValid, error ->  // CHANGED: Added name validation
                    _signUpState.update {
                        it.copy(
                            managerSignupRequest = it.managerSignupRequest.copy(
                                lastName = event.lastName
                            ),
                            error = if (!isValid) error else null
                        )
                    }
                }
            }
            is SignUpManagerUiEvent.OnOtpChange -> {
                _signUpState.update {
                    it.copy(
                        managerSignupRequest = it.managerSignupRequest.copy(
                            otpEmailVerifyCode = event.otp
                        )
                    )
                }
            }
            is SignUpManagerUiEvent.OnPasswordChange -> {
                validationJob?.cancel()
                validationJob = viewModelScope.launch {
                    delay(500)
                    validatePassword(event.password) { isValid, error ->
                        _signUpState.update {
                            it.copy(
                                managerSignupRequest = it.managerSignupRequest.copy(
                                    password = event.password
                                ),
                                error = if (!isValid && event.password.isNotBlank()) error else null
                            )
                        }
                    }
                }
            }
            is SignUpManagerUiEvent.OnPhoneNumberChange -> {
                validationJob?.cancel()
                validationJob = viewModelScope.launch {
                    delay(500)
                    validatePhoneNumber(event.phoneNumber) { isValid, error ->  // CHANGED: Added phone validation
                        _signUpState.update {
                            it.copy(
                                managerSignupRequest = it.managerSignupRequest.copy(
                                    phoneNumber = event.phoneNumber
                                ),
                                error = if (!isValid) error else null
                            )
                        }
                    }
                }
            }
            is SignUpManagerUiEvent.OnUsernameChange -> {
                validationJob?.cancel()
                validationJob = viewModelScope.launch {
                    delay(500)
                    val isValid = isValidEmail(event.username)
                    _signUpState.update {
                        it.copy(
                            managerSignupRequest = it.managerSignupRequest.copy(
                                username = event.username
                            ),
                            error = if (!isValid && event.username.isNotBlank())
                                "Please enter a valid email address"
                            else null
                        )
                    }
                }
            }
            is SignUpManagerUiEvent.OnVerifyEmail -> {
                verifyEmail(event.verificationRequestDto)
            }
            is SignUpManagerUiEvent.SignUpManager -> {
                signUpManager(event.managerSignupRequest)
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"""
        return email.matches(emailPattern.toRegex())
    }

    private fun validatePassword(password: String, callback: (Boolean, String) -> Unit) {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { it in "@#$!%*?&^(){}\\[]<>_+=|\\~`:;,./\\-" }
        val isLongEnough = password.length >= 10

        when {
            !isLongEnough -> callback(false, "Password must be at least 10 characters long")
            !hasUpperCase -> callback(false, "Password must contain at least one uppercase letter")
            !hasLowerCase -> callback(false, "Password must contain at least one lowercase letter")
            !hasDigit -> callback(false, "Password must contain at least one number")
            !hasSpecialChar -> callback(
                false,
                "Password must contain at least one special character"
            )

            else -> callback(true, "")
        }
    }

    private fun validateName(name: String, callback: (Boolean, String) -> Unit) {
        if (name.isBlank()) {
            callback(false, "Name cannot be empty")
            return
        }
        if (!name.matches("""^[a-zA-Z\s-]{2,30}$""".toRegex())) {
            callback(
                false,
                "Name should only contain letters, spaces, and hyphens (2-30 characters)"
            )
            return
        }
        callback(true, "")
    }

    private fun validatePhoneNumber(phone: String, callback: (Boolean, String) -> Unit) {
        if (!phone.matches("""^\+?[0-9]{10,15}$""".toRegex())) {
            callback(false, "Please enter a valid phone number (10-15 digits)")
            return
        }
        callback(true, "")
    }


    private fun verifyEmail(verificationRequestDto: VerificationRequestDto) {
        viewModelScope.launch {
            try {
                _signUpState.update { it.copy(isLoading = true, error = null) }

                when (val result = repository.verifyEmail(verificationRequestDto)) {
                    is AuthResult.Authenticated -> {
                        Log.d("SignUpManagerViewModel", "Authentication successful")
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true
                            )
                        }
                    }

                    is AuthResult.UnAuthenticated -> {
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                error = result.message ?: "Authentication failed"
                            )
                        }
                    }

                    is AuthResult.UnknownError -> {
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                error = result.message ?: "An unknown error occurred"
                            )
                        }
                    }

                    is AuthResult.SignedOut -> {
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _signUpState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }

    private fun signUpManager(managerSignupRequest: ManagerSignupRequest) {
        viewModelScope.launch {
            try {
                _signUpState.update { it.copy(isLoading = true, error = null) }

                when (val result = repository.signupManager(managerSignupRequest)) {
                    is AuthResult.Authenticated -> {
                        Log.d("SignUpManagerViewModel", "Authentication successful")
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                error = null
                            )
                        }
                    }

                    is AuthResult.UnAuthenticated -> {
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                error = result.message ?: "Authentication failed"
                            )
                        }
                    }

                    is AuthResult.UnknownError -> {
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                error = result.message ?: "An unknown error occurred"
                            )
                        }
                    }

                    is AuthResult.SignedOut -> {
                        _signUpState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = false,
                                error = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _signUpState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unexpected error occurred"
                    )
                }
            }
        }
    }
}