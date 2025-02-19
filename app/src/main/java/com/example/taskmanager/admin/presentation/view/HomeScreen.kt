package com.example.taskmanager.admin.presentation.view

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

/**
 * @author Abdallah Elsokkary
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    innerPadding: PaddingValues
) {
    // State for managing the navigation drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Current user state from ViewModel
//    val currentUser by viewModel.currentUser.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Profile Section
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
                        // Profile Image
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
                            text = "USER",
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

                Divider(modifier = Modifier.padding(vertical = 8.dp))

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
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Welcome",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "currentUser.fullName",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Menu, "Open Menu")
                    }
                }
            )
            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                DashboardContent(
                    stats = DashboardStats(
                        adminCount = 5,
                        managerCount = 12,
                        employeeCount = 48,
                        taskCount = 156,
                        departmentCount = 8
                    ),
                    recentTasks = listOf(
                        TaskItem(
                            title = "Update Employee Handbook",
                            assignee = "John Doe",
                            dueDate = "2024-02-25",
                            status = "In Progress"
                        ),
                        TaskItem(
                            title = "Quarterly Performance Review",
                            assignee = "Jane Smith",
                            dueDate = "2024-02-28",
                            status = "Pending"
                        ),
                    )
                )
            }

            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* Handle home navigation */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Business, "Departments") },
                    label = { Text("Departments") },
                    selected = false,
                    onClick = { /* Handle departments navigation */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Task, "Tasks") },
                    label = { Text("Tasks") },
                    selected = false,
                    onClick = { /* Handle tasks navigation */ }
                )
            }

        }
    }
}

// First, let's create a data class for our statistics
data class DashboardStats(
    val adminCount: Int,
    val managerCount: Int,
    val employeeCount: Int,
    val taskCount: Int,
    val departmentCount: Int
)

// And one for our task items
data class TaskItem(
    val title: String,
    val assignee: String,
    val dueDate: String,
    val status: String
)

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    stats: DashboardStats,
    recentTasks: List<TaskItem>
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Admin Card
                item {
                    StatisticCard(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Admins",
                        count = stats.adminCount,
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                // Manager Card
                item {
                    StatisticCard(
                        icon = Icons.Default.SupervisorAccount,
                        title = "Managers",
                        count = stats.managerCount,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                // Employee Card
                item {
                    StatisticCard(
                        icon = Icons.Default.Group,
                        title = "Employees",
                        count = stats.employeeCount,
                        backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }

                // Task Card
                item {
                    StatisticCard(
                        icon = Icons.Default.Task,
                        title = "Tasks",
                        count = stats.taskCount,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Department Card
                item {
                    StatisticCard(
                        icon = Icons.Default.Business,
                        title = "Departments",
                        count = stats.departmentCount,
                        backgroundColor = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
        }

        // Recent Tasks Header
        item {
            Text(
                text = "Recent Tasks",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Recent Tasks Items
        items(recentTasks) { task ->
            TaskCard(task = task)
        }
    }
}

@Composable
fun StatisticCard(
    icon: ImageVector,
    title: String,
    count: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )

            Column {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TaskCard(task: TaskItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Assigned to: ${task.assignee}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            TaskStatusChip(status = task.status)
        }
    }
}

@Composable
fun TaskStatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = { /* Optional click handler */ },
        label = {
            Text(
                text = status,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = when (status) {
                "In Progress" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                "Completed" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            },
            labelColor = when (status) {
                "In Progress" -> MaterialTheme.colorScheme.primary
                "Completed" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            }
        ),
        modifier = modifier
    )
}