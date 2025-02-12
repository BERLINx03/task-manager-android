package com.example.taskmanager.auth.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.presentation.event.SignUpEmployeeUiEvent
import com.example.taskmanager.auth.presentation.state.EmployeeSignupUiState
import com.example.taskmanager.auth.utils.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author Abdallah Elsokkary
 */
@HiltViewModel
class SignUpEmployeeViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _signUpState = MutableStateFlow(EmployeeSignupUiState())
    val signUpState = _signUpState.asStateFlow()

    fun onEvent(event: SignUpEmployeeUiEvent) {
        when (event) {
            is SignUpEmployeeUiEvent.Error -> {
                _signUpState.update {
                    it.copy(
                        isLoading = false,
                        error = event.message
                    )
                }
            }

            is SignUpEmployeeUiEvent.Loading -> {
                _signUpState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnBirthDateChange -> {
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            birthDate = event.birthDate
                        )
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnDepartmentIdChange -> {
                _signUpState.update {
                    it.copy(
                        departmentIdText = event.value,
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            departmentId = event.departmentId
                        )
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnFirstNameChange -> {
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            firstName = event.firstName
                        )
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnGenderChange ->
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            gender = event.gender
                        )
                    )
                }

            is SignUpEmployeeUiEvent.OnLastNameChange -> {
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            lastName = event.lastName
                        )
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnPhoneNumberChange -> {
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            phoneNumber = event.phoneNumber
                        )
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnUsernameChange -> {
                val isValid = isValidEmail(event.username)
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            username = event.username
                        ),
                        error = if (!isValid && event.username.isNotBlank())
                            "Please enter a valid email address"
                        else null
                    )
                }
            }

            is SignUpEmployeeUiEvent.OnPasswordChange -> {
                val isValid = isValidPassword(event.password)
                _signUpState.update {
                    it.copy(
                        employeeSignupRequest = it.employeeSignupRequest.copy(
                            password = event.password
                        ),
                        error = if (!isValid && event.password.isNotBlank())
                            "Password must be at least 10 characters and contain uppercase, lowercase, number, and special character"
                        else null
                    )
                }
            }

            is SignUpEmployeeUiEvent.SignUpEmployee -> {
                viewModelScope.launch {
                    signUpEmployee(event.employeeSignupRequest)
                }
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        return password.matches("""^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@#$!%*?&^(){}\[\]<>_+=|\\~`:;,./\-])[A-Za-z\d@$!%*?&^(){}\[\]<>_+=|\\~`:;,./\-]{10,}$""".toRegex())
    }

    private fun signUpEmployee(employeeSignupRequest: EmployeeSignupRequest) {
        viewModelScope.launch {
            val result = repository.signupEmployee(employeeSignupRequest)
            _signUpState.update {
                it.copy(
                    isLoading = false,
                    error = null
                )
            }

            when (result) {
                is AuthResult.Authenticated -> {
                    Log.d("SignUpEmployeeViewModel", "Authentication successful")
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

                is AuthResult.Loading -> {
                    _signUpState.update {
                        it.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                }
            }
        }
    }
}