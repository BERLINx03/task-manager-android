package com.example.taskmanager.core.utils

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

    sealed class AppScreens(route: String) : Screens(route) {
        data object Dashboard : AppScreens("dashboard")
        data object Departments : AppScreens("departments")
        data object DepartmentDetails : AppScreens("departments/{departmentId}")
        data object Managers : AppScreens("managers")
        data object Employees : AppScreens("employees")
        data object Profile : AppScreens("profile/{role}/{userId}")
        data object CurrentUserProfile: AppScreens("my_profile?isCurrent={isCurrent}")
        data object Tasks : AppScreens("tasks/{managerId}")
        data object TaskDetails: AppScreens("task_details/{role}/{taskId}")
        data object AddTask: AppScreens("add_task/{managerId}") {
            fun createRoute(managerId: String): String {
                return "add_task/$managerId"
            }
        }
        data object EditTask: AppScreens("TaskDetailsFlow/edit_task/{operationType}"){
            fun createRoute(operationType: String): String {
                return "TaskDetailsFlow/edit_task/$operationType"
            }
        }
        data object Settings: AppScreens("settings")
    }
}