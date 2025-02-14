package com.example.taskmanager.auth.presentation.event

import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed class SignUpManagerUiEvent {
    data class Loading(val message: String?) : SignUpManagerUiEvent()
    data class OnOtpChange(val otp: String) : SignUpManagerUiEvent()
    data class OnVerifyEmail(val verificationRequestDto: VerificationRequestDto) : SignUpManagerUiEvent()
    data class SignUpManager(val managerSignupRequest: ManagerSignupRequest) : SignUpManagerUiEvent()
    data class OnFirstNameChange(val firstName: String) : SignUpManagerUiEvent()
    data class OnLastNameChange(val lastName: String) : SignUpManagerUiEvent()
    data class OnPhoneNumberChange(val phoneNumber: String) : SignUpManagerUiEvent()
    data class OnGenderChange(val gender: Int) : SignUpManagerUiEvent()
    data class OnBirthDateChange(val birthDate: String) : SignUpManagerUiEvent()
    data class OnDepartmentIdChange(val value: String, val departmentId: UUID) : SignUpManagerUiEvent()
    data class OnUsernameChange(val username: String) : SignUpManagerUiEvent()
    data class OnPasswordChange(val password: String) : SignUpManagerUiEvent()
    data class Error(val message: String) : SignUpManagerUiEvent()
}