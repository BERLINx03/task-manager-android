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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.taskmanager.auth.presentation.viewmodel.LoginViewModel
import com.example.taskmanager.core.domain.model.Department
import com.example.taskmanager.core.presentation.intents.DepartmentIntents
import com.example.taskmanager.core.presentation.viewmodel.DepartmentDetailsViewModel
import com.example.taskmanager.core.presentation.viewmodel.DepartmentsViewModel
import com.example.taskmanager.core.utils.ErrorBox
import com.example.taskmanager.core.utils.NavigationDrawer
import com.example.taskmanager.core.utils.PermissionDialog
import com.example.taskmanager.core.utils.Screens
import com.example.taskmanager.core.utils.SortOption
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentsScreen(
    departmentsViewModel: DepartmentsViewModel,
    loginViewModel: LoginViewModel,
    navController: NavController
) {
    val state by departmentsViewModel.departmentsState.collectAsStateWithLifecycle()
    val user = state.user
    val userRole = departmentsViewModel.userRole.collectAsStateWithLifecycle("").value
    val snackbarHostState = remember { SnackbarHostState() }


    val showAddDialog = remember { mutableStateOf(false) }
    val showPermissionDialog = remember { mutableStateOf(false) }
    val showSortOptions = remember { mutableStateOf(false) }
    val newDepartmentTitle = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val pullRefreshState = rememberPullToRefreshState()

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        departmentsViewModel.addedSuccessfully.collect {
            snackbarHostState.showSnackbar("Department added successfully")
            departmentsViewModel.onIntent(DepartmentIntents.LoadDepartments(true))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavigationDrawer(
            drawerState = drawerState,
            user = user,
            loginViewModel = loginViewModel,
            navController = navController
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                TopAppBar(
                    title = {},
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
                                departmentsViewModel.onIntent(
                                    DepartmentIntents.OnSearchQueryChange(it)
                                )
                            },
                            modifier = Modifier
                                .width(300.dp)
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
                        Box {
                            IconButton(
                                onClick = { showSortOptions.value = true }
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort Options",
                                    tint = colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showSortOptions.value,
                                onDismissRequest = { showSortOptions.value = false },
                                modifier = Modifier.width(180.dp)
                            ) {
                                Text(
                                    text = "Sort by",
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )

                                HorizontalDivider()

                                SortOption(
                                    title = "Name (A-Z)",
                                    selected = state.sortOption == "title_asc",
                                    onClick = {
                                        departmentsViewModel.onIntent(
                                            DepartmentIntents.SetSortOption(
                                                "title_asc"
                                            )
                                        )
                                        showSortOptions.value = false
                                    }
                                )

                                SortOption(
                                    title = "Name (Z-A)",
                                    selected = state.sortOption == "title_desc",
                                    onClick = {
                                        departmentsViewModel.onIntent(
                                            DepartmentIntents.SetSortOption(
                                                "title_desc"
                                            )
                                        )
                                        showSortOptions.value = false
                                    }
                                )

                                SortOption(
                                    title = "Date Added (Newest)",
                                    selected = state.sortOption == "date_desc",
                                    onClick = {
                                        departmentsViewModel.onIntent(
                                            DepartmentIntents.SetSortOption(
                                                "date_desc"
                                            )
                                        )
                                        showSortOptions.value = false
                                    }
                                )

                                SortOption(
                                    title = "Date Added (Oldest)",
                                    selected = state.sortOption == "date_asc",
                                    onClick = {
                                        departmentsViewModel.onIntent(
                                            DepartmentIntents.SetSortOption(
                                                "date_asc"
                                            )
                                        )
                                        showSortOptions.value = false
                                    }
                                )
                            }
                        }
                    }
                )

                // Main content
                PullToRefreshBox(
                    state = pullRefreshState,
                    onRefresh = { departmentsViewModel.onIntent(DepartmentIntents.Refresh) },
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

                            // Department list
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(state.departments.size) { index ->
                                    val department = state.departments[index]
                                    DepartmentCard(
                                        department = department,
                                        onClick = {
                                            navController.navigate(
                                                Screens.AppScreens.DepartmentDetails.route
                                                    .replace(
                                                        "{role}",
                                                        if (userRole == "Admin") "Admin" else if (userRole == "Manager") "Manager" else "Employee"
                                                    )
                                                    .replace(
                                                        "{departmentId}",
                                                        department.id.toString()
                                                    )
                                            )

                                        }
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

                            // Pagination controls
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
                                        onClick = { departmentsViewModel.onIntent(DepartmentIntents.LoadPreviousPage) },
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
                                        onClick = { departmentsViewModel.onIntent(DepartmentIntents.LoadNextPage) },
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

                        // Error message overlay
                        state.errorMessage?.let {
                            if (it.isNotEmpty()) {
                                ErrorBox(
                                    error = it,
                                ) {
                                    departmentsViewModel.onIntent(
                                        DepartmentIntents.LoadDepartments(
                                            true
                                        )
                                    )
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
                }
            }

            // Add Department Dialog
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
                                                departmentsViewModel.addDepartment(
                                                    newDepartmentTitle.value
                                                )
                                                showAddDialog.value = false
                                                newDepartmentTitle.value = ""
                                            }
                                        }
                                    },
                                    enabled = newDepartmentTitle.value.isNotBlank(),
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(
                                        horizontal = 24.dp,
                                        vertical = 12.dp
                                    )
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
            if (showPermissionDialog.value) {
                PermissionDialog(showPermissionDialog)
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
            // Initial letter avatar with gradient background
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
                    text = department.title.take(1).uppercase(),
                    color = colorScheme.onPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Department name with elegant typography
            Text(
                text = department.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                letterSpacing = 0.2.sp,
                modifier = Modifier.weight(1f)
            )

            // Chevron icon with subtle animation
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View Department",
                    tint = colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}