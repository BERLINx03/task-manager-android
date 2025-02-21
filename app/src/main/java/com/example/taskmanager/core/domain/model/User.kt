package com.example.taskmanager.core.domain.model

import java.util.UUID

data class User(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val gender: Int,
    val birthDate: String,
) {
    companion object {
        val Empty = User(
            id = UUID.randomUUID(),
            firstName = "",
            lastName = "",
            phoneNumber = "",
            gender = 0,
            birthDate = ""
        )
    }
}
