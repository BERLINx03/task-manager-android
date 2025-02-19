package com.example.taskmanager.admin.data.remote.dto

data class UpdateAdminRequestDto(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
)
