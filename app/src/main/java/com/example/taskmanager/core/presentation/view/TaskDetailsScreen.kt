package com.example.taskmanager.core.presentation.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SupervisorAccount
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.core.presentation.intents.TaskDetailsIntents
import com.example.taskmanager.core.presentation.viewmodel.TaskDetailsViewModel
import com.example.taskmanager.core.utils.Screens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskDetailsScreen(
    taskDetailsViewModel: TaskDetailsViewModel,
    navController: NavController,
) {
    val state by taskDetailsViewModel.state.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    val userRole = taskDetailsViewModel.role.collectAsStateWithLifecycle("").value
    val task = state.task

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = rememberLazyListState()
        ) {
            stickyHeader {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            task?.title?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } ?: Text("Task Details")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { navController.navigate(Screens.AppScreens.EditTask.createRoute("edit")) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Task",
                                tint = colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = colorScheme.error
                            )
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
                        .padding(16.dp)
                ) {
                    // Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            val statusInfo = when (task?.status) {
                                1 -> StatusInfo("TO DO", colorScheme.primary)
                                2 -> StatusInfo("IN PROGRESS", colorScheme.tertiary)
                                3 -> StatusInfo("DONE", colorScheme.secondary)
                                else -> StatusInfo("UNKNOWN", colorScheme.error)
                            }

                            val priorityInfo = when (task?.priority) {
                                0 -> PriorityInfo("HIGH", colorScheme.error, Icons.Default.ArrowUpward)
                                1 -> PriorityInfo("MEDIUM", colorScheme.tertiary, Icons.AutoMirrored.Filled.ArrowForward)
                                2 -> PriorityInfo("LOW", colorScheme.secondary, Icons.Default.ArrowDownward)
                                else -> PriorityInfo("UNKNOWN", colorScheme.error, Icons.Default.Error)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = statusInfo.color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, statusInfo.color.copy(alpha = 0.2f))
                                ) {
                                    Text(
                                        text = statusInfo.text,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = statusInfo.color
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = priorityInfo.icon,
                                        contentDescription = "Priority",
                                        tint = priorityInfo.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = priorityInfo.text,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = priorityInfo.color
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Details Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Task Details",
                                style = MaterialTheme.typography.titleMedium,
                                color = colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            task?.description?.let {
                                TaskDetailItem(
                                    icon = Icons.Default.Description,
                                    label = "Description",
                                    value = it
                                )
                            }

                            task?.dueDate?.let {
                                TaskDetailItem(
                                    icon = Icons.Default.DateRange,
                                    label = "Due Date",
                                    value = it
                                )
                            }

                            if (userRole == "Admin" || userRole == "Manager") {
                                TaskDetailItem(
                                    icon = Icons.Default.Badge,
                                    label = "Employee ID",
                                    value = task?.employeeId.toString()
                                )

                                TaskDetailItem(
                                    icon = Icons.Default.SupervisorAccount,
                                    label = "Manager ID",
                                    value = task?.managerId.toString()
                                )

                                TaskDetailItem(
                                    icon = Icons.Default.Business,
                                    label = "Department ID",
                                    value = task?.departmentId.toString()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading indicator
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Error handling with Snackbar
        LaunchedEffect(state.errorMessage) {
            state.errorMessage?.let { errorMessage ->
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = errorMessage,
                        actionLabel = "Dismiss",
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                    // Clear the error after showing
                    taskDetailsViewModel.onIntent(TaskDetailsIntents.Refresh)
                }
            }
        }

        // Custom Snackbar
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


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Track task title for feedback message
                        val taskTitle = task?.title ?: "Task"

                        // Execute delete operation
                        taskDetailsViewModel.onIntent(TaskDetailsIntents.DeleteTask)
                        showDeleteDialog = false

                        // Show snackbar before navigating
                        coroutineScope.launch {
                            // Wait briefly to see if there's an error
                            delay(300)

                            // If no error appears, show success and navigate
                            if (state.errorMessage == null) {
                                snackbarHostState.showSnackbar(
                                    message = "$taskTitle deleted successfully",
                                    actionLabel = "OK",
                                    withDismissAction = true
                                )
                                navController.navigateUp()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}



private data class StatusInfo(
    val text: String,
    val color: androidx.compose.ui.graphics.Color
)

private data class PriorityInfo(
    val text: String,
    val color: androidx.compose.ui.graphics.Color,
    val icon: ImageVector
)

@Composable
private fun TaskDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 16.sp,
            color = colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}