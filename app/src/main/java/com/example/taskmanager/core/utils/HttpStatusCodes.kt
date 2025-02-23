package com.example.taskmanager.core.utils

import java.net.HttpURLConnection

object HttpStatusCodes {
    const val OK = HttpURLConnection.HTTP_OK
    const val CREATED = HttpURLConnection.HTTP_CREATED
    const val BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST
    const val HTTP_CONFLICT = HttpURLConnection.HTTP_CONFLICT
    const val UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED
    const val FORBIDDEN = HttpURLConnection.HTTP_FORBIDDEN
    const val NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND
}