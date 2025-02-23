package com.example.taskmanager.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.taskmanager.core.presentation.view.DashboardScreen
import com.example.taskmanager.core.presentation.viewmodel.DashboardViewModel
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
import com.example.taskmanager.core.presentation.view.DepartmentsScreen
import com.example.taskmanager.core.presentation.view.ManagersScreen
import com.example.taskmanager.core.presentation.view.TasksScreen
import com.example.taskmanager.core.presentation.viewmodel.DepartmentsViewModel
import com.example.taskmanager.core.presentation.viewmodel.ManagersViewModel
import com.example.taskmanager.core.presentation.viewmodel.TasksViewModel
import com.example.taskmanager.core.utils.Screens
import com.example.taskmanager.core.ui.theme.TaskManagerTheme
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

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            loginViewModel.isCheckingAuth.value
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskManagerTheme {
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
                                navController = navController,
                                innerPadding = innerPadding
                            )
                        }

                        animatedComposable(Screens.AppScreens.Departments.route) {
                            DepartmentsScreen(
                                departmentsViewModel = departmentViewModel,
                                loginViewModel = loginViewModel,
                                navController = navController,
                                innerPadding = innerPadding,
                            ) {

                            }
                        }

                        animatedComposable(Screens.AppScreens.Tasks.route) {
                            TasksScreen(
                                loginViewModel = loginViewModel,
                                navController = navController,
                                innerPadding = innerPadding,
                                tasksViewModel = tasksViewModel
                            ) {

                            }
                        }

                        animatedComposable(Screens.AppScreens.Managers.route){
                            ManagersScreen(
                                loginViewModel = loginViewModel,
                                navController = navController,
                                innerPadding = innerPadding,
                                managersViewModel = managersViewModel,
                                departmentViewModel = departmentViewModel
                            ) { }
                        }
                    }
                }
            }
        }
    }
}