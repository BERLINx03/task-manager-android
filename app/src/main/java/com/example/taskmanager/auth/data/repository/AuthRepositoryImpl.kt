package com.example.taskmanager.auth.data.repository

import android.util.Log
import com.example.taskmanager.auth.data.local.TokenDataStore
import com.example.taskmanager.auth.data.remote.AuthApi
import com.example.taskmanager.auth.data.remote.reponsemodels.ResponseDto
import com.example.taskmanager.auth.data.remote.requestmodels.AdminSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.EmployeeSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ForgotPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.LoginRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ManagerSignupRequest
import com.example.taskmanager.auth.data.remote.requestmodels.ResetPasswordRequestDto
import com.example.taskmanager.auth.data.remote.requestmodels.VerificationRequestDto
import com.example.taskmanager.auth.domain.repository.AuthRepository
import com.example.taskmanager.auth.utils.AuthResult
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import java.net.HttpURLConnection

/**
 * @author Abdallah Elsokkary
 */
const val AUTH_REPOSITORY_TAG = "AuthRepository"
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {


    override suspend fun loginUser(loginRequest: LoginRequest): AuthResult<ResponseDto<ResponseDto.LoginData>> {
        return try {
            val response = api.loginUser(loginRequest)

            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    val token = response.data?.token?.accessToken
                    if (token.isNullOrEmpty()) {
                        AuthResult.UnknownError("Empty token received")
                    } else {
                        try {
                            tokenDataStore.saveAuthState(true)
                            tokenDataStore.saveToken(token)
                            AuthResult.Authenticated(response)
                        } catch (e: Exception) {
                            AuthResult.UnknownError("Failed to save authentication token")
                        }
                    }
                }

                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)

                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")
                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")

                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }
        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }

    override suspend fun signupEmployee(employee: EmployeeSignupRequest): AuthResult<ResponseDto<String>> {
        return try {
            val response = api.signUpEmployee(employee)
            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    Log.d(AUTH_REPOSITORY_TAG,"Employee signup successful")
                    AuthResult.Authenticated(response)
                }

                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)

                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")
                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")

                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }
        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }


    override suspend fun signupManager(manager: ManagerSignupRequest): AuthResult<ResponseDto<String>> {
        return try {
            val response = api.signUpManager(manager)
            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    Log.d(AUTH_REPOSITORY_TAG,"Manager signup successful")

                    AuthResult.Authenticated(response)
                }

                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)

                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")
                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")

                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }
        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }

    override suspend fun signupAdmin(admin: AdminSignupRequest): AuthResult<ResponseDto<String>> {
        return try {
            val response = api.signUpAdmin(admin)
            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    Log.d(AUTH_REPOSITORY_TAG,"Admin signup successful")
                    AuthResult.Authenticated(response)
                }

                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)

                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")
                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")

                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }
        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }

    override suspend fun verifyEmail(verification: VerificationRequestDto): AuthResult<ResponseDto<String>> {
        return try {
            val response = api.verifyEmail(verification)
            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    Log.d(AUTH_REPOSITORY_TAG,"Email verification successful")
                    AuthResult.Authenticated(response)
                }
                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)
                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")
                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")

                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }

        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }

    override suspend fun forgetPassword(forgetPassword: ForgotPasswordRequestDto): AuthResult<ResponseDto<String>> {
        return try {
            val response = api.forgetPassword(forgetPassword)
            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    Log.d(AUTH_REPOSITORY_TAG,"Forget password successful")
                    AuthResult.Authenticated(response)
                }
                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)

                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")

                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")
                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }
        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }

    override suspend fun resetPassword(resetPassword: ResetPasswordRequestDto): AuthResult<ResponseDto<String>> {
        return try {
            val response = api.resetPassword(resetPassword)
            when (response.statusCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    Log.d(AUTH_REPOSITORY_TAG,"Reset password successful")
                    AuthResult.Authenticated(response)
                }
                HttpURLConnection.HTTP_BAD_REQUEST,
                HttpURLConnection.HTTP_CONFLICT -> AuthResult.UnknownError(response.message)
                HttpURLConnection.HTTP_UNAUTHORIZED -> AuthResult.UnAuthenticated()
                HttpURLConnection.HTTP_FORBIDDEN -> AuthResult.UnknownError("Access Denied")
                HttpURLConnection.HTTP_NOT_FOUND -> AuthResult.UnknownError("Not Found")

                HttpURLConnection.HTTP_INTERNAL_ERROR -> AuthResult.UnknownError("Server Error")
                else -> {
                    Log.e(AUTH_REPOSITORY_TAG, "Unexpected response code: ${response.statusCode}")
                    AuthResult.UnknownError("Unexpected Error: ${response.statusCode}")
                }
            }
        } catch (e: IOException) {
            Log.e(AUTH_REPOSITORY_TAG, "Network Failure", e)
            AuthResult.UnknownError("Network Failure: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Log.e(AUTH_REPOSITORY_TAG, "HTTP Error", e)
            AuthResult.UnknownError("HTTP Error: ${e.message()}")
        } catch (e: Exception) {
            Log.e(AUTH_REPOSITORY_TAG, "Unknown Error", e)
            AuthResult.UnknownError("Unknown Error: ${e.localizedMessage}")
        }
    }
}
