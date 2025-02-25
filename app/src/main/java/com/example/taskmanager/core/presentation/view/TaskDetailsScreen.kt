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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.core.presentation.viewmodel.TaskDetailsViewModel

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
                            } ?: Text("\"Task Details\"")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { /*TODO(" PDF download implementation ") */ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download PDF",
                                tint = colorScheme.primary
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

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
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