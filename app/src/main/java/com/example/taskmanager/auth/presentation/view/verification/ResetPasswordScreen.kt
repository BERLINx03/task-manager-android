package com.example.taskmanager.auth.presentation.view.verification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanager.auth.data.remote.requestmodels.ResetPasswordRequestDto
import com.example.taskmanager.auth.presentation.event.LoginUiEvent
import com.example.taskmanager.auth.presentation.view.toForgotPasswordRequest
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.auth.utils.Screens
import kotlinx.coroutines.delay

/**
 * @author Abdallah Elsokkary
 */
@Composable
fun ResetPasswordScreen(
    onResetClick: (LoginUiEvent) -> Unit,
    navController: NavController,
    viewModel: LoginViewModel
) {
    val state by viewModel.loginState.collectAsState()
    var timeLeft by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        canResend = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Reset Password",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Reset Your Password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "Enter verification code and new password",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Error Message
            AnimatedVisibility(visible = state.error != null) {
                state.error?.let {
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

            // OTP Field
            OutlinedTextField(
                value = state.otp,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        viewModel.onEvent(LoginUiEvent.OnOtpChange(it))
                    }
                },
                label = { Text("Verification Code") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "OTP"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                visualTransformation = { text ->
                    val transformed = text.text.map { "●" }.joinToString(" ")
                    TransformedText(
                        AnnotatedString(transformed),
                        object : OffsetMapping {
                            override fun originalToTransformed(offset: Int): Int {
                                return (offset * 2).coerceAtMost(transformed.length)
                            }
                            override fun transformedToOriginal(offset: Int): Int {
                                return (offset / 2).coerceAtMost(text.text.length)
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password Field
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = { viewModel.onEvent(LoginUiEvent.OnNewPasswordChange(it)) },
                label = { Text("New Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "New Password"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible)
                                "Hide password" else "Show password"
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

            Spacer(modifier = Modifier.height(24.dp))

            // Reset Password Button
            Button(
                onClick = {
                    onResetClick(LoginUiEvent.ResetPassword(
                        ResetPasswordRequestDto(
                            email = state.loginRequest.username,
                            otpCode = state.otp,
                            newPassword = state.newPassword
                        )
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading && state.otp.length == 6 && state.newPassword.isNotEmpty(),
                shape = MaterialTheme.shapes.medium
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Reset Password")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!canResend) {
                    Text(
                        text = "Resend code in ${timeLeft}s",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    TextButton(
                        onClick = {
                            timeLeft = 60
                            canResend = false
                            viewModel.onEvent(LoginUiEvent.ForgotPassword(state.loginRequest.username.toForgotPasswordRequest()))
                        }
                    ) {
                        Text("Resend Code")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigate(Screens.AuthScreens.Login.route) }
            ) {
                Text("Back to Login")
            }
        }
    }
}