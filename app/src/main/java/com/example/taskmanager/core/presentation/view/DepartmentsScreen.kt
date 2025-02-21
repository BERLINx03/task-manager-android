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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Task
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.presentation.intents.DepartmentIntents
import com.example.taskmanager.core.presentation.viewmodel.DepartmentsViewModel
import com.example.taskmanager.core.utils.NavigationDrawer
import com.example.taskmanager.core.utils.Screens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentsScreen(
    departmentsViewModel: DepartmentsViewModel,
    loginViewModel: LoginViewModel,
    navController: NavController,
    innerPadding: PaddingValues,
    navigateToDepartmentDetail: (String) -> Unit,
) {
    val state by departmentsViewModel.departmentsState.collectAsStateWithLifecycle()
    val user = state.user

    val showAddDialog = remember { mutableStateOf(false) }
    val newDepartmentTitle = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pullRefreshState = rememberPullToRefreshState()

    val colorScheme = MaterialTheme.colorScheme

    NavigationDrawer(
        drawerState = drawerState,
        user = user,
        loginViewModel = loginViewModel,
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 4.dp,
                color = colorScheme.surface
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Departments",
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
                        // CHANGE: Moved search to actions area
                        OutlinedTextField(
                            value = state.searchQuery ?: "",
                            onValueChange = {
                                departmentsViewModel.onIntent(
                                    DepartmentIntents.OnSearchQueryChange(it)
                                )
                            },
                            modifier = Modifier
                                .width(200.dp)
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

                Box(modifier = Modifier.weight(1f)) {
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        state = pullRefreshState,
                        onRefresh = { departmentsViewModel.loadDepartments(forceFetchFromRemote = true) },
                        isRefreshing = state.isRefreshing,
                        indicator = {
                            Indicator(
                                modifier = Modifier.align(Alignment.TopCenter),
                                isRefreshing = state.isRefreshing,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                state = pullRefreshState
                            )
                        }
                    ) {
                        // Add Department Button
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
                                    text = "Add New Department",
                                    color = colorScheme.onPrimaryContainer,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(state.departments.size) { index ->
                                    val department = state.departments[index]
                                    DepartmentCard(
                                        department = department,
                                        onClick = { navigateToDepartmentDetail(department.id.toString()) }
                                    )
                                }

                                if (state.departments.isEmpty() && !state.isLoading && state.errorMessage == null) {
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 64.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                Icons.Default.FolderOpen,
                                                contentDescription = "No Departments",
                                                modifier = Modifier.size(48.dp),
                                                tint = colorScheme.onBackground.copy(alpha = 0.4f)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "No departments found",
                                                color = colorScheme.onBackground.copy(alpha = 0.6f),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

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
                                                    departmentsViewModel.loadDepartments(
                                                        forceFetchFromRemote = true
                                                    )
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

                            if (state.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(),
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
                        if (state.departments.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorScheme.surface)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalIconButton(
                                    onClick = { departmentsViewModel.loadPreviousPage() },
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
                                    onClick = { departmentsViewModel.loadNextPage() },
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
                }
            }
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { /* Handle home navigation */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Business, "Departments") },
                    label = { Text("Departments") },
                    selected = true,
                    onClick = { navController.navigate(Screens.AppScreens.Departments.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Task, "Tasks") },
                    label = { Text("Tasks") },
                    selected = false,
                    onClick = { /* Handle tasks navigation */ }
                )
            }
        }
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
                                Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Add New Department",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = newDepartmentTitle.value,
                            onValueChange = { newDepartmentTitle.value = it },
                            label = { Text("Department Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surface,
                                unfocusedContainerColor = colorScheme.surface,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outline,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    tint = colorScheme.primary.copy(alpha = 0.8f)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

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
                                    if (newDepartmentTitle.value.isNotBlank()) {
                                        coroutineScope.launch {
                                            departmentsViewModel.addDepartment(newDepartmentTitle.value)
                                            showAddDialog.value = false
                                            newDepartmentTitle.value = ""
                                        }
                                    }
                                },
                                enabled = newDepartmentTitle.value.isNotBlank(),
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
                                    "Add",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DepartmentCard(
    department: Department,
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = department.title.take(1).uppercase(),
                        color = colorScheme.onSecondaryContainer,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = department.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "ID: ${department.id}",
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Details",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}