package com.example.taskmanager.core.utils

import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object HttpStatusCodes {
    const val OK = HttpURLConnection.HTTP_OK
    const val CREATED = HttpURLConnection.HTTP_CREATED
    const val BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST
    const val HTTP_CONFLICT = HttpURLConnection.HTTP_CONFLICT
    const val UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED
    const val FORBIDDEN = HttpURLConnection.HTTP_FORBIDDEN
    const val NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND
}
fun getUserFriendlyMessage(statusCode: Int): String {
    return when (statusCode) {
        HttpURLConnection.HTTP_OK -> "Success! Your request was processed successfully."

        HttpURLConnection.HTTP_CREATED -> "Great! The resource was created successfully."

        HttpURLConnection.HTTP_BAD_REQUEST -> "There seems to be an issue with your request. Please check the information you've provided and try again."

        HttpURLConnection.HTTP_CONFLICT -> "We couldn't complete this action because it conflicts with the current state of the resource. This might happen if someone else made changes at the same time."

        HttpURLConnection.HTTP_UNAUTHORIZED -> "You need to be signed in to access this feature. Please sign in and try again."

        HttpURLConnection.HTTP_FORBIDDEN -> "Sorry, you don't have permission to access this feature. Please contact support if you believe this is an error."

        HttpURLConnection.HTTP_NOT_FOUND -> "We couldn't find what you're looking for. The item may have been moved or removed."

        HttpURLConnection.HTTP_INTERNAL_ERROR -> "We're experiencing technical difficulties on our end. Please try again later."

        HttpURLConnection.HTTP_UNAVAILABLE -> "Our service is temporarily unavailable. We're working to restore it as quickly as possible."

        HttpURLConnection.HTTP_GATEWAY_TIMEOUT -> "The request took too long to process. Please check your connection and try again."

        HttpURLConnection.HTTP_NOT_IMPLEMENTED -> "This feature isn't available yet. We're still working on it."

        429 -> "You've made too many requests in a short time. Please wait a moment before trying again."

        else -> "Something went wrong. Please try again later or contact support if the problem persists."
    }
}

fun getIOExceptionMessage(exception: IOException): String {
    return when (exception) {
        is FileNotFoundException -> "We couldn't find the file you're looking for. It may have been moved or deleted."

        is SocketTimeoutException -> "The connection timed out. Please check your internet connection and try again."

        is ConnectException -> "We couldn't connect to our servers. Please check your internet connection and try again later."

        is UnknownHostException -> "We couldn't reach our servers. Please check your internet connection and try again."

        is SocketException -> "Your connection was interrupted. Please check your internet connection and try again."

        else -> "We encountered a problem accessing the required resources. Please try again later."
    }
}
