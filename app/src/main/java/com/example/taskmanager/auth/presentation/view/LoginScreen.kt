package com.example.taskmanager.auth.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.taskmanager.R
import com.example.taskmanager.auth.data.remote.requestmodels.ForgotPasswordRequestDto
import com.example.taskmanager.auth.presentation.event.LoginUiEvent
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.core.utils.Screens
import com.example.taskmanager.core.presentation.viewmodel.LanguageViewModel

@Composable
fun LoginScreen(
    onLoginClick: (LoginUiEvent) -> Unit,
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel(),
    languageViewModel: LanguageViewModel = hiltViewModel()
) {

    val state = viewModel.loginState.collectAsState()
    val languageState = languageViewModel.currentLanguage.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { route ->
            navController.navigate(route) {
                popUpTo(Screens.AuthScreens.Login.route) { inclusive = true }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        val newLanguage = if (languageState.value == "en") "ar" else "en"
                        languageViewModel.changeLanguage(newLanguage)
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(R.string.switch_language),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (languageState.value == "en") "English" else "Arabic",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TASKER",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.welcome_back),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = stringResource(R.string.sign_in_to_continue),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message
            AnimatedVisibility(visible = state.value.error != null) {
                state.value.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Email Field
            OutlinedTextField(
                value = state.value.loginRequest.username,
                onValueChange = {
                    val cleanedValue = it.replace("\n", "").trim()
                    viewModel.onEvent(LoginUiEvent.OnUsernameChange(cleanedValue))
                },
                label = { Text(stringResource(R.string.email)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(R.string.email)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = state.value.loginRequest.password,
                onValueChange = { viewModel.onEvent(LoginUiEvent.OnPasswordChange(it)) },
                label = { Text(stringResource(R.string.password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(R.string.password)
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible)
                                stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible)
                    VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password
            TextButton(
                onClick = {
                    viewModel.onEvent(LoginUiEvent.ForgotPassword(state.value.loginRequest.username.toForgotPasswordRequest()))
                    navController.navigate(Screens.AuthScreens.ResetPassword.route)
                          },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.forgot_password))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = { onLoginClick(LoginUiEvent.Login(state.value.loginRequest))},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.value.isLoading && state.value.loginRequest.username.isNotEmpty() && state.value.loginRequest.password.isNotEmpty(),
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.value.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.login))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.don_t_have_an_account),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                TextButton(onClick = { navController.navigate(Screens.AuthScreens.ChooseRole.route) }) {
                    Text("Sign Up")
                }
            }
        }
    }
}

fun String.toForgotPasswordRequest(): ForgotPasswordRequestDto{
    return ForgotPasswordRequestDto(
        email = this.trim()
    )
}