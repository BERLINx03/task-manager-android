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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.domain.model.ManagerAndEmployee
import com.example.taskmanager.core.presentation.intents.DepartmentDetailsIntents
import com.example.taskmanager.core.presentation.intents.ProfileIntents
import com.example.taskmanager.core.presentation.viewmodel.DepartmentDetailsViewModel
import com.example.taskmanager.core.utils.ErrorBox
import com.example.taskmanager.core.utils.PermissionDialog
import com.example.taskmanager.core.utils.Screens

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentDetailsScreen(
    departmentDetailsViewModel: DepartmentDetailsViewModel,
    navController: NavController,
) {
    val departmentState by departmentDetailsViewModel.departmentDetailState.collectAsStateWithLifecycle()
    val userRole by departmentDetailsViewModel.userRole.collectAsStateWithLifecycle("")
    val departmentId = departmentDetailsViewModel.departmentId

    val showDeleteDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val showPermissionDialog = remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme

    val pullRefreshState = remember { PullToRefreshState() }
    val tabState = remember { mutableIntStateOf(0) }
    val tabs = listOf("Managers", "Employees")

    LaunchedEffect(Unit) {
        departmentDetailsViewModel.successAddedMessage.collect {
            snackbarHostState.showSnackbar("Department's name updated successfully")
            departmentDetailsViewModel.onIntent(
                DepartmentDetailsIntents.LoadDepartmentDetails(
                    departmentId
                )
            )
        }
    }


    PullToRefreshBox(
        state = pullRefreshState,
        onRefresh = {
            departmentDetailsViewModel.onIntent(DepartmentDetailsIntents.RefreshEmployees)
            departmentDetailsViewModel.onIntent(DepartmentDetailsIntents.RefreshEmployees)
        },
        isRefreshing = departmentState.isRefreshing,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = departmentState.isRefreshing,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                state = pullRefreshState
            )
        },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = departmentState.department?.title ?: "Department Details",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        if (userRole == "Admin") {
                            IconButton(onClick = { showEditDialog.value = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }

                            IconButton(onClick = { showDeleteDialog.value = true }) {
                                Icon(
                                    Icons.Default.Delete, contentDescription = "Delete",
                                    tint = colorScheme.error
                                )
                            }
                        }
                    }
                )

                // Department Details Card
                departmentState.department?.let { department ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = department.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Tag,
                                    contentDescription = "ID",
                                    tint = colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Department ID: ${department.id}",
                                    fontSize = 14.sp,
                                    color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = tabState.intValue,
                    containerColor = colorScheme.surface,
                    contentColor = colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabState.intValue == index,
                            onClick = { tabState.intValue = index },
                            text = { Text(text = title) },
                            icon = {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.SupervisorAccount else Icons.Default.Group,
                                    contentDescription = title
                                )
                            }
                        )
                    }
                }

                // Content based on selected tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (tabState.intValue) {
                        0 -> DepartmentManagersTab(
                            departmentId = departmentId,
                            navController = navController,
                            departmentDetailsViewModel = departmentDetailsViewModel,
                            userRole = userRole,
                            showPermissionDialog = showPermissionDialog
                        )

                        1 -> DepartmentEmployeesTab(
                            departmentId = departmentId,
                            departmentDetailsViewModel = departmentDetailsViewModel,
                            navController = navController,
                            userRole = userRole,
                            showPermissionDialog = showPermissionDialog
                        )
                    }

                    // Error handling
                    departmentState.errorMessage?.let {
                        if (it.isNotEmpty()) {
                            ErrorBox(it) { }
                        }
                    }


                    // Loading indicator
                    if (departmentState.isLoading) {
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

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                snackbar = { snackbarData ->
                    Snackbar(
                        shape = RoundedCornerShape(12.dp),
                        containerColor = colorScheme.secondaryContainer,
                        contentColor = colorScheme.onSecondaryContainer,
                        actionContentColor = colorScheme.onPrimary,
                        dismissActionContentColor = colorScheme.primary,
                        modifier = Modifier.padding(12.dp),
                        action = snackbarData.visuals.actionLabel?.let { actionLabel ->
                            {
                                TextButton(
                                    onClick = { snackbarData.performAction() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = actionLabel,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        dismissAction = if (snackbarData.visuals.withDismissAction) {
                            {
                                IconButton(onClick = { snackbarData.dismiss() }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = colorScheme.primary
                                    )
                                }
                            }
                        } else null
                    ) {
                        Text(
                            text = snackbarData.visuals.message,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }
    }


    // Dialogs
    if (showDeleteDialog.value) {
        DeleteDepartmentDialog(
            showDialog = showDeleteDialog,
            departmentTitle = departmentState.department?.title ?: "this department",
            onConfirm = {
                departmentDetailsViewModel.onIntent(
                    DepartmentDetailsIntents.DeleteDepartment(
                        departmentId
                    )
                )
                navController.popBackStack()
            }
        )
    }

    if (showEditDialog.value && departmentState.department != null) {
        EditDepartmentDialog(
            showDialog = showEditDialog,
            department = departmentState.department!!,
            onUpdate = { updatedDepartment ->
                departmentDetailsViewModel.onIntent(
                    DepartmentDetailsIntents.UpdateDepartment(
                        updatedDepartment.title
                    )
                )
            }
        )
    }

    if (showPermissionDialog.value) {
        PermissionDialog(showPermissionDialog)
    }
}

@Composable
fun DepartmentManagersTab(
    departmentId: String,
    departmentDetailsViewModel: DepartmentDetailsViewModel,
    navController: NavController,
    userRole: String?,
    showPermissionDialog: MutableState<Boolean>
) {
    val managersState by departmentDetailsViewModel.departmentManagersState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(departmentId) {
        departmentDetailsViewModel.onIntent(
            DepartmentDetailsIntents.LoadDepartmentManagers(
                departmentId
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(managersState.managers.size) { index ->
                val manager = managersState.managers[index]
                ManagerCard(
                    manager = manager,
                    departments = emptyList(),
                    onClick = {
                        if (userRole == "Admin" || userRole == "Manager") {
                            navController.navigate(
                                Screens.AppScreens.Profile.route
                                    .replace("{userId}", manager.id.toString())
                                    .replace("{role}", "Manager")
                            )
                        } else {
                            showPermissionDialog.value = true
                        }
                    }
                )
            }

            if (managersState.managers.isEmpty() && !managersState.isLoading && managersState.errorMessage == null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.SupervisorAccount,
                            contentDescription = "No Managers",
                            modifier = Modifier.size(48.dp),
                            tint = colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No managers found in this department",
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Error handling
        managersState.errorMessage?.let {
            if (it.isNotEmpty()) {
                ErrorBox(it) { }
            }
        }

        // Loading indicator
        if (managersState.isLoading) {
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

@Composable
fun DepartmentEmployeesTab(
    departmentId: String,
    departmentDetailsViewModel: DepartmentDetailsViewModel,
    navController: NavController,
    userRole: String?,
    showPermissionDialog: MutableState<Boolean>
) {
    val employeesState by departmentDetailsViewModel.departmentEmployeesState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(departmentId) {
        departmentDetailsViewModel.onIntent(
            DepartmentDetailsIntents.LoadDepartmentEmployees(
                departmentId
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(employeesState.employees.size) { index ->
                val employee = employeesState.employees[index]
                EmployeeCard(
                    employee = employee,
                    onClick = {
                        if (userRole == "Admin" || userRole == "Manager") {
                            navController.navigate(
                                Screens.AppScreens.Profile.route
                                    .replace("{userId}", employee.id.toString())
                                    .replace("{role}", "Employee")
                            )
                        } else {
                            showPermissionDialog.value = true
                        }
                    }
                )
            }

            if (employeesState.employees.isEmpty() && !employeesState.isLoading && employeesState.errorMessage == null) {
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
                            contentDescription = "No Employees",
                            modifier = Modifier.size(48.dp),
                            tint = colorScheme.onBackground.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No employees found in this department",
                            color = colorScheme.onBackground.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Error handling
        employeesState.errorMessage?.let {
            if (it.isNotEmpty()) {
                ErrorBox(it) { }
            }
        }
    }

    // Loading indicator
    if (employeesState.isLoading) {
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

@Composable
fun EmployeeCard(
    employee: ManagerAndEmployee,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

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
                                colorScheme.secondary.copy(alpha = 0.7f),
                                colorScheme.tertiary.copy(alpha = 0.8f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${employee.firstName.take(1)}${employee.lastName.take(1)}",
                    color = colorScheme.onSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${employee.firstName} ${employee.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }

            Icon(
                imageVector = if (employee.gender == 0) Icons.Default.Male else Icons.Default.Female,
                contentDescription = "Gender",
                tint = colorScheme.secondary.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View Details",
                tint = colorScheme.secondary.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun DeleteDepartmentDialog(
    showDialog: MutableState<Boolean>,
    departmentTitle: String,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = {
            Text(
                text = "Delete Department",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete $departmentTitle? This action cannot be undone and will also affect all employees and managers in this department."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    showDialog.value = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { showDialog.value = false }
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditDepartmentDialog(
    showDialog: MutableState<Boolean>,
    department: Department,
    onUpdate: (Department) -> Unit
) {
    val title = remember { mutableStateOf(department.title) }

    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = {
            Text(
                text = "Edit Department",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Department Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(
                        department.copy(
                            title = title.value,
                        )
                    )
                    showDialog.value = false
                },
                enabled = title.value.isNotEmpty()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { showDialog.value = false }
            ) {
                Text("Cancel")
            }
        }
    )
}