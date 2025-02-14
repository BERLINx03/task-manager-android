package com.example.taskmanager.auth.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmanager.auth.data.remote.requestmodels.AdminSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.presentation.event.SignUpAdminUiEvent
import com.example.taskmanager.auth.presentation.state.AdminSignupUiState
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
class SignUpAdminViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _signUpState = MutableStateFlow(AdminSignupUiState())
    val signUpState = _signUpState.asStateFlow()


    fun onEvent(event: SignUpAdminUiEvent) {
        when (event) {
            is SignUpAdminUiEvent.Error -> {
                _signUpState.update {
                    it.copy(
                        isLoading = false,
                        error = event.message
                    )
                }
            }

            is SignUpAdminUiEvent.Loading -> {
                _signUpState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }
            }

            is SignUpAdminUiEvent.OnBirthDateChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            birthDate = event.birthDate
                        )
                    )
                }
            }

            is SignUpAdminUiEvent.OnFirstNameChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            firstName = event.firstName
                        )
                    )

                }
            }

            is SignUpAdminUiEvent.OnGenderChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            gender = event.gender
                        )
                    )
                }
            }

            is SignUpAdminUiEvent.OnLastNameChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            lastName = event.lastName
                        )
                    )
                }

            }

            is SignUpAdminUiEvent.OnOtpChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            otpEmailVerifyCode = event.otp
                        )
                    )
                }
            }

            is SignUpAdminUiEvent.OnPasswordChange -> {


                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            password = event.password
                        )
                    )
                }


            }

            is SignUpAdminUiEvent.OnPhoneNumberChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            phoneNumber = event.phoneNumber
                        )
                    )
                }
            }

            is SignUpAdminUiEvent.OnUsernameChange -> {
                _signUpState.update {
                    it.copy(
                        adminSignupRequest = it.adminSignupRequest.copy(
                            username = event.username.trim()
                        )
                    )
                }

            }

            is SignUpAdminUiEvent.OnVerifyEmail -> verifyEmail(event.verificationRequestDto)
            is SignUpAdminUiEvent.SignUpAdmin -> signUpAdmin(event.adminSignupRequest)
        }
    }


    private fun verifyEmail(verificationRequestDto: VerificationRequestDto) {
        viewModelScope.launch {
            try {
                _signUpState.update { it.copy(isLoading = true, error = null) }

                Log.d("SignUpAdminViewModel", _signUpState.value.toString())

                when (val result = repository.verifyEmail(verificationRequestDto)) {
                    is AuthResult.Authenticated -> {
                        Log.d("SignUpAdminViewModel", "Authentication successful")
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

    private fun signUpAdmin(adminSignupRequest: AdminSignupRequest) {
        viewModelScope.launch {
            try {
                _signUpState.update { it.copy(isLoading = true, error = null) }

                when (val result = repository.signupAdmin(adminSignupRequest)) {
                    is AuthResult.Authenticated -> {
                        Log.d("SignUpAdminViewModel", "Authentication successful")
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