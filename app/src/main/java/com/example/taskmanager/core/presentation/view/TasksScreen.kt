package com.example.taskmanager.core.presentation.view

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Title
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.core.domain.model.Task
import com.example.taskmanager.core.presentation.intents.TasksIntents
import com.example.taskmanager.core.presentation.viewmodel.TasksViewModel
import com.example.taskmanager.core.utils.NavigationDrawer
import com.example.taskmanager.core.utils.PermissionDialog
import com.example.taskmanager.core.utils.Screens
import kotlinx.coroutines.launch

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasksViewModel: TasksViewModel,
    loginViewModel: LoginViewModel,
    navController: NavController,
) {
    val state by tasksViewModel.tasksState.collectAsStateWithLifecycle()
    val user = state.user
    val userRole = tasksViewModel.userRole.collectAsStateWithLifecycle("").value

    // Dialog states
    val showAddDialog = remember { mutableStateOf(false) }
    val showPermissionDialog = remember { mutableStateOf(false) }

    // Task form states
    val newTaskTitle = remember { mutableStateOf("") }
    val newTaskDescription = remember { mutableStateOf("") }
    val newTaskDueDate = remember { mutableStateOf("") }
    val newTaskPriority = remember { mutableIntStateOf(1) }

    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pullRefreshState = rememberPullToRefreshState()
    val colorScheme = MaterialTheme.colorScheme

    NavigationDrawer(
        drawerState = drawerState,
        user = user,
        loginViewModel = loginViewModel,
        tasksSelected = true,
        navController = navController
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar with search functionality
            TopAppBar(
                title = {
                    Text(
                        text = "Tasks",
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
                            tasksViewModel.onIntent(TasksIntents.OnSearchQueryChange(it))
                        },
                        modifier = Modifier
                            .width(240.dp)
                            .padding(end = 8.dp),
                        placeholder = { Text("Search tasks...", fontSize = 14.sp) },
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
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                    )
                }
            )

            // Main content
            PullToRefreshBox(
                state = pullRefreshState,
                onRefresh = { tasksViewModel.onIntent(TasksIntents.Refresh) },
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
                        // Add Task Button
                        if (userRole == "Manager"){
                            Card(
                                onClick = { showAddDialog.value = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.primaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = colorScheme.primary,
                                        modifier = Modifier.size(36.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Add",
                                            tint = colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(8.dp)
                                                .size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Add New Task",
                                        color = colorScheme.onPrimaryContainer,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Tasks list
                        if (userRole == "Admin"){
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(state.tasks.size) { index ->
                                    val task = state.tasks[index]
                                    TaskCardinTasks(
                                        task = task,
                                        onClick = {
                                            navController.navigate(
                                                Screens.AppScreens.TaskDetails.route
                                                    .replace("{taskId}", task.id.toString())
                                                    .replace("{role}", userRole)
                                            )
                                        }
                                    )
                                }

                                if (state.tasks.isEmpty() && !state.isLoading && state.errorMessage == null) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 64.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.Assignment,
                                                contentDescription = "No Tasks",
                                                modifier = Modifier.size(48.dp),
                                                tint = colorScheme.onBackground.copy(alpha = 0.4f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "No tasks found",
                                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Pagination controls
                        if (state.tasks.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorScheme.surface)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalIconButton(
                                    onClick = { tasksViewModel.onIntent(TasksIntents.LoadPreviousPage) },
                                    enabled = state.hasPreviousPage,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous Page",
                                        tint = if (state.hasPreviousPage) colorScheme.onSecondaryContainer
                                        else colorScheme.onSurface.copy(alpha = 0.4f)
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
                                    onClick = { tasksViewModel.onIntent(TasksIntents.LoadNextPage) },
                                    enabled = state.hasNextPage,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Next Page",
                                        tint = if (state.hasNextPage) colorScheme.onSecondaryContainer
                                        else colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }

                    // Error message overlay
                    state.errorMessage?.let {
                        if (it.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
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
                                            tasksViewModel.onIntent(TasksIntents.LoadTasks(true))
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

                    // Loading indicator
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

        // Add Task Dialog
        if (showAddDialog.value) {
            Dialog(onDismissRequest = { showAddDialog.value = false }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AddTask,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Add New Task",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Task Title
                        OutlinedTextField(
                            value = newTaskTitle.value,
                            onValueChange = { newTaskTitle.value = it },
                            label = { Text("Task Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Title,
                                    contentDescription = null,
                                    tint = colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Task Description
                        OutlinedTextField(
                            value = newTaskDescription.value,
                            onValueChange = { newTaskDescription.value = it },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

// Due Date Selection
                        OutlinedTextField(
                            value = newTaskDueDate.value,
                            onValueChange = { newTaskDueDate.value = it },
                            label = { Text("Due Date") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

// Priority Selection
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                "Priority",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PriorityButton(
                                    text = "Low",
                                    isSelected = newTaskPriority.intValue == 1,
                                    onClick = { newTaskPriority.intValue = 1 },
                                    color = colorScheme.primary.copy(alpha = 0.6f)
                                )
                                PriorityButton(
                                    text = "Medium",
                                    isSelected = newTaskPriority.intValue == 2,
                                    onClick = { newTaskPriority.intValue = 2 },
                                    color = colorScheme.primary.copy(alpha = 0.8f)
                                )
                                PriorityButton(
                                    text = "High",
                                    isSelected = newTaskPriority.intValue == 3,
                                    onClick = { newTaskPriority.intValue = 3 },
                                    color = colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

// Dialog Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { showAddDialog.value = false },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, colorScheme.outline)
                            ) {
                                Text(
                                    "Cancel",
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Button(
                                onClick = {
//                                    if (newTaskTitle.value.isNotBlank()) {
//                                        coroutineScope.launch {
//                                            tasksViewModel.onIntent(
//                                                TasksIntents.AddTask(
//                                                    title = newTaskTitle.value,
//                                                    description = newTaskDescription.value,
//                                                    dueDate = newTaskDueDate.value,
//                                                    priority = newTaskPriority.value
//                                                )
//                                            )
//                                            showAddDialog.value = false
//                                            // Reset form fields
//                                            newTaskTitle.value = ""
//                                            newTaskDescription.value = ""
//                                            newTaskDueDate.value = ""
//                                            newTaskPriority.value = 1
//                                        }
//                                    }
                                },
                                enabled = newTaskTitle.value.isNotBlank(),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Add Task",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPermissionDialog.value) {
        PermissionDialog(showPermissionDialog)
    }
}

// Helper Composable for Priority Selection
@Composable
private fun PriorityButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(96.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color else Color.Transparent,
            contentColor = if (isSelected) Color.White else color
        ),
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Task Card Composable
@Composable
fun TaskCardinTasks(
    task: Task,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    // Define priority colors and labels
    val priorityColor = when (task.priority) {
        1 -> colorScheme.primary.copy(alpha = 0.6f)
        2 -> colorScheme.primary.copy(alpha = 0.8f)
        else -> colorScheme.primary
    }

    val priorityLabel = when (task.priority) {
        1 -> "Low"
        2 -> "Medium"
        else -> "High"
    }

    // Define status colors and labels
    val statusColor = when (task.status) {
        1 -> colorScheme.secondary
        2 -> colorScheme.tertiary
        else -> colorScheme.primary
    }

    val statusLabel = when (task.status) {
        1 -> "To Do"
        2 -> "In Progress"
        else -> "Completed"
    }

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
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Task",
                    tint = colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = task.description,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority chip
                Surface(
                    color = priorityColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, priorityColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Flag,
                            contentDescription = null,
                            tint = priorityColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = priorityLabel,
                            fontSize = 12.sp,
                            color = priorityColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status chip
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Due date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = task.dueDate,
                        fontSize = 12.sp,
                        color = colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}