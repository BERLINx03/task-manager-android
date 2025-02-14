package com.example.taskmanager.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskmanager.auth.presentation.view.LoginScreen
import com.example.taskmanager.auth.presentation.view.OtpVerificationScreen
import com.example.taskmanager.auth.presentation.view.RoleSelectionScreen
import com.example.taskmanager.auth.presentation.view.SignUpEmployeeScreen
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.auth.presentation.viewmodel.SignUpEmployeeViewModel
import com.example.taskmanager.auth.utils.Screens
import com.example.taskmanager.core.ui.theme.TaskManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskManagerActivity : ComponentActivity() {
    private val loginViewModel: LoginViewModel by viewModels()
    private val signUpEmployeeViewModel: SignUpEmployeeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskManagerTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding),content ={
                        NavHost(
                            navController = navController,
                            startDestination = Screens.AuthScreens.Login.route
                        ){
                            composable(Screens.AuthScreens.Login.route){
                                LoginScreen(
                                    onLoginClick = {
                                        loginViewModel.onEvent(it)
                                    },
                                    navController = navController,
                                    viewModel = loginViewModel
                                )
                            }

                            composable(Screens.AuthScreens.ChooseRole.route){
                                RoleSelectionScreen(navController = navController)
                            }

                            composable(Screens.AuthScreens.SignUp.Employee.route) {
                                SignUpEmployeeScreen(
                                    onSignUpClick = {
                                        signUpEmployeeViewModel.onEvent(it)
                                    },
                                    navController = navController,
                                    viewModel = signUpEmployeeViewModel
                                )
                            }

                            composable(Screens.AuthScreens.VerifyOtp.route){
                                OtpVerificationScreen(
                                    onVerifyClick = {
                                        signUpEmployeeViewModel.onEvent(it)
                                    },
                                    navController = navController,
                                    viewModel = signUpEmployeeViewModel
                                )
                            }
                        }
                    })
                }
            }
        }
    }
}
