package com.example.taskmanager.core.presentation.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.presentation.intents.ProfileIntents
import com.example.taskmanager.core.presentation.viewmodel.ProfileViewModel
import com.example.taskmanager.core.utils.DeleteManagerDialog
import com.example.taskmanager.core.utils.Screens
import java.util.UUID

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    navController: NavController,
    isCurrent: Boolean = false
) {
    val state by profileViewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val userRole = profileViewModel.role.collectAsStateWithLifecycle("").value
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val department = state.department

    val isCurrentUser = navController.currentBackStackEntry
        ?.arguments?.getString("isCurrentUser")?.toBoolean() ?: false

    LaunchedEffect(Unit) {
        if (isCurrentUser) {
            profileViewModel.onIntent(ProfileIntents.LoadCurrentUser)
        }
    }
    LaunchedEffect(state.deletedSuccessfully) {
        if (state.deletedSuccessfully) {
            navController.navigateUp()
        }
    }

    PullToRefreshBox(
        state = pullRefreshState,
        onRefresh = { profileViewModel.onIntent(ProfileIntents.Refresh) },
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
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState()
            ) {
                stickyHeader {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${state.firstName} ${state.lastName}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        },
                        actions = {
                            if (isCurrent)
                                IconButton(onClick = { showEditDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = colorScheme.primary
                                    )
                                }

                            if (userRole == "Admin") {
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete User",
                                        tint = colorScheme.error
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // User Role Badge
                        Surface(
                            color = when (profileViewModel.userType) {
                                "Admin" -> colorScheme.error.copy(alpha = 0.1f)
                                "Manager" -> colorScheme.primary.copy(alpha = 0.1f)
                                else -> colorScheme.tertiary.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp, when (profileViewModel.userType) {
                                    "Admin" -> colorScheme.error.copy(alpha = 0.2f)
                                    "Manager" -> colorScheme.primary.copy(alpha = 0.2f)
                                    else -> colorScheme.tertiary.copy(alpha = 0.2f)
                                }
                            )
                        ) {
                            Text(
                                text = profileViewModel.userType.ifEmpty { state.role },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = when (profileViewModel.userType) {
                                    "Admin" -> colorScheme.error
                                    "Manager" -> colorScheme.primary
                                    else -> colorScheme.tertiary
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profile info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        colorScheme.primary,
                                                        colorScheme.secondary
                                                    )
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${state.firstName.take(1)}${
                                                state.lastName.take(
                                                    1
                                                )
                                            }",
                                            color = colorScheme.onPrimary,
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${state.firstName} ${state.lastName}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = colorScheme.onSurface
                                        )

                                        if (profileViewModel.userId.isNotEmpty()) {
                                            Text(
                                                text = "ID: ${profileViewModel.userId}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))



                                ProfileDetailItem(
                                    icon = Icons.Default.Phone,
                                    label = "Phone",
                                    value = state.phoneNumber
                                )

                                ProfileDetailItem(
                                    icon = if (state.gender == 0) Icons.Default.Male else Icons.Default.Female,
                                    label = "Gender",
                                    value = if (state.gender == 0) "Male" else "Female"
                                )

                                ProfileDetailItem(
                                    icon = Icons.Default.DateRange,
                                    label = "Birth Date",
                                    value = state.birthDate
                                )

//                                 Only show department for Manager and Employee
                                if (profileViewModel.userType != "Admin" && department.isNotEmpty()) {
                                    ProfileDetailItem(
                                        icon = Icons.Default.Business,
                                        label = "Department",
                                        value = department
                                    )
                                }


                                if (profileViewModel.userType == "Employee") {
                                    ProfileDetailItem(
                                        icon = Icons.Default.Work,
                                        label = "Tasks Completed",
                                        value = "${state.tasksCompleted}"
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = if (profileViewModel.userType == "Manager") {"Tasks Managed by ${state.firstName}"}
                                else if (profileViewModel.userType == "Employee") {"Tasks Assigned to ${state.firstName}"} else {
                                    ""
                                },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )

                    }
                }

                items(state.tasks.size) { index ->
                    val task = state.tasks[index]
                    TaskCardinProfile(
                        task = task,
                        onClick = {
                            if (userRole == "Admin" || userRole == "Manager" || userRole == "Employee") {
                                navController.navigate(
                                    Screens.AppScreens.TaskDetails.route
                                        .replace("{taskId}", task.id.toString())
                                        .replace("{role}", userRole)
                                )
                            }
                        }
                    )
                }

                if ((state.tasks.isEmpty() && !state.isLoading) && userRole != "Admin") {
                    item {
                        EmptyTasksPlaceholder()
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }

        // Delete dialog
        DeleteManagerDialog(
            showDialog = showDeleteDialog,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                if (profileViewModel.userType == "Manager") {
                    run {
                        profileViewModel.onIntent(
                            ProfileIntents.DeleteManager(
                                UUID.fromString(
                                    profileViewModel.userId
                                )
                            )
                        )
                    }
                } else if (profileViewModel.userType == "Employee") {
                    run {
                        profileViewModel.onIntent(
                            ProfileIntents.DeleteEmployee(
                                UUID.fromString(
                                    profileViewModel.userId
                                )
                            )
                        )
                    }
                }
            },
            isLoading = state.isLoading,
            firstName = state.firstName,
            lastName = state.lastName
        )

        // Edit profile dialog
        if (showEditDialog) {
            EditProfileDialog(
                showDialog = showEditDialog,
                onDismiss = { showEditDialog = false },
                onSave = { firstName, lastName, phoneNumber, gender, birthDate ->
                    profileViewModel.onIntent(
                        ProfileIntents.UpdateProfile(
                            firstName = firstName,
                            lastName = lastName,
                            phoneNumber = phoneNumber,
                            gender = gender,
                            birthDate = birthDate
                        )
                    )
                    showEditDialog = false
                },
                initialFirstName = state.firstName,
                initialLastName = state.lastName,
                initialPhoneNumber = state.phoneNumber,
                initialGender = state.gender,
                initialBirthDate = state.birthDate,
                isLoading = state.isLoading
            )
        }
    }
}

@Composable
private fun ProfileDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TaskCardinProfile(task: Task, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    val (statusText, statusColor) = when (task.status) {
        1 -> "TO DO" to colorScheme.primary
        2 -> "IN PROGRESS" to colorScheme.tertiary
        3 -> "DONE" to colorScheme.secondary
        else -> "UNKNOWN" to colorScheme.error
    }

    val priorityInfo = when (task.priority) {
        0 -> Triple("HIGH", colorScheme.error, Icons.Default.ArrowUpward)
        1 -> Triple("MEDIUM", colorScheme.tertiary, Icons.AutoMirrored.Filled.ArrowForward)
        2 -> Triple("LOW", colorScheme.secondary, Icons.Default.ArrowDownward)
        else -> Triple("UNKNOWN", colorScheme.error, Icons.Default.Error)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurface
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = priorityInfo.third,
                        contentDescription = "Priority",
                        tint = priorityInfo.second,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = priorityInfo.first,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityInfo.second
                    )
                }
            }

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor
                    )
                }

                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun EmptyTasksPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Assignment,
            contentDescription = "No Tasks",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No tasks found",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, phoneNumber: String, gender: Int, birthDate: String) -> Unit,
    initialFirstName: String,
    initialLastName: String,
    initialPhoneNumber: String,
    initialGender: Int,
    initialBirthDate: String,
    isLoading: Boolean
) {
    var firstName by remember { mutableStateOf(initialFirstName) }
    var lastName by remember { mutableStateOf(initialLastName) }
    var phoneNumber by remember { mutableStateOf(initialPhoneNumber) }
    var gender by remember { mutableStateOf(initialGender) }
    var birthDate by remember { mutableStateOf(initialBirthDate) }
    var expanded by remember { mutableStateOf(false) }

    val genderOptions = listOf("Male", "Female")

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Profile") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Birth Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true
                    )

                    // Gender dropdown
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = if (gender == 0) "Male" else "Female",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            genderOptions.forEachIndexed { index, option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = index
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSave(firstName, lastName, phoneNumber, gender, birthDate)
                    },
                    enabled = !isLoading && firstName.isNotBlank() && lastName.isNotBlank() && phoneNumber.isNotBlank() && birthDate.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}