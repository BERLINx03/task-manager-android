package com.example.taskmanager.core.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.taskmanager.auth.presentation.event.LoginUiEvent
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.core.domain.model.User

/**
 * @author Abdallah Elsokkary
 */
@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    user: User?,
    loginViewModel: LoginViewModel,
    navController: NavController,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.padding(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = user?.let {
                                "${it.firstName} ${it.lastName}"
                            } ?: "Unknown User",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Items
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, "Profile Settings") },
                    label = { Text("Profile Settings") },
                    selected = false,
                    onClick = { /* Handle profile settings navigation */ },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Build, "App Settings") },
                    label = { Text("App Settings") },
                    selected = false,
                    onClick = { /* Handle app settings navigation */ },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.SupervisorAccount, "Managers") },
                    label = { Text("Managers") },
                    selected = false,
                    onClick = { /* Handle managers navigation */ },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Group, "Employees") },
                    label = { Text("Employees") },
                    selected = false,
                    onClick = { /* Handle employees navigation */ },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.weight(1f))

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") },
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        loginViewModel.onEvent(LoginUiEvent.Logout)
                        navController.navigate(Screens.AuthScreens.Login.route)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        content()
    }
}