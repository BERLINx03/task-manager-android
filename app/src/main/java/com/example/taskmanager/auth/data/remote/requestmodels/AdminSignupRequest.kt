package com.example.taskmanager.auth.data.remote.requestmodels

data class AdminSignupRequest(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
    val username: String,
    val password: String
)
