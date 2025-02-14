package com.example.taskmanager.auth.presentation.event

import com.example.taskmanager.auth.data.remote.requestmodels.AdminSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto

sealed class SignUpAdminUiEvent{
    data class Loading(val message: String?) : SignUpAdminUiEvent()
    data class OnOtpChange(val otp: String) : SignUpAdminUiEvent()
    data class OnVerifyEmail(val verificationRequestDto: VerificationRequestDto) : SignUpAdminUiEvent()
    data class SignUpAdmin(val adminSignupRequest: AdminSignupRequest) : SignUpAdminUiEvent()
    data class OnUsernameChange(val username: String) : SignUpAdminUiEvent()
    data class OnPasswordChange(val password: String) : SignUpAdminUiEvent()
    data class Error(val message: String) : SignUpAdminUiEvent()
    data class OnFirstNameChange(val firstName: String) : SignUpAdminUiEvent()
    data class OnLastNameChange(val lastName: String) : SignUpAdminUiEvent()
    data class OnPhoneNumberChange(val phoneNumber: String) : SignUpAdminUiEvent()
    data class OnGenderChange(val gender: Int) : SignUpAdminUiEvent()
    data class OnBirthDateChange(val birthDate: String) : SignUpAdminUiEvent()
}
