package com.example.taskmanager.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskmanager.auth.presentation.view.LoginScreen
import com.example.taskmanager.auth.presentation.view.RoleSelectionScreen
import com.example.taskmanager.auth.presentation.view.SignUpAdminScreen
import com.example.taskmanager.auth.presentation.view.SignUpEmployeeScreen
import com.example.taskmanager.auth.presentation.view.SignUpManagerScreen
import com.example.taskmanager.auth.presentation.view.verification.OtpAdminVerificationScreen
import com.example.taskmanager.auth.presentation.view.verification.OtpEmployeeVerificationScreen
import com.example.taskmanager.auth.presentation.view.verification.OtpManagerVerificationScreen
import com.example.taskmanager.auth.presentation.view.verification.ResetPasswordScreen
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.auth.presentation.viewmodel.SignUpAdminViewModel
import com.example.taskmanager.auth.presentation.viewmodel.SignUpEmployeeViewModel
import com.example.taskmanager.auth.presentation.viewmodel.SignUpManagerViewModel
import com.example.taskmanager.core.presentation.view.DashboardScreen
import com.example.taskmanager.core.presentation.view.DepartmentDetailsScreen
import com.example.taskmanager.core.presentation.view.DepartmentsScreen
import com.example.taskmanager.core.presentation.view.EmployeesScreen
import com.example.taskmanager.core.presentation.view.ManagersScreen
import com.example.taskmanager.core.presentation.view.ProfileScreen
import com.example.taskmanager.core.presentation.view.SettingsScreen
import com.example.taskmanager.core.presentation.view.TaskDetailsScreen
import com.example.taskmanager.core.presentation.view.TasksScreen
import com.example.taskmanager.core.presentation.viewmodel.DashboardViewModel
import com.example.taskmanager.core.presentation.viewmodel.DepartmentDetailsViewModel
import com.example.taskmanager.core.presentation.viewmodel.DepartmentsViewModel
import com.example.taskmanager.core.presentation.viewmodel.EmployeesViewModel
import com.example.taskmanager.core.presentation.viewmodel.LanguageViewModel
import com.example.taskmanager.core.presentation.viewmodel.ManagersViewModel
import com.example.taskmanager.core.presentation.viewmodel.ProfileViewModel
import com.example.taskmanager.core.presentation.viewmodel.TaskDetailsViewModel
import com.example.taskmanager.core.presentation.viewmodel.TasksViewModel
import com.example.taskmanager.core.ui.ThemeViewModel
import com.example.taskmanager.core.ui.theme.TaskManagerTheme
import com.example.taskmanager.core.utils.Screens
import com.example.taskmanager.core.utils.animatedComposable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskManagerActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val signUpEmployeeViewModel: SignUpEmployeeViewModel by viewModels()
    private val signUpManagerViewModel: SignUpManagerViewModel by viewModels()
    private val signUpAdminViewModel: SignUpAdminViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private val departmentViewModel: DepartmentsViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by viewModels()
    private val managersViewModel: ManagersViewModel by viewModels()
    private val employeesViewModel: EmployeesViewModel by viewModels()
    private val languageViewModel: LanguageViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()

    //    private val profileViewModel: ProfileViewModel by viewModels() you can't use same viewmodel for different screens of this
    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            loginViewModel.isCheckingAuth.value
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            val currentLanguage by languageViewModel.currentLanguage.collectAsState()

            LaunchedEffect(currentLanguage) {
                if (currentLanguage.isNotEmpty()) {
                    languageViewModel.changeLanguage(currentLanguage)
                }
            }
            TaskManagerTheme(darkTheme = isDarkMode) {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screens.AuthScreens.Login.route,
                    ) {
                        animatedComposable(Screens.AuthScreens.Login.route) {
                            LoginScreen(
                                onLoginClick = {
                                    loginViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = loginViewModel
                            )
                        }

                        animatedComposable(Screens.AuthScreens.ResetPassword.route) {
                            ResetPasswordScreen(
                                onResetClick = {
                                    loginViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = loginViewModel
                            )
                        }


                        animatedComposable(Screens.AuthScreens.ChooseRole.route) {
                            RoleSelectionScreen(navController = navController)
                        }

                        animatedComposable(Screens.AuthScreens.SignUp.Employee.route) {
                            SignUpEmployeeScreen(
                                onSignUpClick = {
                                    signUpEmployeeViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = signUpEmployeeViewModel
                            )
                        }

                        animatedComposable(Screens.AuthScreens.VerifyOtp.Employee.route) {
                            OtpEmployeeVerificationScreen(
                                onVerifyClick = {
                                    signUpEmployeeViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = signUpEmployeeViewModel
                            )
                        }

                        animatedComposable(Screens.AuthScreens.SignUp.Manager.route) {
                            SignUpManagerScreen(
                                onSignUpClick = {
                                    signUpManagerViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = signUpManagerViewModel
                            )
                        }

                        animatedComposable(Screens.AuthScreens.VerifyOtp.Manager.route) {
                            OtpManagerVerificationScreen(
                                onVerifyClick = {
                                    signUpManagerViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = signUpManagerViewModel
                            )
                        }

                        animatedComposable(Screens.AuthScreens.SignUp.Admin.route) {
                            SignUpAdminScreen(
                                onSignUpClick = {
                                    signUpAdminViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = signUpAdminViewModel
                            )
                        }

                        animatedComposable(Screens.AuthScreens.VerifyOtp.Admin.route) {
                            OtpAdminVerificationScreen(
                                onVerifyClick = {
                                    signUpAdminViewModel.onEvent(it)
                                },
                                navController = navController,
                                viewModel = signUpAdminViewModel
                            )
                        }

                        animatedComposable(Screens.AppScreens.Dashboard.route) {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                loginViewModel = loginViewModel,
                                navController = navController
                            )
                        }

                        animatedComposable(Screens.AppScreens.Departments.route) {
                            DepartmentsScreen(
                                departmentsViewModel = departmentViewModel,
                                loginViewModel = loginViewModel,
                                navController = navController,
                            )
                        }

                        animatedComposable(
                            route = Screens.AppScreens.DepartmentDetails.route,
                            arguments = listOf(
                                navArgument("departmentId") { type = NavType.StringType },
                            )) {
                            val departmentDetailsViewModel = hiltViewModel<DepartmentDetailsViewModel>()
                            DepartmentDetailsScreen(
                                navController = navController,
                                departmentDetailsViewModel = departmentDetailsViewModel,
                            )
                        }

                        animatedComposable(Screens.AppScreens.Tasks.route) {
                            TasksScreen(
                                loginViewModel = loginViewModel,
                                navController = navController,
                                tasksViewModel = tasksViewModel
                            )
                        }

                        animatedComposable(Screens.AppScreens.Managers.route) {
                            ManagersScreen(
                                loginViewModel = loginViewModel,
                                navController = navController,
                                managersViewModel = managersViewModel,
                                departmentViewModel = departmentViewModel
                            )
                        }

                        animatedComposable(Screens.AppScreens.Employees.route) {
                            EmployeesScreen(
                                loginViewModel = loginViewModel,
                                navController = navController,
                                employeesViewModel = employeesViewModel,
                                departmentViewModel = departmentViewModel
                            )
                        }
                        animatedComposable(
                            route = Screens.AppScreens.Profile.route,
                            arguments = listOf(
                                navArgument("userId") {
                                    type = NavType.StringType
                                },
                                navArgument("role") {
                                    type = NavType.StringType
                                }
                            )) {
                            val profileViewModel: ProfileViewModel = hiltViewModel()

                            ProfileScreen(
                                profileViewModel = profileViewModel,
                                navController = navController
                            )
                        }

                        animatedComposable(Screens.AppScreens.CurrentUserProfile.route,){
                            val profileViewModel: ProfileViewModel = hiltViewModel()
                            val isCurrent = it.arguments?.getString("isCurrent")?.toBoolean() ?: false
                            ProfileScreen(
                                profileViewModel = profileViewModel,
                                navController = navController,
                                isCurrent = isCurrent
                            )
                        }

                        animatedComposable(
                            route = Screens.AppScreens.TaskDetails.route,
                            arguments = listOf(
                                navArgument("taskId") { type = NavType.StringType },
                                navArgument("role") { type = NavType.StringType }
                            )) {
                            val taskDetailsViewModel: TaskDetailsViewModel = hiltViewModel()

                            TaskDetailsScreen(
                                taskDetailsViewModel = taskDetailsViewModel,
                                navController = navController
                            )
                        }

                        animatedComposable(Screens.AppScreens.Settings.route){
                            SettingsScreen(
                                languageViewModel = languageViewModel,
                                themeViewModel = themeViewModel
                            ) {
                                navController.navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }
}