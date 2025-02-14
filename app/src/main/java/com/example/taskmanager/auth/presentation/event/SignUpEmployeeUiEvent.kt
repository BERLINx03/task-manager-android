package com.example.taskmanager.auth.presentation.event

import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
sealed class SignUpEmployeeUiEvent{
    data class Loading(val message: String?) : SignUpEmployeeUiEvent()
    data class OnOtpChange(val otp: String) : SignUpEmployeeUiEvent()
    data class OnVerifyEmail(val verificationRequestDto: VerificationRequestDto) : SignUpEmployeeUiEvent()
    data class SignUpEmployee(val employeeSignupRequest: EmployeeSignupRequest) : SignUpEmployeeUiEvent()
    data class OnFirstNameChange(val firstName: String) : SignUpEmployeeUiEvent()
    data class OnLastNameChange(val lastName: String) : SignUpEmployeeUiEvent()
    data class OnPhoneNumberChange(val phoneNumber: String) : SignUpEmployeeUiEvent()
    data class OnGenderChange(val gender: Int) : SignUpEmployeeUiEvent()
    data class OnBirthDateChange(val birthDate: String) : SignUpEmployeeUiEvent()
    data class OnDepartmentIdChange(val value: String, val departmentId: UUID) : SignUpEmployeeUiEvent()
    data class OnUsernameChange(val username: String) : SignUpEmployeeUiEvent()
    data class OnPasswordChange(val password: String) : SignUpEmployeeUiEvent()
    data class Error(val message: String) : SignUpEmployeeUiEvent()
}