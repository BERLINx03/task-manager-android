package com.example.taskmanager.auth.utils

/**
 * @author Abdallah Elsokkary
 */
sealed class Screens(val route: String) {

    sealed class AuthScreens(route: String) : Screens(route) {

        data object Login : AuthScreens("login")
        data object ResetPassword : AuthScreens("reset_password")

        sealed class SignUp(route: String) : AuthScreens(route) {
            data object Employee : SignUp("signup/employee")
            data object Manager : SignUp("signup/manager")
            data object Admin : SignUp("signup/admin")
        }

        sealed class VerifyOtp(route: String) : AuthScreens(route){
            data object Employee : VerifyOtp("employee")
            data object Manager : VerifyOtp("manager")
            data object Admin : VerifyOtp("admin")
        }

        data object ChooseRole : AuthScreens("choose_role")
    }
}