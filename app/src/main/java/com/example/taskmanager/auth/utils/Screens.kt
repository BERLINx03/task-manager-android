package com.example.taskmanager.auth.utils

/**
 * @author Abdallah Elsokkary
 */
sealed class Screens(val route: String) {
    sealed class AuthScreens(route: String) : Screens(route) {
        data object Login : AuthScreens("login")

        sealed class SignUp(route: String) : AuthScreens(route) {
            data object Employee : SignUp("signup/employee")
            data object Manager : SignUp("signup/manager")
            data object Admin : SignUp("signup/admin")
        }

        data object VerifyOtp : AuthScreens("verify_otp")

        data object ChooseRole : AuthScreens("choose_role")
    }
}