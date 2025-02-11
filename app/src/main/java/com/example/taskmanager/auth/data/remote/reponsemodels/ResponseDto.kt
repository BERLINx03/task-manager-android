package com.example.taskmanager.auth.data.remote.reponsemodels

/**
 * @author Abdallah Elsokkary
 */
data class ResponseDto<T>(
    val isSuccess: Boolean,
    val message: String,
    val errors: List<String>?,
    val data: T?,
    val statusCode: Int
){
    data class LoginData(
        val token: TokenInfo
    )

    data class TokenInfo(
        val accessToken: String
    )
}