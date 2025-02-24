package com.example.taskmanager.core.presentation.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.presentation.intents.ManagersIntents
import com.example.taskmanager.core.presentation.viewmodel.DepartmentsViewModel
import com.example.taskmanager.core.presentation.viewmodel.ManagersViewModel
import com.example.taskmanager.core.utils.NavigationDrawer
import com.example.taskmanager.core.utils.PermissionDialog
import com.example.taskmanager.core.utils.Screens
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagersScreen(
    managersViewModel: ManagersViewModel,
    loginViewModel: LoginViewModel,
    departmentViewModel: DepartmentsViewModel,
    navController: NavController,
) {
    val state by managersViewModel.managersState.collectAsStateWithLifecycle()
    val user = state.user
    val userRole = managersViewModel.userRole.collectAsStateWithLifecycle("").value
    val departmentState by departmentViewModel.departmentsState.collectAsStateWithLifecycle()

    val showPermissionDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pullRefreshState = rememberPullToRefreshState()

    val colorScheme = MaterialTheme.colorScheme

    NavigationDrawer(
        drawerState = drawerState,
        user = user,
        managersSelected = true,
        loginViewModel = loginViewModel,
        navController = navController
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Managers",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Menu, "Open Menu")
                    }
                },
                actions = {
                    OutlinedTextField(
                        value = state.searchQuery ?: "",
                        onValueChange = {
                            managersViewModel.onIntent(
                                ManagersIntents.OnSearchQueryChange(it)
                            )
                        },
                        modifier = Modifier
                            .width(240.dp)
                            .padding(end = 8.dp),
                        placeholder = { Text("Search...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            disabledContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                    )
                }
            )

            PullToRefreshBox(
                state = pullRefreshState,
                onRefresh = { managersViewModel.onIntent(ManagersIntents.Refresh) },
                isRefreshing = state.isRefreshing,
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = state.isRefreshing,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        state = pullRefreshState
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.managers.size) { index ->
                                val manager = state.managers[index]
                                ManagerCard(
                                    manager = manager,
                                    departments = departmentState.departments,
                                    onClick = {
                                        if (userRole == "Admin" || userRole == "Manager") {
                                            navController.navigate(
                                                Screens.AppScreens.Profile.route
                                                    .replace("{userId}",manager.id.toString())
                                                    .replace("{role}","Manager")
                                            )
                                        } else {
                                            showPermissionDialog.value = true
                                        }
                                    }
                                )
                            }

                            if (state.managers.isEmpty() && !state.isLoading && state.errorMessage == null) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 64.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Group,
                                            contentDescription = "No Managers",
                                            modifier = Modifier.size(48.dp),
                                            tint = colorScheme.onBackground.copy(alpha = 0.4f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No managers found",
                                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        if (state.managers.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorScheme.surface)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalIconButton(
                                    onClick = { managersViewModel.onIntent(ManagersIntents.LoadPreviousPage) },
                                    enabled = state.hasPreviousPage,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous Page",
                                        tint = if (state.hasPreviousPage) colorScheme.onSecondaryContainer else colorScheme.onSurface.copy(
                                            alpha = 0.4f
                                        )
                                    )
                                }

                                Text(
                                    text = "Page ${state.currentPage} of ${state.totalPages}",
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurface
                                )

                                FilledTonalIconButton(
                                    onClick = { managersViewModel.onIntent(ManagersIntents.LoadNextPage) },
                                    enabled = state.hasNextPage,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Next Page",
                                        tint = if (state.hasNextPage) colorScheme.onSecondaryContainer else colorScheme.onSurface.copy(
                                            alpha = 0.4f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    state.errorMessage?.let {
                        if (it.isNotEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = colorScheme.error,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = it,
                                            color = colorScheme.onErrorContainer,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                managersViewModel.onIntent(ManagersIntents.Refresh)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colorScheme.error
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Retry",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (state.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                        }
                    }
                }
            }
        }

        if (showPermissionDialog.value) {
            PermissionDialog(showPermissionDialog)
        }
    }
}

@Composable
fun ManagerCard(
    manager: ManagerAndEmployee,
    departments: List<Department>,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    val managerDepartment = departments.find { it.id == manager.departmentId }
    Timber.d("Manager: $manager Department: $managerDepartment")

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colorScheme.primary.copy(alpha = 0.7f),
                                colorScheme.secondary.copy(alpha = 0.8f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${manager.firstName.take(1)}${manager.lastName.take(1)}",
                    color = colorScheme.onPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${manager.firstName} ${manager.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )

                Text(
                    text = managerDepartment?.title ?: "Not found. Try reload departments",
                    fontSize = 14.sp,
                    color = colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = if (manager.gender == 0) Icons.Default.Male else Icons.Default.Female,
                contentDescription = "Gender",
                tint = colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = colorScheme.primary.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}